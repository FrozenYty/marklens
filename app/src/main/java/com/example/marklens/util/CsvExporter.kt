package com.example.marklens.util

import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore

object CsvExporter {

    fun export(records: List<ExamRecord>, allScores: Map<Long, List<QuestionScore>>): String {
        val sb = StringBuilder()

        // Determine max question count for dynamic columns
        val maxQuestions = allScores.values.flatten().maxOfOrNull { it.questionNumber } ?: 0
        val questionHeaders = (1..maxQuestions).joinToString(",") { "Q$it" }

        // Header
        sb.appendLine("Student,StudentID,Class,Subject,TotalScore,$questionHeaders")

        for (record in records) {
            val scores = allScores[record.id].orEmpty()
            val scoreMap = scores.associateBy { it.questionNumber }
            val qValues = (1..maxQuestions).joinToString(",") { q ->
                scoreMap[q]?.score?.toString() ?: ""
            }
            // Student info placeholders — filled by caller or from joined data
            sb.appendLine(",,,${record.subject},${record.totalScore},$qValues")
        }

        return sb.toString().trimEnd()
    }
}
