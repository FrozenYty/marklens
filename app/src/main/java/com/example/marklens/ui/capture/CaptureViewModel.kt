package com.example.marklens.ui.capture

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.ViewModel
import com.example.marklens.data.ExamRepository
import com.example.marklens.ocr.OcrEngine
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CaptureUiState(
    val capturedBitmap: Bitmap? = null,
    val regions: List<OcrRegion> = emptyList(),
    val selectedRegionId: String? = null,
    val selectedLabel: RegionLabel? = null,
    val isRecognizing: Boolean = false,
    val templateNames: List<String> = emptyList()
)

class CaptureViewModel(
    private val repository: ExamRepository? = null,
    private val ocrEngine: OcrEngine? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaptureUiState())
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    fun setPhoto(bitmap: Bitmap) {
        _uiState.update { it.copy(capturedBitmap = bitmap) }
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

    override fun onCleared() {
        super.onCleared()
        _uiState.value.capturedBitmap?.recycle()
    }
}
