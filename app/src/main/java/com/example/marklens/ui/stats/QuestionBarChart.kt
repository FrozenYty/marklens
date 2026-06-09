package com.example.marklens.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.ui.theme.*
import com.example.marklens.util.QuestionStat

/** Average score per question (horizontal bars). Shows mean score out of max for each question number. */
@Composable
fun QuestionBarChart(stats: List<QuestionStat>) {
    if (stats.isEmpty()) return

    Column(Modifier.fillMaxWidth()) {
        Text("Average score by question", fontSize = 11.sp, color = InkMuted)
        Text("Mean points earned out of maximum", fontSize = 10.sp, color = InkFaint)
        Spacer(Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            for (q in stats) {
                val pct = (q.averageScore / q.maxScore).toFloat().coerceIn(0f, 1f)
                Row(Modifier.fillMaxWidth().height(26.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Q${q.questionNumber}", Modifier.width(28.dp), fontSize = 12.sp, color = InkMuted)
                    Box(Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(9.dp))
                        .background(StampTeal.copy(alpha = 0.12f))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(pct)
                            .clip(RoundedCornerShape(9.dp)).background(StampTeal))
                    }
                    Text("${q.averageScore.toInt()}/${q.maxScore.toInt()}",
                        Modifier.width(44.dp), fontSize = 11.sp, color = InkMuted)
                }
            }
        }
    }
}
