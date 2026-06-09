package com.example.marklens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.marklens.data.ExamRepository
import com.example.marklens.data.MarkLensDatabase
import com.example.marklens.di.AppViewModelFactory
import com.example.marklens.ocr.OcrEngine
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel
import com.example.marklens.parser.ParsedStudentInfo
import com.example.marklens.parser.ScoreParser
import com.example.marklens.parser.StudentInfoParser
import com.example.marklens.ui.capture.CaptureScreen
import com.example.marklens.ui.capture.CaptureViewModel
import com.example.marklens.ui.list.RecordListScreen
import com.example.marklens.ui.list.RecordListViewModel
import com.example.marklens.ui.review.ReviewScreen
import com.example.marklens.ui.review.ReviewViewModel
import com.example.marklens.ui.review.ScoreField
import com.example.marklens.ui.stats.StatsScreen
import com.example.marklens.ui.stats.StatsViewModel
import kotlinx.coroutines.launch

/**
 * Main entry point. Manual DI, simple state-based navigation.
 *
 * @author Tianyu Yao
 */
class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(applicationContext, MarkLensDatabase::class.java, "marklens.db").build()
    }
    private val repository by lazy {
        ExamRepository(db.studentDao(), db.examRecordDao(), db.questionScoreDao(), db.regionTemplateDao())
    }
    private val ocrEngine by lazy { OcrEngine() }
    private val factory by lazy { AppViewModelFactory(repository, ocrEngine) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val autoDemo = intent?.action == "com.example.marklens.AUTO_DEMO"
        setContent { MarkLensApp(factory, ocrEngine, autoDemo) }
    }

    override fun onDestroy() {
        super.onDestroy()
        ocrEngine.close()
    }
}

sealed class Screen {
    data object Capture : Screen()
    data object RecordList : Screen()
    data class Review(val imageUri: String) : Screen()
    data class Stats(val subject: String?) : Screen()
}

@Composable
private fun MarkLensApp(factory: ViewModelProvider.Factory, ocrEngine: OcrEngine, autoDemo: Boolean = false) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Capture) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val autoTriggered = remember { mutableStateOf(false) }

    val captureVm: CaptureViewModel = viewModel(factory = factory)
    val reviewVm: ReviewViewModel = viewModel(factory = factory)

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri ->
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.let { captureVm.setPhoto(it) }
            }
        }
    }

    // Auto-demo: full-image OCR + keyword matching
    LaunchedEffect(autoDemo) {
        if (autoDemo && !autoTriggered.value) {
            autoTriggered.value = true
            //Log.d("MarkLens", "Auto-demo triggered")
            captureVm.setPhotoFromPath("/data/local/tmp/truely.jpg")
            kotlinx.coroutines.delay(2000)
            val bmp = captureVm.uiState.value.capturedBitmap
            if (bmp != null) {
                //Log.d("MarkLens", "Full OCR on ${bmp.width}x${bmp.height}")
                val fullText = ocrEngine.recognize(bmp)
                val blocks = fullText.textBlocks.map { it.text.trim() }.filter { it.isNotBlank() }
                //Log.d("MarkLens", "Blocks: $blocks")

                // Match labels to values positionally.
                // OCR splits layout: label blocks first, then value blocks.
                // e.g. ["StudentName : Alice Wang", "Stu dent ID :", "Class:", "Subject", "Toto Score",
                //        "2024 001", "Class 3-2", "Mathematics", "40.s|50"]
                var name: String? = null; var studentId: String? = null
                var className: String? = null; var subject: String? = null
                var totalScore: String? = null

                for (line in blocks) {
                    val l = line.lowercase()
                    when {
                        // Name: block has both label and value after ":"
                        (l.contains("name") || l.contains("nome")) && line.contains(":") && line.substringAfter(":").isNotBlank() ->
                            name = line.substringAfter(":").trim()
                        // ID value: looks like a student number (starts with digits, maybe spaces)
                        l.matches(Regex("^\\d[\\d\\s]+$")) && studentId == null ->
                            studentId = line
                        // Class value: contains "class" followed by a class number
                        l.contains("class") && l.any { it.isDigit() } ->
                            className = line
                        // Subject: standalone word, no numbers, not a known label
                        !l.contains("name") && !l.contains("id") && !l.contains("class")
                            && !l.contains("subject") && !l.contains("total") && !l.contains("toto")
                            && !l.contains("score") && !l.any { it.isDigit() } && line.length > 2 ->
                            subject = line
                        // Total score: last block with digits (positional — appears at end)
                        l.any { it.isDigit() } && (l.contains("/") || l.contains("|") || l.contains(".")) ->
                            totalScore = line
                    }
                }
                // Fallbacks: try value after ":" for any colon line
                if (name == null) name = blocks.find { it.lowercase().contains("name") }?.substringAfter(":")?.trim()
                if (studentId == null) studentId = blocks.find { it.lowercase().contains("id") && it.substringAfter(":").isNotBlank() }?.substringAfter(":")?.trim()
                if (className == null) className = blocks.find { it.lowercase().contains("class") && it.substringAfter(":").isNotBlank() }?.substringAfter(":")?.trim()
                // Clean OCR: "40.s|50" → "40.5/50", "2024 001" → "2024001"
                totalScore = totalScore?.replace('|', '/')?.replace('s', '5')?.replace('S', '5')
                studentId = studentId?.replace(" ", "")
                //Log.d("MarkLens", "Parsed: name=$name id=$studentId class=$className subj=$subject total=$totalScore")
                reviewVm.setParsedData(
                    info = ParsedStudentInfo(name, studentId, className),
                    subject = subject ?: "",
                    totalScore = totalScore ?: "",
                    scores = emptyList()
                )
                currentScreen = Screen.Review("auto_demo")
            }
        }
    }

    when (val screen = currentScreen) {
        is Screen.Capture -> {
            CaptureScreen(
                viewModel = captureVm,
                onGalleryClick = { galleryLauncher.launch("image/*") },
                onReviewClick = { bitmap, regions ->
                    scope.launch {
                        val filledRegions = regions.map { region ->
                            if (region.label == RegionLabel.CUSTOM) region
                            else region.copy(rawText = ocrEngine.recognizeRegion(bitmap, region.rect).trim())
                        }
                        val info = StudentInfoParser().parse(filledRegions)
                        val subj = filledRegions.find { it.label == RegionLabel.SUBJECT }?.rawText ?: ""
                        val qs = ScoreParser().parseScores(filledRegions, 0)
                        val total = ScoreParser().parseTotalScore(filledRegions)
                        reviewVm.setParsedData(
                            info, subj, total?.toString() ?: "",
                            qs.map { ScoreField(it.questionNumber, it.score.toString(), it.maxScore) }
                        )
                        currentScreen = Screen.Review("photo_uri")
                    }
                }
            )
        }
        is Screen.Review -> {
            ReviewScreen(viewModel = reviewVm, imageUri = screen.imageUri, onSaved = { currentScreen = Screen.RecordList })
        }
        is Screen.RecordList -> {
            val listVm: RecordListViewModel = viewModel(factory = factory)
            RecordListScreen(viewModel = listVm, onStatsClick = { currentScreen = Screen.Stats(it) })
        }
        is Screen.Stats -> {
            val statsVm: StatsViewModel = viewModel(factory = factory)
            StatsScreen(viewModel = statsVm, onBack = { currentScreen = Screen.RecordList })
        }
    }
}

/** Extract value after ":" separator, or return the whole string. */
private fun valueAfterColon(line: String): String {
    val after = line.substringAfter(":").trim()
    return after.ifBlank { line.trim() }
}
