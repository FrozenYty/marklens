package com.example.marklens.util

import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore

data class StatsResult(
    val totalRecords: Int = 0,
    val averageScore: Double = 0.0,
    val maxScore: Double = 0.0,
    val minScore: Double = 0.0,
    val passRate: Double = 0.0,
    val scoreDistribution: Map<String, Int> = emptyMap(),
    val perQuestionStats: List<QuestionStat> = emptyList()
)

data class QuestionStat(
    val questionNumber: Int,
    val maxScore: Double,
    val averageScore: Double,
    val errorRate: Double,
    val totalAttempts: Int
)

class StatsCalculator {

    fun calculate(records: List<ExamRecord>, allScores: Map<Long, List<QuestionScore>>): StatsResult {
        if (records.isEmpty()) return StatsResult()
        val totals = records.map { it.totalScore }
        val passCount = totals.count { it >= 60.0 }
        return StatsResult(
            totalRecords = records.size,
            averageScore = totals.average(),
            maxScore = totals.max(),
            minScore = totals.min(),
            passRate = passCount.toDouble() / records.size,
            scoreDistribution = scoreDistribution(totals),
            perQuestionStats = perQuestionStats(allScores)
        )
    }

    fun scoreDistribution(scores: List<Double>): Map<String, Int> {
        val buckets = listOf("90-100", "80-89", "70-79", "60-69", "50-59", "40-49", "30-39", "0-29")
        val result = mutableMapOf<String, Int>()
        for (bucket in buckets) result[bucket] = 0
        for (s in scores) {
            val bucket = when {
                s >= 90 -> "90-100"
                s >= 80 -> "80-89"
                s >= 70 -> "70-79"
                s >= 60 -> "60-69"
                s >= 50 -> "50-59"
                s >= 40 -> "40-49"
                s >= 30 -> "30-39"
                else -> "0-29"
            }
            result[bucket] = result[bucket]!! + 1
        }
        return result.filter { it.value > 0 }
    }

    fun perQuestionStats(allScores: Map<Long, List<QuestionScore>>): List<QuestionStat> {
        if (allScores.isEmpty()) return emptyList()
        val byQuestion = mutableMapOf<Int, MutableList<QuestionScore>>()
        for ((_, scores) in allScores) {
            for (s in scores) {
                byQuestion.getOrPut(s.questionNumber) { mutableListOf() }.add(s)
            }
        }
        return byQuestion.entries.sortedBy { it.key }.map { (qNum, scores) ->
            QuestionStat(
                questionNumber = qNum,
                maxScore = scores.first().maxScore,
                averageScore = scores.map { it.score }.average(),
                errorRate = scores.count { it.isWrong }.toDouble() / scores.size,
                totalAttempts = scores.size
            )
        }
    }
}
