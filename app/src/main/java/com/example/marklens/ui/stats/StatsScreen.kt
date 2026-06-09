package com.example.marklens.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.ui.theme.Ink
import com.example.marklens.ui.theme.Slate
import com.example.marklens.ui.theme.SoftGreen
import com.example.marklens.ui.theme.SurfaceWhite

/**
 * Statistics screen — key metrics + 4 visualization charts.
 *
 * @author Jianheng Sun
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val allScores by viewModel.allScores.collectAsState()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize().background(Ink), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SoftGreen)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Text(
            "${uiState.subject} — Statistics",
            color = SurfaceWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Top row: key metrics
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricCard("Average", "${uiState.averageScore.toInt()}")
            MetricCard("Highest", "${uiState.maxScore.toInt()}")
            MetricCard("Lowest", "${uiState.minScore.toInt()}")
        }

        // Total records + pass rate summary
        if (uiState.totalRecords > 0) {
            // Pass-rate donut
            SectionCard("Pass Rate") {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    PassRateDonut(uiState.passRate, uiState.totalRecords)
                }
            }

            // Score histogram
            if (uiState.scoreDistribution.isNotEmpty()) {
                SectionCard("Score Distribution") {
                    ScoreHistogram(uiState.scoreDistribution)
                }
            }

            // Per-question chart
            if (uiState.perQuestionStats.isNotEmpty()) {
                SectionCard("Question Analysis") {
                    QuestionBarChart(uiState.perQuestionStats)
                }
            }

            // Heatmap
            if (allScores.isNotEmpty()) {
                SectionCard("Error Heatmap") {
                    ErrorHeatmap(allScores)
                }
            }
        } else {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 48.sp)
                    Text("No data available", color = Slate, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = SurfaceWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(label, color = Slate, fontSize = 12.sp)
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
            .background(com.example.marklens.ui.theme.SurfaceWhite.copy(alpha = 0.95f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            title,
            fontWeight = FontWeight.SemiBold,
            color = com.example.marklens.ui.theme.Ink,
            fontSize = 15.sp
        )
        androidx.compose.material3.HorizontalDivider(
            color = Slate.copy(alpha = 0.2f)
        )
        content()
    }
}

@Preview(showSystemUi = true)
@Composable
private fun StatsScreenPreview() {
    StatsScreen(viewModel = StatsViewModel())
}
