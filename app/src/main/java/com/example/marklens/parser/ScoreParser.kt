package com.example.marklens.parser

import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel

class ScoreParser {

    /**
     * Parse QUESTION_SCORE regions into QuestionScore list.
     * Expected format: "8" or "8/10".
     * Non-numeric text → score 0.0.
     * Numbered sequentially starting from 1.
     */
    fun parseScores(regions: List<OcrRegion>, examRecordId: Long): List<QuestionScore> {
        val scoreRegions = regions.filter { it.label == RegionLabel.QUESTION_SCORE }
        return scoreRegions.mapIndexed { idx, region ->
            val (score, maxScore) = parseScore(region.rawText.trim())
            QuestionScore(
                examRecordId = examRecordId,
                questionNumber = idx + 1,
                score = score,
                maxScore = maxScore,
                isWrong = score < maxScore
            )
        }
    }

    /**
     * Extract a score from raw text. Supports:
     * - "8" → (8.0, 10.0)
     * - "8/10" → (8.0, 10.0)
     * - "??" → (0.0, 10.0)
     * - "-2" → (-2.0, 10.0)
     */
    private fun parseScore(raw: String): Pair<Double, Double> {
        if (raw.contains("/")) {
            val parts = raw.split("/")
            val s = parts[0].trim().toDoubleOrNull() ?: 0.0
            val m = parts[1].trim().toDoubleOrNull() ?: 10.0
            return s to m
        }
        val s = raw.toDoubleOrNull() ?: 0.0
        return s to 10.0
    }

    /**
     * Parse TOTAL_SCORE region.
     */
    fun parseTotalScore(regions: List<OcrRegion>): Double? {
        val total = regions.find { it.label == RegionLabel.TOTAL_SCORE } ?: return null
        return total.rawText.trim().toDoubleOrNull()
    }
}
