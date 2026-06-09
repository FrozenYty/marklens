package com.example.marklens.parser

import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel

/**
 * Parses question scores from OCR text.
 * Supports:
 *   1. Multiple QUESTION_SCORE regions (one per question)
 *   2. Single QUESTION_SCORE region containing a table (column-oriented OCR output)
 *
 * @author Tianyu Yao
 */
class ScoreParser {

    fun parseScores(regions: List<OcrRegion>, examRecordId: Long): List<QuestionScore> {
        val scoreRegions = regions.filter { it.label == RegionLabel.QUESTION_SCORE }
        if (scoreRegions.isEmpty()) return emptyList()

        if (scoreRegions.size == 1) {
            val raw = scoreRegions[0].rawText.trim()
            // If it looks like a single score ("8", "8/10", "-2", "??"), treat as single-region mode
            if (raw.matches(Regex("""^-?\d+\.?\d*(/\d+\.?\d*)?$""")) || !raw.any { it.isLetter() }) {
                val (score, maxScore) = parseSingleScore(raw)
                return listOf(QuestionScore(
                    examRecordId = examRecordId, questionNumber = 1,
                    score = score, maxScore = maxScore, isWrong = score < maxScore
                ))
            }
            return parseTableScores(raw, examRecordId)
        }

        return scoreRegions.mapIndexed { idx, region ->
            val (score, maxScore) = parseSingleScore(region.rawText.trim())
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
     * Extract scores from a table-format text block.
     * ML Kit returns column-oriented text like:
     *   "No. 1 2 3 4 5 Question Question 1 ... Score 15 14 15 17 15 Max 20 20 20 20 20 Result Pass..."
     * Strategy: find "Score" keyword, extract numbers after it until next keyword.
     *           find "Max" keyword, extract numbers after it.
     */
    private fun parseTableScores(raw: String, examRecordId: Long): List<QuestionScore> {
        val text = raw.replace(Regex("[\\n\\r]+"), " ")

        // Strategy 1: Column-header based (Score ... Max ...)
        val scoreIdx = text.indexOf("Score", ignoreCase = true)
        val maxIdx = text.indexOf("Max", ignoreCase = true)

        if (scoreIdx >= 0 && maxIdx >= 0 && maxIdx > scoreIdx) {
            val scorePart = text.substring(scoreIdx + 5, maxIdx)
            val maxPart = if (text.indexOf("Result", ignoreCase = true) > maxIdx) {
                text.substring(maxIdx + 3, text.indexOf("Result", ignoreCase = true))
            } else {
                text.substring(maxIdx + 3)
            }

            val scores = Regex("""\d+""").findAll(scorePart).map { it.value.toDouble() }.toList()
            val maxScores = Regex("""\d+""").findAll(maxPart).map { it.value.toDouble() }.toList()

            if (scores.isNotEmpty()) {
                return scores.mapIndexed { idx, score ->
                    val max = maxScores.getOrElse(idx) { 20.0 }
                    QuestionScore(
                        examRecordId = examRecordId,
                        questionNumber = idx + 1,
                        score = score,
                        maxScore = max,
                        isWrong = score < max
                    )
                }
            }
        }

        // Strategy 2: "Question N" pattern — extract score after each Question N
        val qPattern = Regex("""Question\s*(\d+)""", RegexOption.IGNORE_CASE)
        val qMatches = qPattern.findAll(text).toList()
        if (qMatches.isNotEmpty() && scoreIdx >= 0) {
            val numQuestions = qMatches.size
            val scorePart2 = text.substring(scoreIdx + 5)
            val allNums = Regex("""\d+""").findAll(scorePart2).map { it.value.toDouble() }.toList()
            if (allNums.size >= numQuestions) {
                return (0 until numQuestions).map { idx ->
                    val score = allNums[idx]
                    val max = if (allNums.size >= numQuestions * 2) allNums[numQuestions + idx] else 20.0
                    QuestionScore(
                        examRecordId = examRecordId,
                        questionNumber = idx + 1,
                        score = score,
                        maxScore = max,
                        isWrong = score < max
                    )
                }
            }
        }

        // Strategy 3: line-by-line for row-oriented tables
        val lines = raw.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val results = mutableListOf<QuestionScore>()
        for (line in lines) {
            val numbers = Regex("""\d+""").findAll(line).map { it.value.toInt() }.toList()
            if (numbers.size >= 3 && numbers[0] in 1..10) {
                val qNum = numbers[0]
                val candidates = numbers.drop(1).filter { it != qNum }
                if (candidates.size >= 2) {
                    for (i in 0 until candidates.size - 1) {
                        if (candidates[i + 1] >= candidates[i] && candidates[i + 1] in 10..100) {
                            results.add(QuestionScore(
                                examRecordId = examRecordId,
                                questionNumber = qNum,
                                score = candidates[i].toDouble(),
                                maxScore = candidates[i + 1].toDouble(),
                                isWrong = candidates[i] < candidates[i + 1]
                            ))
                            break
                        }
                    }
                }
            }
        }
        return results.sortedBy { it.questionNumber }
    }

    private fun parseSingleScore(raw: String): Pair<Double, Double> {
        if (raw.contains("/")) {
            val parts = raw.split("/")
            val s = parts[0].trim().toDoubleOrNull() ?: 0.0
            val m = parts[1].trim().toDoubleOrNull() ?: 10.0
            return s to m
        }
        val s = raw.toDoubleOrNull() ?: 0.0
        return s to 10.0
    }

    fun parseTotalScore(regions: List<OcrRegion>): Double? {
        val total = regions.find { it.label == RegionLabel.TOTAL_SCORE } ?: return null
        val raw = total.rawText.trim()
        val num = Regex("""(\d+\.?\d*)""").find(raw)?.value
        return num?.toDoubleOrNull()
    }
}
