package com.example.marklens.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.util.QuestionStat
import com.example.marklens.ui.theme.Amber
import com.example.marklens.ui.theme.MarkRed
import com.example.marklens.ui.theme.Slate
import com.example.marklens.ui.theme.SoftGreen

/**
 * Horizontal bar chart per question — average score ratio with color coding.
 *
 * @author Jianheng Sun
 */
@Composable
fun QuestionBarChart(
    stats: List<QuestionStat>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        for (stat in stats) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Q-number label
                Text(
                    "Q${stat.questionNumber}",
                    color = Slate,
                    fontSize = 12.sp,
                    modifier = Modifier.width(32.dp)
                )

                // Bar container
                val ratio = (stat.averageScore / stat.maxScore).coerceIn(0.0, 1.0)
                val barColor = when {
                    ratio >= 0.8 -> SoftGreen
                    ratio >= 0.6 -> Amber
                    else -> MarkRed
                }
                Box(modifier = Modifier.weight(1f).height(20.dp)) {
                    // Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Slate.copy(alpha = 0.2f))
                    )
                    // Fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio.toFloat())
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor)
                    )
                }

                // Score text
                Text(
                    "${stat.averageScore.toInt()}/${stat.maxScore.toInt()}",
                    color = Slate,
                    fontSize = 11.sp,
                    modifier = Modifier.width(48.dp)
                )
                // Error rate
                Text(
                    "${(stat.errorRate * 100).toInt()}% err",
                    color = MarkRed.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    modifier = Modifier.width(48.dp)
                )
            }
        }
    }
}
