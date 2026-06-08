package com.example.marklens.util

import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class StatsCalculatorTest {

    private val calc = StatsCalculator()

    @Test
    fun calculate_empty_shouldReturnZeros() {
        val result = calc.calculate(emptyList(), emptyMap())
        assertEquals(0.0, result.averageScore, 0.01)
        assertEquals(0.0, result.maxScore, 0.01)
        assertEquals(0.0, result.passRate, 0.01)
    }

    @Test
    fun calculate_singleRecord_shouldReturnExactValues() {
        val records = listOf(record(1, "Math", 85.0))
        val scores = mapOf(1L to listOf(
            score(1, 1, 8.0, 10.0),
            score(1, 2, 9.0, 10.0)
        ))
        val result = calc.calculate(records, scores)
        assertEquals(85.0, result.averageScore, 0.01)
        assertEquals(85.0, result.maxScore, 0.01)
    }

    @Test
    fun calculate_multipleRecords_shouldComputeCorrectly() {
        val records = listOf(
            record(1, "Math", 90.0),
            record(2, "Math", 60.0),
            record(3, "Math", 80.0)
        )
        val scores = mapOf(
            1L to listOf(score(1, 1, 9.0, 10.0)),
            2L to listOf(score(2, 1, 6.0, 10.0)),
            3L to listOf(score(3, 1, 8.0, 10.0))
        )
        val result = calc.calculate(records, scores)
        assertEquals(76.67, result.averageScore, 0.5)
        assertEquals(90.0, result.maxScore, 0.01)
        assertEquals(60.0, result.minScore, 0.01)
    }

    @Test
    fun passRate_shouldComputeCorrectly() {
        val records = listOf(
            record(1, "Math", 90.0),
            record(2, "Math", 50.0),
            record(3, "Math", 60.0),
            record(4, "Math", 40.0)
        )
        val result = calc.calculate(records, emptyMap())
        assertEquals(0.5, result.passRate, 0.01) // 2/4 >= 60
    }

    @Test
    fun scoreDistribution_shouldGroupCorrectly() {
        val dist = calc.scoreDistribution(listOf(95.0, 82.0, 75.0, 63.0, 45.0, 30.0))
        assertEquals(1, dist["90-100"])
        assertEquals(1, dist["80-89"])
        assertEquals(1, dist["70-79"])
        assertEquals(1, dist["60-69"])
        assertEquals(1, dist["40-49"])
        assertEquals(1, dist["30-39"])
    }

    @Test
    fun perQuestionStats_shouldComputeErrorRates() {
        val allScores = mapOf(
            1L to listOf(score(1, 1, 10.0, 10.0), score(1, 2, 5.0, 10.0)),
            2L to listOf(score(2, 1, 8.0, 10.0), score(2, 2, 6.0, 10.0))
        )
        val stats = calc.perQuestionStats(allScores)
        assertEquals(2, stats.size)
        assertEquals(9.0, stats[0].averageScore, 0.5)
        assertEquals(0.0, stats[0].errorRate, 0.01) // Q1: all correct
        assertEquals(5.5, stats[1].averageScore, 0.5)
        assertEquals(1.0, stats[1].errorRate, 0.01) // Q2: both wrong
    }

    private fun record(id: Long, subject: String, total: Double) =
        ExamRecord(id = id, studentId = 1, subject = subject, totalScore = total, imageUri = "")

    private fun score(recordId: Long, qNum: Int, s: Double, max: Double) =
        QuestionScore(examRecordId = recordId, questionNumber = qNum, score = s, maxScore = max, isWrong = s < max)
}
