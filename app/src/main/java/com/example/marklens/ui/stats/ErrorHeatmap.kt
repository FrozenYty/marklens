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
import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.ui.theme.*

/** Count of wrong answers per question. Each bar = number of students who got that question wrong. */
@Composable
fun ErrorHeatmap(allScores: Map<Long, List<QuestionScore>>) {
    if (allScores.isEmpty()) return
    val errCount = mutableMapOf<Int, Int>(); var total = 0
    for ((_, ss) in allScores) for (s in ss) {
        total++; if (s.isWrong) errCount[s.questionNumber] = (errCount[s.questionNumber] ?: 0) + 1
    }
    val sorted = errCount.entries.sortedBy { it.key }
    val maxErr = sorted.maxOfOrNull { it.value } ?: 1

    Column(Modifier.fillMaxWidth()) {
        Text("Wrong answers by question", fontSize = 11.sp, color = InkMuted)
        Text("Number of incorrect responses (${total} total)", fontSize = 10.sp, color = InkFaint)
        Spacer(Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for ((qn, count) in sorted) {
                Row(Modifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Q$qn", Modifier.width(28.dp), fontSize = 12.sp, color = InkMuted)
                    Box(Modifier.weight(1f).height(16.dp).clip(RoundedCornerShape(8.dp))
                        .background(GradeRed.copy(alpha = 0.1f))) {
                        Box(Modifier.fillMaxHeight()
                            .fillMaxWidth(count.toFloat() / (maxErr + 2))
                            .clip(RoundedCornerShape(8.dp)).background(GradeRed.copy(alpha = 0.55f)))
                    }
                    Text("$count err", Modifier.width(44.dp), fontSize = 11.sp, color = InkMuted)
                }
            }
        }
    }
}
