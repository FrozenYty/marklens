package com.example.marklens.ocr

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * Pluggable OCR provider — users can configure their own backend.
 *
 * Built-in: [MlKitOcrProvider] (free, offline, printed text).
 * Cloud: implement this interface with Cloud Vision / custom API.
 *
 * Architecture: one [recognizeRegion] call per field region.
 * Template tells us WHERE each field is → deterministic, high accuracy.
 */
interface OcrProvider {
    /** Recognize text from a cropped region of the image. */
    suspend fun recognizeRegion(image: Bitmap, region: RectF): String

    /** Recognize all text blocks with positions (for auto-layout analysis). */
    suspend fun recognizeBlocks(image: Bitmap): List<TextBlock>

    fun close()
}

/**
 * Default provider using Google ML Kit (free, on-device, printed-text only).
 * Replace with Cloud Vision for handwriting support.
 */
class MlKitOcrProvider : OcrProvider {

    private val engine = OcrEngine()

    override suspend fun recognizeRegion(image: Bitmap, region: RectF): String {
        return engine.recognizeRegion(image, region)
    }

    override suspend fun recognizeBlocks(image: Bitmap): List<TextBlock> {
        return engine.recognizeBlocks(image)
    }

    override fun close() { engine.close() }
}
