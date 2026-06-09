package com.example.marklens.parser

import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel

data class ParsedStudentInfo(
    val name: String?,
    val studentId: String?,
    val className: String?
)

class StudentInfoParser {
    fun parse(regions: List<OcrRegion>): ParsedStudentInfo {
        fun textFor(label: RegionLabel): String? {
            val text = regions.find { it.label == label }?.rawText?.trim()
            return cleanLabel(text)
        }
        return ParsedStudentInfo(
            name = textFor(RegionLabel.STUDENT_NAME),
            studentId = textFor(RegionLabel.STUDENT_ID),
            className = textFor(RegionLabel.CLASS_NAME)
        )
    }

    companion object {
        fun cleanLabel(text: String?): String? {
            if (text.isNullOrEmpty()) return null
            if (text.contains(":") || text.contains("：")) {
                return text.substringAfter(":").substringAfter("：").trim().ifBlank { text }
            }
            return text
        }
    }
}
