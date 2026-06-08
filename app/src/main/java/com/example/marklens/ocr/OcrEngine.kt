package com.example.marklens.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * Wraps ML Kit Text Recognition for on-device OCR.
 * No network required — runs entirely offline.
 */
class OcrEngine {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * Recognize text from the full bitmap.
     */
    suspend fun recognize(bitmap: Bitmap): Text {
        val image = InputImage.fromBitmap(bitmap, 0)
        return recognizer.process(image).await()
    }

    /**
     * Recognize text from a cropped region of the bitmap.
     * @param region normalized coordinates [0, 1]
     */
    suspend fun recognizeRegion(bitmap: Bitmap, region: android.graphics.RectF): String {
        val cropped = crop(bitmap, region)
        val text = recognize(cropped)
        cropped.recycle()
        return text.text
    }

    private fun crop(bitmap: Bitmap, region: android.graphics.RectF): Bitmap {
        val left = (region.left * bitmap.width).toInt().coerceIn(0, bitmap.width)
        val top = (region.top * bitmap.height).toInt().coerceIn(0, bitmap.height)
        val right = (region.right * bitmap.width).toInt().coerceIn(0, bitmap.width)
        val bottom = (region.bottom * bitmap.height).toInt().coerceIn(0, bitmap.height)
        return Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)
    }

    fun close() {
        recognizer.close()
    }
}
