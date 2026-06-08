package com.example.marklens.ui

import android.graphics.Bitmap
import android.graphics.RectF
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel
import com.example.marklens.ui.capture.CaptureViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CaptureViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CaptureViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_shouldHaveNoPhotoAndNoRegions() = runTest {
        val state = viewModel.uiState.value
        assertNull(state.capturedBitmap)
        assertTrue(state.regions.isEmpty())
    }

    @Test
    fun setPhoto_shouldUpdateBitmap() = runTest {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        viewModel.setPhoto(bitmap)
        val state = viewModel.uiState.value
        assertNotNull(state.capturedBitmap)
    }

    @Test
    fun addRegion_shouldAppendToList() = runTest {
        viewModel.setPhoto(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        val region = OcrRegion(label = RegionLabel.STUDENT_NAME, rect = RectF(0.1f, 0.1f, 0.5f, 0.2f))
        viewModel.addRegion(region)
        val state = viewModel.uiState.value
        assertEquals(1, state.regions.size)
        assertEquals(RegionLabel.STUDENT_NAME, state.regions[0].label)
    }

    @Test
    fun deleteRegion_shouldRemoveIt() = runTest {
        viewModel.setPhoto(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        val region = OcrRegion(label = RegionLabel.TOTAL_SCORE, rect = RectF(0.1f, 0.1f, 0.5f, 0.2f))
        viewModel.addRegion(region)
        viewModel.deleteRegion(region.id)
        assertEquals(0, viewModel.uiState.value.regions.size)
    }

    @Test
    fun moveRegion_shouldUpdateRect() = runTest {
        viewModel.setPhoto(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        val region = OcrRegion(label = RegionLabel.SUBJECT, rect = RectF(0.1f, 0.1f, 0.3f, 0.2f))
        viewModel.addRegion(region)
        val newRect = RectF(0.2f, 0.2f, 0.5f, 0.4f)
        viewModel.moveRegion(region.id, newRect)
        assertEquals(newRect, viewModel.uiState.value.regions[0].rect)
    }

    @Test
    fun changeLabel_shouldUpdateRegionLabel() = runTest {
        viewModel.setPhoto(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        val region = OcrRegion(label = RegionLabel.CUSTOM, rect = RectF(0.1f, 0.1f, 0.3f, 0.2f))
        viewModel.addRegion(region)
        viewModel.changeRegionLabel(region.id, RegionLabel.TOTAL_SCORE)
        assertEquals(RegionLabel.TOTAL_SCORE, viewModel.uiState.value.regions[0].label)
    }

    @Test
    fun clearRegions_shouldRemoveAll() = runTest {
        viewModel.setPhoto(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888))
        viewModel.addRegion(OcrRegion(label = RegionLabel.CUSTOM, rect = RectF(0f, 0f, 0.1f, 0.1f)))
        viewModel.addRegion(OcrRegion(label = RegionLabel.CUSTOM, rect = RectF(0.1f, 0.1f, 0.2f, 0.2f)))
        viewModel.clearRegions()
        assertEquals(0, viewModel.uiState.value.regions.size)
    }
}
