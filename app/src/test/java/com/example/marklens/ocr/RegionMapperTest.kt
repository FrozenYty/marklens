package com.example.marklens.ocr

import android.graphics.RectF
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class RegionMapperTest {

    private val mapper = RegionMapper()

    @Test
    fun map_emptyRegions_shouldReturnEmpty() {
        val result = mapper.mapBlocksToRegions(emptyTextBlocks(), emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun map_emptyBlocks_shouldReturnRegionsWithEmptyText() {
        val regions = listOf(
            OcrRegion(label = RegionLabel.STUDENT_NAME, rect = RectF(0.1f, 0.1f, 0.4f, 0.15f))
        )
        val result = mapper.mapBlocksToRegions(emptyTextBlocks(), regions)
        assertEquals(1, result.size)
        assertEquals("", result[0].rawText)
    }

    @Test
    fun map_blockInsideRegion_shouldAssignText() {
        val region = OcrRegion(label = RegionLabel.STUDENT_NAME, rect = RectF(0.1f, 0.1f, 0.5f, 0.2f))
        val blocks = listOf(
            TextBlock("Alice", RectF(0.15f, 0.12f, 0.35f, 0.18f))
        )
        val result = mapper.mapBlocksToRegions(blocks, listOf(region))
        assertEquals("Alice", result[0].rawText)
    }

    @Test
    fun map_blocksOutsideRegion_shouldNotAssign() {
        val region = OcrRegion(label = RegionLabel.STUDENT_NAME, rect = RectF(0.1f, 0.1f, 0.5f, 0.2f))
        val blocks = listOf(
            TextBlock("Outside", RectF(0.6f, 0.6f, 0.9f, 0.7f))
        )
        val result = mapper.mapBlocksToRegions(blocks, listOf(region))
        assertEquals("", result[0].rawText)
    }

    @Test
    fun map_multipleBlocksOneRegion_shouldConcatenate() {
        val region = OcrRegion(label = RegionLabel.STUDENT_NAME, rect = RectF(0.1f, 0.1f, 0.5f, 0.2f))
        val blocks = listOf(
            TextBlock("Alice", RectF(0.12f, 0.12f, 0.25f, 0.18f)),
            TextBlock("Wang", RectF(0.28f, 0.12f, 0.42f, 0.18f))
        )
        val result = mapper.mapBlocksToRegions(blocks, listOf(region))
        assertEquals("Alice Wang", result[0].rawText)
    }

    @Test
    fun map_multipleRegions_shouldAssignIndependently() {
        val nameRegion = OcrRegion("r1", RegionLabel.STUDENT_NAME, RectF(0.1f, 0.1f, 0.5f, 0.2f))
        val idRegion = OcrRegion("r2", RegionLabel.STUDENT_ID, RectF(0.1f, 0.25f, 0.5f, 0.35f))
        val blocks = listOf(
            TextBlock("Alice", RectF(0.12f, 0.12f, 0.35f, 0.18f)),
            TextBlock("2024001", RectF(0.15f, 0.26f, 0.40f, 0.33f))
        )
        val result = mapper.mapBlocksToRegions(blocks, listOf(nameRegion, idRegion))
        assertEquals("Alice", result[0].rawText)
        assertEquals("2024001", result[1].rawText)
    }

    @Test
    fun map_overlappingBlock_shouldAssignToFirstMatchingRegion() {
        val r1 = OcrRegion("r1", RegionLabel.CUSTOM, RectF(0.1f, 0.1f, 0.5f, 0.3f))
        val r2 = OcrRegion("r2", RegionLabel.CUSTOM, RectF(0.1f, 0.1f, 0.5f, 0.3f))
        val blocks = listOf(TextBlock("X", RectF(0.2f, 0.15f, 0.3f, 0.25f)))
        val result = mapper.mapBlocksToRegions(blocks, listOf(r1, r2))
        assertEquals("X", result[0].rawText)
        assertEquals("", result[1].rawText)
    }

    private fun emptyTextBlocks() = emptyList<TextBlock>()
}
