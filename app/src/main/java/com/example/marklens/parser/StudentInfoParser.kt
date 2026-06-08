package com.example.marklens.parser

import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel

data class ParsedStudentInfo(
    val name: String?,
    val studentId: String?,
    val className: String?
)

class StudentInfoParser {

    /**
     * Extract student info from OCR regions.
     * Returns null for any field where the corresponding region is missing or empty.
     */
    fun parse(regions: List<OcrRegion>): ParsedStudentInfo {
        fun textFor(label: RegionLabel): String? {
            val text = regions.find { it.label == label }?.rawText?.trim()
            return if (text.isNullOrEmpty()) null else text
        }
        return ParsedStudentInfo(
            name = textFor(RegionLabel.STUDENT_NAME),
            studentId = textFor(RegionLabel.STUDENT_ID),
            className = textFor(RegionLabel.CLASS_NAME)
        )
    }
}
