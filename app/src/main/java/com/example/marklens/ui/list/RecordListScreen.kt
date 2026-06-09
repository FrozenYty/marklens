package com.example.marklens.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.ui.theme.Ink
import com.example.marklens.ui.theme.MarkRed
import com.example.marklens.ui.theme.Slate
import com.example.marklens.ui.theme.SoftGreen
import com.example.marklens.ui.theme.SurfaceWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Record list screen — displays all saved exam records with subject filtering.
 *
 * @author Jianheng Sun
 */
@Composable
fun RecordListScreen(
    viewModel: RecordListViewModel,
    onRecordClick: (Long) -> Unit = {},
    onStatsClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Exam Records",
                    color = SurfaceWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${uiState.records.size} record${if (uiState.records.size != 1) "s" else ""}",
                    color = Slate,
                    fontSize = 14.sp
                )
            }
            if (uiState.records.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SoftGreen.copy(alpha = 0.2f))
                        .clickable {
                            onStatsClick(uiState.selectedSubject ?: "")
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("📊 Stats", color = SoftGreen, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Subject filter chips
        if (uiState.subjects.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All" chip
                FilterChip(
                    label = "All",
                    selected = uiState.selectedSubject == null,
                    onClick = { viewModel.onEvent(RecordListEvent.SubjectSelected(null)) }
                )
                for (subject in uiState.subjects) {
                    FilterChip(
                        label = subject,
                        selected = uiState.selectedSubject == subject,
                        onClick = {
                            viewModel.onEvent(
                                RecordListEvent.SubjectSelected(
                                    if (uiState.selectedSubject == subject) null else subject
                                )
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SoftGreen)
                }
            }
            uiState.records.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Text(
                            if (uiState.selectedSubject != null) "No records for ${uiState.selectedSubject}"
                            else "No records yet",
                            color = Slate,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.records, key = { it.id }) { record ->
                        RecordCard(
                            record = record,
                            onClick = { onRecordClick(record.id) },
                            onDelete = {
                                viewModel.onEvent(RecordListEvent.RecordDeleted(record.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordCard(
    record: ExamRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite.copy(alpha = 0.95f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(record.subject, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                "Score: ${record.totalScore.toInt()}",
                color = if (record.totalScore >= 60) SoftGreen else MarkRed,
                fontSize = 14.sp
            )
            Text(
                dateFormat.format(Date(record.createdAt)),
                color = Slate,
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onDelete) {
            Text("🗑", fontSize = 16.sp)
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) SoftGreen else SurfaceWhite.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) SurfaceWhite else Slate,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun RecordListScreenPreview() {
    RecordListScreen(viewModel = RecordListViewModel())
}
