package com.example.marklens.ocr

import android.graphics.RectF
import java.util.UUID

/**
 * A user-defined region on the exam paper image.
 * Coordinates are normalized [0, 1] relative to image dimensions.
 */
data class OcrRegion(
    val id: String = UUID.randomUUID().toString(),
    val label: RegionLabel,
    val rect: RectF,              // normalized [0, 1]
    val rawText: String = "",     // OCR result
    val parsedValue: String = ""  // user-facing value after correction
)

enum class RegionLabel(val displayName: String) {
    STUDENT_NAME("Name"),
    STUDENT_ID("Student ID"),
    CLASS_NAME("Class"),
    SUBJECT("Subject"),
    QUESTION_SCORE("Q Score"),
    TOTAL_SCORE("Total Score"),
    CUSTOM("Custom")
}
