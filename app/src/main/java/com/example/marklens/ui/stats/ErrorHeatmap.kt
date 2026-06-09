package com.example.marklens.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.data.entity.QuestionScore
import com.example.marklens.ui.theme.MarkRed
import com.example.marklens.ui.theme.Slate
import com.example.marklens.ui.theme.SoftGreen

/**
 * Heatmap grid: rows = students, columns = questions.
 * Green cell = correct, red cell = wrong.
 *
 * @author Jianheng Sun
 */
@Composable
fun ErrorHeatmap(
    allScores: Map<Long, List<QuestionScore>>,
    modifier: Modifier = Modifier
) {
    val entries = allScores.entries.toList()
    if (entries.isEmpty()) return

    val maxQ = entries.flatMap { it.value }.maxOf { it.questionNumber }
    val cellSize = 20.dp

    Column(modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        // Column header
        Row {
            for (q in 1..maxQ) {
                Text(
                    "Q$q",
                    color = Slate,
                    fontSize = 9.sp,
                    modifier = Modifier.width(cellSize),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Heatmap rows
        for ((idx, entry) in entries.withIndex()) {
            val scores = entry.value.sortedBy { it.questionNumber }
            Row {
                for (score in scores) {
                    Box(modifier = Modifier.size(cellSize).padding(2.dp)) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawRect(
                                if (score.isWrong) MarkRed.copy(alpha = 0.7f)
                                else SoftGreen.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
