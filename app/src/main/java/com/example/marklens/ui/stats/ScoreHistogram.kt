package com.example.marklens.ui.stats

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.example.marklens.ui.theme.SoftGreen

/**
 * Bar chart showing score distribution across buckets (0-29, 30-39, ..., 90-100).
 *
 * @author Jianheng Sun
 */
@Composable
fun ScoreHistogram(
    distribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val buckets = listOf("90-100", "80-89", "70-79", "60-69", "50-59", "40-49", "30-39", "0-29")
    val maxCount = (distribution.values.maxOrNull() ?: 1).coerceAtLeast(1)

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val barW = size.width / buckets.size * 0.7f
        val gap = size.width / buckets.size * 0.3f
        val textPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            isAntiAlias = true
        }
        val countPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 20f
            isAntiAlias = true
        }

        buckets.forEachIndexed { i, bucket ->
            val count = distribution[bucket] ?: 0
            val barH = (count.toFloat() / maxCount) * (size.height - 28f)
            val x = i * (barW + gap) + gap / 2
            val y = size.height - barH - 24f

            // Bar
            drawRect(SoftGreen, Offset(x, y), Size(barW, barH.coerceAtLeast(2f)))

            // X-axis label (abbreviated)
            drawContext.canvas.nativeCanvas.drawText(
                bucket.take(2),
                x + barW / 4,
                size.height - 4f,
                textPaint
            )

            // Count on top
            if (count > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "$count",
                    x + barW / 4,
                    y - 4f,
                    countPaint
                )
            }
        }
    }
}
