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
 * First-match-wins: a block is assigned to the first region whose rect
 * overlaps it, and is excluded from subsequent regions.
 */
class RegionMapper {

    fun mapBlocksToRegions(blocks: List<TextBlock>, regions: List<OcrRegion>): List<OcrRegion> {
        if (blocks.isEmpty()) return regions.map { it.copy(rawText = "") }
        val assigned = BooleanArray(blocks.size)
        return regions.map { region ->
            val sb = StringBuilder()
            for (i in blocks.indices) {
                if (!assigned[i] && intersects(region.rect, blocks[i].boundingBox)) {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append(blocks[i].text)
                    assigned[i] = true
                }
            }
            region.copy(rawText = sb.toString())
        }
    }

    private fun intersects(a: RectF, b: RectF): Boolean {
        return a.left < b.right && a.right > b.left && a.top < b.bottom && a.bottom > b.top
    }
}
