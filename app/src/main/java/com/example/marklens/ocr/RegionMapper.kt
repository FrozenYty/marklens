package com.example.marklens.ocr

import android.graphics.RectF

/**
 * Simplified text block from OCR output.
 * Decouples RegionMapper from ML Kit's Text.TextBlock for testability.
 */
data class TextBlock(
    val text: String,
    val boundingBox: RectF
)

/**
 * Maps OCR text blocks to user-defined regions by spatial intersection.
 * A block is assigned to the first region whose rect overlaps the block's bounding box.
 */
class RegionMapper {

    /**
     * For each region, collect all OCR text blocks whose bounding boxes
     * intersect the region's rect. Concatenate with space.
     */
    fun mapBlocksToRegions(blocks: List<TextBlock>, regions: List<OcrRegion>): List<OcrRegion> {
        if (blocks.isEmpty()) return regions.map { it.copy(rawText = "") }
        return regions.map { region ->
            val matched = blocks
                .filter { block -> intersects(region.rect, block.boundingBox) }
                .joinToString(" ") { it.text }
            region.copy(rawText = matched)
        }
    }

    /**
     * Check if two rectangles overlap (intersect).
     */
    private fun intersects(a: RectF, b: RectF): Boolean {
        return a.left < b.right && a.right > b.left && a.top < b.bottom && a.bottom > b.top
    }
}
