package com.example.marklens.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StatsScreen(viewModel: StatsViewModel, onBack: () -> Unit = {},
                repository: com.example.marklens.data.ExamRepository? = null) {
    val uiState by viewModel.uiState.collectAsState()
    val allScores by viewModel.allScores.collectAsState()

    // Subject filter state
    var subjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    // Default to first subject if none selected
    LaunchedEffect(subjects) {
        if (selectedSubject == null && subjects.isNotEmpty()) selectedSubject = subjects.first()
    }
    val activeVm = remember(selectedSubject) {
        if (selectedSubject != null) StatsViewModel(repository, selectedSubject)
        else viewModel
    }
    val activeState by activeVm.uiState.collectAsState()
    val activeScores by activeVm.allScores.collectAsState()

    LaunchedEffect(repository) {
        repository?.getAllRecords()?.collectLatest { records ->
            subjects = records.map { it.subject }.distinct().sorted()
        }
    }

    if (activeState.isLoading) {
        Box(Modifier.fillMaxSize().background(PaperCream), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = StampTeal)
        }
        return
    }

    Column(Modifier.fillMaxSize().background(PaperCream).statusBarsPadding()
        .verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // Header with back button
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.clip(RoundedCornerShape(10.dp))
                    .background(CardWhite).clickable(onClick = onBack)
                    .padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text("← Back", fontSize = 13.sp, color = InkPrimary, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${activeState.subject} — Stats", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                    Text("${activeState.totalRecords} records", fontSize = 12.sp, color = InkMuted)
                }
            }
        }

        // Subject filter chips
        if (subjects.size > 1) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (s in subjects) { Chip(s, selectedSubject == s) { selectedSubject = s } }
            }
        }

        if (activeState.totalRecords == 0) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Text("No data", color = InkMuted, fontSize = 14.sp)
            }
            return@Column
        }

        // Metrics
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Metric("Avg", "${activeState.averageScore.toInt()}")
            Metric("Max", "${activeState.maxScore.toInt()}")
            Metric("Min", "${activeState.minScore.toInt()}")
        }

        // Pass rate
        Card("Pass Rate") { PassRateDonut(activeState.passRate.toFloat(), activeState.totalRecords) }

        // Score distribution
        if (activeState.scoreDistribution.isNotEmpty())
            Card("Score Distribution") { ScoreHistogram(activeState.scoreDistribution) }

        // Per-question
        if (activeState.perQuestionStats.isNotEmpty())
            Card("Per-Question Average") { QuestionBarChart(activeState.perQuestionStats) }

        // Error rate
        if (activeScores.isNotEmpty())
            Card("Wrong Answers") { ErrorHeatmap(activeScores) }
    }
}

@Composable
private fun Chip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(14.dp))
        .background(if (selected) StampTeal else CardWhite).clickable(onClick = onClick)
        .padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) CardWhite else InkMuted)
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StampTeal)
        Text(label, fontSize = 11.sp, color = InkMuted)
    }
}

@Composable
private fun Card(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
        .background(CardWhite).padding(16.dp)) {
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
        Spacer(Modifier.height(8.dp))
        content()
    }
}
