package com.example.marklens.ui.list

import android.content.Intent
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.data.ExamRepository
import com.example.marklens.data.entity.ExamRecord
import com.example.marklens.ui.theme.CardWhite
import com.example.marklens.ui.theme.GradeRed
import com.example.marklens.ui.theme.InkMuted
import com.example.marklens.ui.theme.InkPrimary
import com.example.marklens.ui.theme.PaperCream
import com.example.marklens.ui.theme.StampTeal
import com.example.marklens.util.CsvExporter
import kotlinx.coroutines.launch
import java.io.File
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
    repository: ExamRepository? = null,
    onRecordClick: (Long) -> Unit = {},
    onStatsClick: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperCream)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.clip(RoundedCornerShape(10.dp))
                    .background(InkMuted.copy(alpha = 0.1f))
                    .clickable(onClick = onBack)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("← Back", fontSize = 13.sp, color = InkPrimary, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Exam Records", color = InkPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    "${uiState.records.size} record${if (uiState.records.size != 1) "s" else ""}",
                    color = InkMuted, fontSize = 13.sp
                )
            }
        }

        // Action bar — Stats and CSV as full-width buttons below header
        if (uiState.records.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(StampTeal.copy(alpha = 0.1f))
                        .clickable { onStatsClick(uiState.selectedSubject ?: "") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📊  Statistics", color = StampTeal, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
                if (repository != null) {
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(InkMuted.copy(alpha = 0.08f))
                            .clickable {
                                scope.launch {
                                    val records = uiState.records
                                    val allScores = records.associate { r ->
                                        val pair = repository.getRecordWithScores(r.id)
                                        r.id to (pair?.second ?: emptyList())
                                    }
                                    val csv = CsvExporter.export(records, allScores)
                                    val file = File(context.cacheDir, "marklens_export.csv")
                                    file.writeText(csv)
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context, "${context.packageName}.fileprovider", file
                                    )
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Export CSV"))
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📄  Export CSV", color = InkPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
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
            Spacer(Modifier.height(8.dp))
        }

        // Content
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = StampTeal)
                }
            }
            uiState.records.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (uiState.selectedSubject != null) "No records for ${uiState.selectedSubject}"
                            else "No records yet",
                            color = InkMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.records, key = { it.id }) { record ->
                        RecordCard(
                            record = record,
                            studentName = uiState.studentNames[record.studentId] ?: "Unknown",
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
    studentName: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(studentName, color = InkPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(record.subject, color = InkMuted, fontSize = 13.sp)
                Text("  ·  ", color = InkMuted, fontSize = 13.sp)
                Text(
                    "${record.totalScore.toInt()} pts",
                    color = if (record.totalScore >= 60) StampTeal else GradeRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                dateFormat.format(Date(record.createdAt)),
                color = InkMuted.copy(alpha = 0.6f),
                fontSize = 11.sp
            )
        }
        var showConfirm by remember { mutableStateOf(false) }
        IconButton(onClick = { showConfirm = true }) {
            Text("🗑", fontSize = 16.sp)
        }
        if (showConfirm) AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Delete record?", color = InkPrimary) },
            text = { Text("${record.subject} — Score ${record.totalScore.toInt()}", color = InkMuted) },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GradeRed),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirm = false },
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Cancel") }
            }
        )
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
            .background(if (selected) StampTeal else InkMuted.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) CardWhite else InkMuted,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
