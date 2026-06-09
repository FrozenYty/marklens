package com.example.marklens.ui.capture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.marklens.data.ExamRepository
import com.example.marklens.data.entity.RegionTemplate
import com.example.marklens.ocr.OcrEngine
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class CaptureUiState(
    val capturedBitmap: Bitmap? = null,
    val regions: List<OcrRegion> = emptyList(),
    val selectedRegionId: String? = null,
    val selectedLabel: RegionLabel? = null,
    val isRecognizing: Boolean = false,
    val templateNames: List<String> = emptyList(),
    val demoData: DemoReviewData? = null
)

/**
 * Pre-filled data for demo mode — bypasses OCR to ensure correct field mapping.
 */
data class DemoReviewData(
    val studentName: String,
    val studentId: String,
    val className: String,
    val subject: String,
    val totalScore: String,
    val scores: List<Pair<String, Double>> // (score, maxScore) pairs
)

/**
 * ViewModel for the capture screen — manages photo, region CRUD,
 * and template save/load.
 *
 * @author Tianyu Yao
 */
class CaptureViewModel(
    private val repository: ExamRepository? = null,
    private val ocrEngine: OcrEngine? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun setPhoto(bitmap: Bitmap) {
        _uiState.update { it.copy(capturedBitmap = bitmap) }
    }

    /**
     * Loads a photo from a file path and auto-creates regions with correct labels
     * at common exam-paper positions. User can adjust positions before OCR.
     */
    fun setPhotoFromPath(path: String) {
        val bmp = BitmapFactory.decodeFile(path)
        if (bmp != null) {
            // No auto-regions — auto-demo uses full-image OCR instead
            _uiState.update { it.copy(capturedBitmap = bmp, regions = emptyList()) }
        }
    }

    /**
     * Creates a demo exam-paper bitmap for testing/screenshots.
     * Draws a paper-like canvas with labeled sections.
     */
    fun loadDemoBitmap() {
        val w = 800
        val h = 600
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bmp)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            textSize = 28f
        }

        // Paper background
        canvas.drawColor(android.graphics.Color.rgb(252, 250, 245))

        // Header - student info area
        paint.color = android.graphics.Color.rgb(60, 60, 80)
        paint.textSize = 30f
        paint.isFakeBoldText = true
        canvas.drawText("Student Name:  Alice Wang", 40f, 60f, paint)
        canvas.drawText("Student ID:    2024001", 40f, 110f, paint)
        canvas.drawText("Class:         Class 3-2", 40f, 160f, paint)

        // Subject
        paint.textSize = 32f
        canvas.drawText("Subject:  Mathematics", 40f, 220f, paint)

        // Separator line
        paint.color = android.graphics.Color.rgb(180, 180, 180)
        paint.strokeWidth = 2f
        canvas.drawLine(30f, 240f, 770f, 240f, paint)
        paint.strokeWidth = 1f

        // Question scores
        paint.color = android.graphics.Color.rgb(40, 40, 60)
        paint.textSize = 26f
        paint.isFakeBoldText = false
        val questions = listOf(
            "Q1:  8 / 10",
            "Q2:  7.5 / 10",
            "Q3:  9 / 10",
            "Q4:  6 / 10",
            "Q5:  10 / 10"
        )
        var y = 290f
        for (q in questions) {
            canvas.drawText(q, 60f, y, paint)
            y += 45f
        }

        // Total score
        paint.textSize = 30f
        paint.isFakeBoldText = true
        paint.color = android.graphics.Color.rgb(200, 40, 40)
        canvas.drawText("Total Score:  40.5 / 50", 40f, y + 30f, paint)

        _uiState.update {
            it.copy(
                capturedBitmap = bmp,
                demoData = DemoReviewData(
                    studentName = "Alice Wang",
                    studentId = "2024001",
                    className = "Class 3-2",
                    subject = "Mathematics",
                    totalScore = "40.5",
                    scores = listOf(
                        "8" to 10.0,
                        "7.5" to 10.0,
                        "9" to 10.0,
                        "6" to 10.0,
                        "10" to 10.0
                    )
                )
            )
        }
    }

    fun addRegion(region: OcrRegion) {
        _uiState.update { it.copy(regions = it.regions + region, selectedRegionId = region.id) }
    }

    fun deleteRegion(regionId: String) {
        _uiState.update { it.copy(regions = it.regions.filter { r -> r.id != regionId }) }
    }

    fun moveRegion(regionId: String, newRect: RectF) {
        _uiState.update { state ->
            state.copy(regions = state.regions.map { r ->
                if (r.id == regionId) r.copy(rect = newRect) else r
            })
        }
    }

    fun changeRegionLabel(regionId: String, newLabel: RegionLabel) {
        _uiState.update { state ->
            state.copy(regions = state.regions.map { r ->
                if (r.id == regionId) r.copy(label = newLabel) else r
            })
        }
    }

    fun selectRegion(regionId: String?) {
        _uiState.update { it.copy(selectedRegionId = regionId) }
    }

    fun clearRegions() {
        _uiState.update { it.copy(regions = emptyList()) }
    }

    // ── Template Save / Load ──

    /**
     * Saves the current region configuration as a named template.
     */
    fun saveTemplate(name: String) {
        val repo = repository ?: return
        val json = regionsToJson(_uiState.value.regions)
        viewModelScope.launch {
            repo.saveTemplate(name, json)
            refreshTemplateNames(repo)
        }
    }

    /**
     * Refreshes the list of available template names from the database.
     */
    fun refreshTemplates() {
        val repo = repository ?: return
        viewModelScope.launch {
            refreshTemplateNames(repo)
        }
    }

    private suspend fun refreshTemplateNames(repo: ExamRepository) {
        repo.getAllTemplates().collect { templates ->
            _uiState.update {
                it.copy(templateNames = templates.map { t -> t.name })
            }
            return@collect // only first emission
        }
    }

    /**
     * Loads template regions by template name — replaces the current region list.
     */
    fun applyTemplate(templateName: String) {
        val repo = repository ?: return
        viewModelScope.launch {
            repo.getAllTemplates().collect { templates ->
                val template = templates.find { it.name == templateName }
                if (template != null) {
                    val regions = jsonToRegions(template.regionsJson)
                    _uiState.update { it.copy(regions = regions) }
                }
                return@collect
            }
        }
    }

    // ── JSON Serialization ──

    private fun regionsToJson(regions: List<OcrRegion>): String {
        val arr = JSONArray()
        for (r in regions) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("label", r.label.name)
            obj.put("left", r.rect.left.toDouble())
            obj.put("top", r.rect.top.toDouble())
            obj.put("right", r.rect.right.toDouble())
            obj.put("bottom", r.rect.bottom.toDouble())
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun jsonToRegions(json: String): List<OcrRegion> {
        val arr = JSONArray(json)
        val regions = mutableListOf<OcrRegion>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val label = try {
                RegionLabel.valueOf(obj.getString("label"))
            } catch (_: Exception) {
                RegionLabel.CUSTOM
            }
            regions.add(
                OcrRegion(
                    id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                    label = label,
                    rect = RectF(
                        obj.getDouble("left").toFloat(),
                        obj.getDouble("top").toFloat(),
                        obj.getDouble("right").toFloat(),
                        obj.getDouble("bottom").toFloat()
                    )
                )
            )
        }
        return regions
    }

    override fun onCleared() {
        super.onCleared()
        _uiState.value.capturedBitmap?.recycle()
    }
}
