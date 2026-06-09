package com.example.marklens.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.data.entity.RegionTemplate
import com.example.marklens.ui.theme.*

@Composable
fun TemplateListScreen(
    templates: List<RegionTemplate>,
    onNewTemplate: () -> Unit,
    onEditTemplate: (RegionTemplate) -> Unit,
    onDeleteTemplate: (RegionTemplate) -> Unit,
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(PaperCream).statusBarsPadding()) {
        Column(Modifier.fillMaxSize()) {
            // Clean header — single row
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Templates", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = InkPrimary)
                Box(Modifier.clip(RoundedCornerShape(10.dp))
                    .background(InkPrimary.copy(alpha = 0.06f)).clickable(onClick = onBack)
                    .padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text("← Back", fontSize = 13.sp, color = InkPrimary, fontWeight = FontWeight.Medium)
                }
            }

            if (templates.isEmpty()) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Text("📋", fontSize = 56.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("No templates yet", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
                    Text("Create one to quickly scan papers\nwith the same layout.",
                        fontSize = 14.sp, color = InkMuted, textAlign = TextAlign.Center, lineHeight = 20.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = onNewTemplate,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GradeRed, contentColor = CardWhite),
                        modifier = Modifier.height(48.dp)) {
                        Text("＋  Create First Template", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(templates, key = { it.id }) { template ->
                        TemplateCard(template, onEditTemplate, onDeleteTemplate)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
        if (templates.isNotEmpty()) {
            Button(onClick = onNewTemplate,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)
                    .align(Alignment.BottomCenter).height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GradeRed, contentColor = CardWhite)) {
                Text("＋  New Template", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: RegionTemplate,
    onEdit: (RegionTemplate) -> Unit,
    onDelete: (RegionTemplate) -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(template.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkPrimary)
            }
            TextButton(onClick = { onEdit(template) }) { Text("Edit", color = InkMuted, fontSize = 13.sp) }
            TextButton(onClick = { showDelete = true }) { Text("🗑", fontSize = 14.sp) }
        }
    }

    if (showDelete) AlertDialog(
        onDismissRequest = { showDelete = false },
        title = { Text("Delete template?", color = InkPrimary) },
        text = { Text("\"${template.name}\" will be permanently removed.", color = InkMuted) },
        confirmButton = {
            Button(onClick = { onDelete(template); showDelete = false },
                colors = ButtonDefaults.buttonColors(containerColor = GradeRed),
                shape = RoundedCornerShape(10.dp)) { Text("Delete") }
        },
        dismissButton = {
            Button(onClick = { showDelete = false }, shape = RoundedCornerShape(10.dp)) { Text("Cancel") }
        }
    )
}
