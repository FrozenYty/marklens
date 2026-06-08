package com.example.marklens.util

import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.data.entity.QuestionScore
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class CsvExporterTest {

    @Test
    fun export_empty_shouldReturnHeaderOnly() {
        val csv = CsvExporter.export(emptyList(), emptyMap())
        val lines = csv.lines()
        assertEquals(1, lines.size)
        assertTrue(lines[0].startsWith("Student,StudentID,Class,Subject"))
    }

    @Test
    fun export_singleRecord_shouldIncludeAllFields() {
        val records = listOf(
            ExamRecord(id = 1, studentId = 10, subject = "Math", totalScore = 85.0, imageUri = "")
        )
        val csv = CsvExporter.export(records, emptyMap())
        val lines = csv.lines()
        assertEquals(2, lines.size)
        val dataLine = lines[1]
        assertTrue(dataLine.contains("Math"))
        assertTrue(dataLine.contains("85"))
    }

    @Test
    fun export_withScores_shouldIncludeQuestionColumns() {
        val records = listOf(
            ExamRecord(id = 1, studentId = 10, subject = "Math", totalScore = 17.0, imageUri = "")
        )
        val scores = mapOf(1L to listOf(
            QuestionScore(examRecordId = 1, questionNumber = 1, score = 8.0, maxScore = 10.0, isWrong = false),
            QuestionScore(examRecordId = 1, questionNumber = 2, score = 9.0, maxScore = 10.0, isWrong = false)
        ))
        val csv = CsvExporter.export(records, scores)
        val lines = csv.lines()
        val header = lines[0]
        assertTrue(header.contains("Q1"))
        assertTrue(header.contains("Q2"))
    }
}
