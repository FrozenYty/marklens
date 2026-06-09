package com.example.marklens.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.ui.theme.*

@Composable
fun ScoreHistogram(distribution: Map<String, Int>) {
    if (distribution.isEmpty()) return
    val entries = distribution.entries.sortedBy { it.key }
    val maxCount = entries.maxOf { it.value }
    val labelP = android.graphics.Paint().apply { color = 0xFF1E1A16.toInt(); textSize = 26f; isAntiAlias = true }
    val valP = android.graphics.Paint().apply { color = 0xFF00796B.toInt(); textSize = 24f; isAntiAlias = true; isFakeBoldText = true }

    Column(Modifier.fillMaxWidth()) {
        Text("Score distribution", fontSize = 11.sp, color = InkMuted)
        Text("Students per score range", fontSize = 10.sp, color = InkFaint)
        Spacer(Modifier.height(6.dp))
        Canvas(Modifier.fillMaxWidth().height(130.dp)) {
            val w = size.width; val h = size.height
            val gap = w * 0.03f; val totalGap = gap * (entries.size + 1)
            val barW = ((w - 30f) - totalGap) / entries.size
            entries.forEachIndexed { i, (label, count) ->
                val x = 15f + gap + i * (barW + gap)
                val bh = (count.toFloat() / maxCount) * (h - 40f)
                drawRect(StampTeal, Offset(x, h - 28f - bh), Size(barW, bh))
                drawContext.canvas.nativeCanvas.drawText("$count", x + barW / 2f - 8f, h - 32f - bh, valP)
                drawContext.canvas.nativeCanvas.drawText(label, x + barW / 2f - 16f, h, labelP)
            }
        }
    }
}
