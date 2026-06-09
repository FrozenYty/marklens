package com.example.marklens

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.marklens.data.ExamRepository
import com.example.marklens.data.MarkLensDatabase
import com.example.marklens.data.entity.RegionTemplate
import com.example.marklens.di.AppViewModelFactory
import com.example.marklens.ocr.MlKitOcrProvider
import com.example.marklens.ocr.OcrProvider
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel
import com.example.marklens.ocr.RegionMapper
import com.example.marklens.parser.ParsedStudentInfo
import com.example.marklens.parser.ScoreParser
import com.example.marklens.parser.StudentInfoParser
import com.example.marklens.ui.editor.TemplateEditorScreen
import com.example.marklens.ui.list.RecordListScreen
import com.example.marklens.ui.list.RecordListViewModel
import com.example.marklens.ui.review.ReviewScreen
import com.example.marklens.ui.review.ReviewViewModel
import com.example.marklens.ui.review.ScoreField
import com.example.marklens.ui.stats.StatsScreen
import com.example.marklens.ui.stats.StatsViewModel
import com.example.marklens.ui.templates.TemplateListScreen
import com.example.marklens.ui.theme.*
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Main entry point — template-first architecture.
 *
 * Navigation:
 *   Home → Scan Paper (pick template → photo → OCR → Records)
 *   Home → Templates (create / edit / delete)
 *   Home → Records (view / review records)
 *   Templates → Editor (create or edit template regions)
 *
 * @author Tianyu Yao
 */
class MainActivity : ComponentActivity() {

    private val db: MarkLensDatabase by lazy {
        Room.databaseBuilder(applicationContext, MarkLensDatabase::class.java, "marklens.db")
            .build()
    }
    private val repository by lazy {
        ExamRepository(db.studentDao(), db.examRecordDao(), db.questionScoreDao(), db.regionTemplateDao())
    }
    private val ocrProvider: OcrProvider by lazy { MlKitOcrProvider() }
    private val factory by lazy { AppViewModelFactory(repository, null) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MarkLensApp(factory, ocrProvider, repository, db) }
    }

    override fun onDestroy() {
        super.onDestroy(); ocrProvider.close()
    }
}

// ── Navigation with back stack ──

sealed class Nav {
    data object Home : Nav()
    data object Editor : Nav()
    data object Templates : Nav()
    data object Records : Nav()
    data class Review(val imageUri: String) : Nav()
    data class Stats(val subject: String?) : Nav()
}

class EditorState {
    var bitmap by mutableStateOf<android.graphics.Bitmap?>(null)
    var regions by mutableStateOf<List<OcrRegion>>(emptyList())
    var selectedId by mutableStateOf<String?>(null)
    var editingTemplateId by mutableStateOf<Long?>(null)
    var editingTemplateName by mutableStateOf("")
    // Proper undo/redo with dual stacks
    var undoStack by mutableStateOf<List<EditorAction>>(emptyList())
    var redoStack by mutableStateOf<List<EditorAction>>(emptyList())
}

sealed class EditorAction {
    data class Add(val region: OcrRegion, val index: Int) : EditorAction()
    data class Delete(val region: OcrRegion, val index: Int) : EditorAction()
}

@Composable
private fun MarkLensApp(factory: ViewModelProvider.Factory, ocr: OcrProvider, repo: ExamRepository, db: MarkLensDatabase) {
    var navStack by remember { mutableStateOf<List<Nav>>(listOf(Nav.Home)) }
    val nav = navStack.last()
    fun push(s: Nav) { navStack = navStack + s }
    fun pop() { if (navStack.size > 1) navStack = navStack.dropLast(1) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Template list state ──
    var templates by remember { mutableStateOf<List<RegionTemplate>>(emptyList()) }
    LaunchedEffect(Unit) {
        repo.getAllTemplates().collect { templates = it }
    }

    // ── Editor state ──
    val editor = remember { EditorState() }

    // ── Scan Paper: template picker state ──
    var showScanPicker by remember { mutableStateOf(false) }
    var pendingScanTemplate by remember { mutableStateOf<RegionTemplate?>(null) }
    var isScanFlow by remember { mutableStateOf(false) }  // true when OCR → Review → Records

    // ── VMs ──
    val reviewVm: ReviewViewModel = viewModel(factory = factory)

    // Shared OCR logic for Scan Paper flow — full-page OCR + block mapping
    fun runScanOcr(bmp: android.graphics.Bitmap, template: RegionTemplate, imageUri: String) {
        scope.launch {
            val regs = jsonToRegions(template.regionsJson)
            // OCR the full page once, then map text blocks to regions by spatial overlap
            val blocks = ocr.recognizeBlocks(bmp)
            val mapper = RegionMapper()
            val filled = mapper.mapBlocksToRegions(blocks, regs)
            val info = StudentInfoParser().parse(filled)
            val subj = StudentInfoParser.cleanLabel(filled.find { it.label == RegionLabel.SUBJECT }?.rawText) ?: ""
            val qs = ScoreParser().parseScores(filled, 0)
            val total = ScoreParser().parseTotalScore(filled)
            reviewVm.setParsedData(info, subj, total?.toString() ?: "",
                qs.map { ScoreField(it.questionNumber, it.score.toString(), it.maxScore) })
            isScanFlow = true
            push(Nav.Review(imageUri))
        }
    }

    // ── Gallery launcher ──
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { stream ->
                BitmapFactory.decodeStream(stream)?.let { bmp ->
                    val scanTemplate = pendingScanTemplate
                    pendingScanTemplate = null
                    if (scanTemplate != null) {
                        runScanOcr(bmp, scanTemplate, uri.toString())
                    } else {
                        // Editor flow: just set the photo
                        editor.bitmap = bmp; editor.regions = emptyList()
                    }
                }
            }
        }
    }
    val pickPhoto: () -> Unit = { galleryLauncher.launch("image/*") }

    // ── System back → pop navigation stack ──
    androidx.activity.compose.BackHandler(enabled = navStack.size > 1) { pop() }

    when (nav) {
        is Nav.Home -> {
            Column(Modifier.fillMaxSize().background(PaperCream).statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text("MarkLens", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                Text("Exam paper digitization", fontSize = 14.sp, color = InkMuted)
                Spacer(Modifier.height(40.dp))

                // Scan Paper: pick template → photo → OCR
                Button(onClick = {
                    if (templates.isEmpty()) {
                        showScanPicker = true // triggers "no templates" dialog below
                    } else if (templates.size == 1) {
                        pendingScanTemplate = templates.first()
                        galleryLauncher.launch("image/*")
                    } else {
                        showScanPicker = true
                    }
                }, Modifier.width(240.dp).height(52.dp), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GradeRed, contentColor = CardWhite)) {
                    Text("📷  Scan Paper", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(14.dp))
                Button(onClick = { push(Nav.Templates) }, Modifier.width(240.dp).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardWhite, contentColor = InkPrimary)) {
                    Text("📋  Templates", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(14.dp))
                Button(onClick = { push(Nav.Records) }, Modifier.width(240.dp).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrassBg, contentColor = BrassGold)) {
                    Text("📊  Records", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            // ── Scan template picker dialog ──
            if (showScanPicker) {
                if (templates.isEmpty()) {
                    AlertDialog(onDismissRequest = { showScanPicker = false },
                        title = { Text("No templates", color = InkPrimary) },
                        text = { Text("Create a template first to scan papers with it.", color = InkMuted) },
                        confirmButton = {
                            Button(onClick = { showScanPicker = false; push(Nav.Templates) },
                                colors = ButtonDefaults.buttonColors(containerColor = StampTeal),
                                shape = RoundedCornerShape(10.dp)) { Text("Go to Templates") }
                        },
                        dismissButton = {
                            Button(onClick = { showScanPicker = false },
                                shape = RoundedCornerShape(10.dp)) { Text("Cancel") }
                        })
                } else {
                    AlertDialog(onDismissRequest = { showScanPicker = false },
                        title = { Text("Choose template", color = InkPrimary) },
                        text = {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                templates.forEach { t ->
                                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            showScanPicker = false
                                            pendingScanTemplate = t
                                            galleryLauncher.launch("image/*")
                                        }
                                        .padding(12.dp)) {
                                        Text(t.name, fontSize = 15.sp, color = InkPrimary, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            Button(onClick = { showScanPicker = false },
                                shape = RoundedCornerShape(10.dp)) { Text("Cancel") }
                        })
                }
            }
        }

        is Nav.Editor -> TemplateEditorScreen(
            bitmap = editor.bitmap, regions = editor.regions, selectedId = editor.selectedId,
            editingTemplateId = editor.editingTemplateId,
            existingTemplateName = editor.editingTemplateName,
            onPhotoRequested = pickPhoto,
            onRegionCreated = { rect ->
                val r = OcrRegion(id = UUID.randomUUID().toString(), label = RegionLabel.CUSTOM, rect = rect)
                val idx = editor.regions.size
                editor.undoStack = editor.undoStack + EditorAction.Add(r, idx)
                editor.redoStack = emptyList()
                editor.regions = editor.regions + r; editor.selectedId = r.id
            },
            onRegionSelected = { id -> editor.selectedId = id },
            onRegionDeleted = { id ->
                val deleted = editor.regions.find { it.id == id }
                if (deleted != null) {
                    val idx = editor.regions.indexOf(deleted)
                    editor.undoStack = editor.undoStack + EditorAction.Delete(deleted, idx)
                    editor.redoStack = emptyList()
                }
                editor.regions = editor.regions.filter { it.id != id }; editor.selectedId = null
            },
            onChangeLabel = { id, label -> editor.regions = editor.regions.map { if (it.id == id) it.copy(label = label) else it } },
            onSaveTemplate = { name ->
                val existingId = editor.editingTemplateId
                // Synchronous duplicate check
                val isDuplicate = if (existingId != null)
                    templates.any { it.name == name && it.id != existingId }
                else
                    templates.any { it.name == name }
                if (isDuplicate) {
                    false
                } else {
                    val json = org.json.JSONArray().apply { editor.regions.forEach { r ->
                        put(org.json.JSONObject().apply {
                            put("id", r.id); put("label", r.label.name)
                            put("left", r.rect.left.toDouble()); put("top", r.rect.top.toDouble())
                            put("right", r.rect.right.toDouble()); put("bottom", r.rect.bottom.toDouble())
                        })
                    }}.toString()
                    scope.launch {
                        if (existingId != null) repo.updateTemplate(existingId, name, json)
                        else repo.saveTemplate(name, json)
                        editor.editingTemplateId = null
                    }
                    pop()
                    true
                }
            },
            onUndo = {
                val stack = editor.undoStack
                if (stack.isNotEmpty()) {
                    val action = stack.last()
                    editor.undoStack = stack.dropLast(1)
                    when (action) {
                        is EditorAction.Add -> {
                            // Undo an add → remove the region
                            editor.regions = editor.regions.filter { it.id != action.region.id }
                            editor.redoStack = editor.redoStack + action
                        }
                        is EditorAction.Delete -> {
                            // Undo a delete → restore the region at original position
                            val mutable = editor.regions.toMutableList()
                            mutable.add(action.index.coerceIn(0, mutable.size), action.region)
                            editor.regions = mutable
                            editor.redoStack = editor.redoStack + action
                        }
                    }
                }
            },
            onRedo = {
                val stack = editor.redoStack
                if (stack.isNotEmpty()) {
                    val action = stack.last()
                    editor.redoStack = stack.dropLast(1)
                    when (action) {
                        is EditorAction.Add -> {
                            // Redo an add → put the region back
                            val mutable = editor.regions.toMutableList()
                            mutable.add(action.index.coerceIn(0, mutable.size), action.region)
                            editor.regions = mutable
                            editor.undoStack = editor.undoStack + action
                        }
                        is EditorAction.Delete -> {
                            // Redo a delete → remove the region again
                            editor.regions = editor.regions.filter { it.id != action.region.id }
                            editor.undoStack = editor.undoStack + action
                        }
                    }
                }
            },
            canUndo = editor.undoStack.isNotEmpty(),
            canRedo = editor.redoStack.isNotEmpty(),
            onBack = { pop() }
        )

        is Nav.Templates -> TemplateListScreen(
            templates = templates,
            onNewTemplate = {
                editor.bitmap = null; editor.regions = emptyList()
                editor.selectedId = null; editor.undoStack = emptyList(); editor.redoStack = emptyList()
                editor.editingTemplateId = null; editor.editingTemplateName = ""
                push(Nav.Editor)
            },
            onEditTemplate = { t ->
                editor.bitmap = null; editor.regions = jsonToRegions(t.regionsJson)
                editor.selectedId = null; editor.undoStack = emptyList(); editor.redoStack = emptyList()
                editor.editingTemplateId = t.id; editor.editingTemplateName = t.name
                push(Nav.Editor)
            },
            onDeleteTemplate = { scope.launch { repo.deleteTemplate(it) } },
            onBack = { pop() }
        )

        is Nav.Records -> {
            val listVm: RecordListViewModel = viewModel(factory = factory)
            RecordListScreen(viewModel = listVm, repository = repo,
                onRecordClick = { recordId ->
                    scope.launch {
                        repo.getRecordWithScores(recordId)?.let { (record, scores) ->
                            val student = db.studentDao().getById(record.studentId)
                            reviewVm.setParsedData(
                                info = ParsedStudentInfo(student?.name, student?.studentId, student?.className),
                                subject = record.subject,
                                totalScore = record.totalScore.toString(),
                                scores = scores.map { ScoreField(it.questionNumber, it.score.toString(), it.maxScore) }
                            )
                            push(Nav.Review("record_$recordId"))
                        }
                    }
                },
                onStatsClick = { push(Nav.Stats(it)) },
                onBack = { pop() })
        }

        is Nav.Review -> ReviewScreen(viewModel = reviewVm, imageUri = nav.imageUri,
            onSaved = {
                if (isScanFlow) {
                    isScanFlow = false
                    // Replace Review with Records in nav stack
                    navStack = navStack.dropLast(1) + Nav.Records
                } else {
                    pop()
                }
            },
            onCancel = { isScanFlow = false; pop() })

        is Nav.Stats -> {
            val subj = nav.subject
            val statsVm = remember(subj) { StatsViewModel(repo, subj) }
            StatsScreen(viewModel = statsVm, repository = repo, onBack = { pop() })
        }
    }
}

private fun jsonToRegions(json: String): List<OcrRegion> {
    val arr = org.json.JSONArray(json)
    return (0 until arr.length()).map { i ->
        val obj = arr.getJSONObject(i)
        OcrRegion(
            id = obj.optString("id", UUID.randomUUID().toString()),
            label = try { RegionLabel.valueOf(obj.getString("label")) } catch (_: Exception) { RegionLabel.CUSTOM },
            rect = android.graphics.RectF(
                obj.getDouble("left").toFloat(), obj.getDouble("top").toFloat(),
                obj.getDouble("right").toFloat(), obj.getDouble("bottom").toFloat()
            )
        )
    }
}

