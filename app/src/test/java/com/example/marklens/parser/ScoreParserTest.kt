package com.example.marklens.parser

import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel
import android.graphics.RectF
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.robolectric.RobolectricTestRunner

@org.junit.runner.RunWith(RobolectricTestRunner::class)
class ScoreParserTest {

    private val parser = ScoreParser()

    @Test
    fun parse_emptyRegions_shouldReturnEmpty() {
        assertTrue(parser.parseScores(emptyList(), 1L).isEmpty())
    }

    @Test
    fun parse_singleScore_shouldExtractNumber() {
        val regions = listOf(
            scoreRegion("10")
        )
        val scores = parser.parseScores(regions, 1L)
        assertEquals(1, scores.size)
        assertEquals(10.0, scores[0].score, 0.01)
    }

    @Test
    fun parse_multipleScores_shouldNumberSequentially() {
        val regions = listOf(
            scoreRegion("8"),
            scoreRegion("7.5"),
            scoreRegion("9")
        )
        val scores = parser.parseScores(regions, 5L)
        assertEquals(3, scores.size)
        assertEquals(1, scores[0].questionNumber)
        assertEquals(8.0, scores[0].score, 0.01)
        assertEquals(2, scores[1].questionNumber)
        assertEquals(7.5, scores[1].score, 0.01)
        assertEquals(3, scores[2].questionNumber)
    }

    @Test
    fun parse_nonNumeric_shouldReturnZero() {
        val regions = listOf(scoreRegion("??"))
        val scores = parser.parseScores(regions, 1L)
        assertEquals(0.0, scores[0].score, 0.01)
    }

    @Test
    fun parse_negativeScore_shouldExtract() {
        val regions = listOf(scoreRegion("-2"))
        val scores = parser.parseScores(regions, 1L)
        assertEquals(-2.0, scores[0].score, 0.01)
    }

    @Test
    fun parse_withMaxScore_shouldSetDefaults() {
        val regions = listOf(
            OcrRegion(
                label = RegionLabel.QUESTION_SCORE,
                rect = RectF(0f, 0f, 0.1f, 0.1f),
                rawText = "8/10"
            )
        )
        val scores = parser.parseScores(regions, 1L)
        assertEquals(1, scores.size)
        assertEquals(8.0, scores[0].score, 0.01)
        assertEquals(10.0, scores[0].maxScore, 0.01)
        assertTrue(scores[0].isWrong)
    }

    @Test
    fun parse_fullMarks_shouldNotBeWrong() {
        val regions = listOf(
            OcrRegion(
                label = RegionLabel.QUESTION_SCORE,
                rect = RectF(0f, 0f, 0.1f, 0.1f),
                rawText = "10/10"
            )
        )
        val scores = parser.parseScores(regions, 1L)
        assertTrue(!scores[0].isWrong)
    }

    @Test
    fun parseTotalScore_shouldExtract() {
        val regions = listOf(
            OcrRegion(label = RegionLabel.TOTAL_SCORE, rect = RectF(0f, 0f, 0.1f, 0.1f), rawText = "85")
        )
        assertEquals(85.0, parser.parseTotalScore(regions)!!, 0.01)
    }

    @Test
    fun parseTotalScore_missing_shouldReturnNull() {
        val regions = listOf<OcrRegion>()
        assertEquals(null, parser.parseTotalScore(regions))
    }

    private fun scoreRegion(text: String) = OcrRegion(
        label = RegionLabel.QUESTION_SCORE,
        rect = RectF(0f, 0f, 0.1f, 0.1f),
        rawText = text
    )
}
