package com.example.marklens.parser

import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel
import android.graphics.RectF
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.robolectric.RobolectricTestRunner

@org.junit.runner.RunWith(RobolectricTestRunner::class)
class StudentInfoParserTest {

    private val parser = StudentInfoParser()

    @Test
    fun parse_allFieldsPresent_shouldReturnAll() {
        val regions = listOf(
            infoRegion(RegionLabel.STUDENT_NAME, "Alice Wang"),
            infoRegion(RegionLabel.STUDENT_ID, "2024001"),
            infoRegion(RegionLabel.CLASS_NAME, "Class 3A")
        )
        val result = parser.parse(regions)
        assertEquals("Alice Wang", result.name)
        assertEquals("2024001", result.studentId)
        assertEquals("Class 3A", result.className)
    }

    @Test
    fun parse_missingField_shouldReturnNull() {
        val regions = listOf(
            infoRegion(RegionLabel.STUDENT_NAME, "Bob")
        )
        val result = parser.parse(regions)
        assertEquals("Bob", result.name)
        assertNull(result.studentId)
        assertNull(result.className)
    }

    @Test
    fun parse_emptyRegions_shouldReturnAllNull() {
        val result = parser.parse(emptyList())
        assertNull(result.name)
        assertNull(result.studentId)
        assertNull(result.className)
    }

    @Test
    fun parse_trimmedText_shouldNotHaveWhitespace() {
        val regions = listOf(
            infoRegion(RegionLabel.STUDENT_NAME, "  Charlie  ")
        )
        assertEquals("Charlie", parser.parse(regions).name)
    }

    private fun infoRegion(label: RegionLabel, text: String) = OcrRegion(
        label = label,
        rect = RectF(0f, 0f, 0.1f, 0.1f),
        rawText = text
    )
}
