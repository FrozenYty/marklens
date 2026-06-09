package com.example.marklens.ui.review

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marklens.ui.theme.CardWhite
import com.example.marklens.ui.theme.GradeRed
import com.example.marklens.ui.theme.InkMuted
import com.example.marklens.ui.theme.InkPrimary
import com.example.marklens.ui.theme.PaperCream
import com.example.marklens.ui.theme.StampTeal

/**
 * Review & correction screen — displays parsed OCR results for user verification
 * before saving to the database.
 *
 * @author Tianyu Yao
 */
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    imageUri: String = "",
    onSaved: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) {
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperCream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.clip(RoundedCornerShape(8.dp)).background(InkMuted.copy(alpha = 0.1f))
                .clickable(onClick = onCancel).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text("← Cancel", color = InkPrimary, fontSize = 13.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Review & Correct", color = InkPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Verify OCR results before saving", color = InkMuted, fontSize = 14.sp)
            }
        }

        // Original photo preview
        if (imageUri.isNotBlank() && !imageUri.startsWith("record_")) {
            SectionCard("Original Photo") {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (imageUri.startsWith("/")) java.io.File(imageUri) else Uri.parse(imageUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "Original exam photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // Student info section
        SectionCard("Student Information") {
            FieldRow(
                label = "Name",
                value = uiState.studentInfo.name ?: "",
                onValueChange = viewModel::updateName
            )
            FieldRow(
                label = "Student ID",
                value = uiState.studentInfo.studentId ?: "",
                onValueChange = viewModel::updateStudentId
            )
            FieldRow(
                label = "Class",
                value = uiState.studentInfo.className ?: "",
                onValueChange = viewModel::updateClassName
            )
        }

        // Exam info
        SectionCard("Exam Information") {
            FieldRow(
                label = "Subject",
                value = uiState.subject,
                onValueChange = viewModel::updateSubject
            )
            FieldRow(
                label = "Total Score",
                value = uiState.totalScore,
                onValueChange = viewModel::updateTotalScore
            )
        }

        // Question scores
        if (uiState.scores.isNotEmpty()) {
            SectionCard("Question Scores") {
                for (field in uiState.scores) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Q${field.questionNumber}",
                            color = InkMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.width(32.dp)
                        )
                        OutlinedTextField(
                            value = field.score,
                            onValueChange = { viewModel.updateScore(field.questionNumber, it) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = fieldColors(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Text(
                            "/ ${field.maxScore.toInt()}",
                            color = InkMuted,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // Save button
        Button(
            onClick = { viewModel.save(imageUri) },
            enabled = !uiState.isSaving && !uiState.saveComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StampTeal,
                contentColor = CardWhite,
                disabledContainerColor = StampTeal.copy(alpha = 0.5f),
                disabledContentColor = CardWhite.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    color = CardWhite,
                    modifier = Modifier.height(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    if (uiState.saveComplete) "Saved ✓" else "Save to Database",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (uiState.saveComplete) {
            Text("Record saved successfully.", color = StampTeal, fontSize = 13.sp)
        }
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
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, color = InkPrimary, fontSize = 15.sp)
        HorizontalDivider(color = InkMuted.copy(alpha = 0.2f))
        content()
    }
}

@Composable
private fun FieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, color = InkMuted, fontSize = 13.sp, modifier = Modifier.width(72.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = fieldColors(),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = InkPrimary,
    unfocusedTextColor = InkPrimary,
    focusedBorderColor = StampTeal,
    unfocusedBorderColor = InkMuted.copy(alpha = 0.3f),
    cursorColor = GradeRed,
    focusedContainerColor = CardWhite,
    unfocusedContainerColor = CardWhite
)
