package com.example.marklens.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.ui.theme.Slate
import com.example.marklens.ui.theme.SoftGreen

/**
 * Donut ring chart showing pass rate (>=60) vs fail rate.
 *
 * @author Jianheng Sun
 */
@Composable
fun PassRateDonut(
    passRate: Double,
    totalRecords: Int,
    modifier: Modifier = Modifier
) {
    val white = Color.White

    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 24f
            val radius = (size.minDimension - strokeW) / 2
            val topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )
            val arcSize = Size(radius * 2, radius * 2)

            // Background ring (fail)
            drawArc(
                Slate.copy(alpha = 0.3f),
                0f, 360f, false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeW)
            )

            // Pass arc (green)
            drawArc(
                SoftGreen,
                -90f,
                (passRate * 360).toFloat(),
                false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeW)
            )
        }
        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(passRate * 100).toInt()}%",
                color = white,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$totalRecords students",
                color = Slate,
                fontSize = 12.sp
            )
        }
    }
}
