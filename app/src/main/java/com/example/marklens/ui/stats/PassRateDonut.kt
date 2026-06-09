package com.example.marklens.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.ui.theme.*

/** Pass rate: percentage of students with total score ≥ 60%. Ring chart with centered percentage. */
@Composable
fun PassRateDonut(passRate: Float, total: Int) {
    val pct = (passRate * 100).toInt()
    val passCount = (total * passRate).toInt()

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Share of students scoring ≥ 60%", fontSize = 11.sp, color = InkMuted)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val sw = 18f; val r = size.width / 2f - sw / 2f
                drawArc(DonutFail, 0f, 360f, false,
                    topLeft = Offset(sw / 2f, sw / 2f), size = Size(r * 2, r * 2),
                    style = Stroke(sw, cap = StrokeCap.Round))
                drawArc(StampTeal, -90f, 360f * passRate, false,
                    topLeft = Offset(sw / 2f, sw / 2f), size = Size(r * 2, r * 2),
                    style = Stroke(sw, cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$pct%", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = StampTeal)
                Text("$passCount / $total", fontSize = 12.sp, color = InkMuted)
            }
        }
    }
}
