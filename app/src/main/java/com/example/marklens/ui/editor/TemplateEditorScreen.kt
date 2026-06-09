package com.example.marklens.ui.editor

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel
import com.example.marklens.ui.theme.*

/**
 * Template editor — single responsibility: draw regions on a photo,
 * label them, save as a reusable template.
 *
 * Clean layout:
 *   Top:   "Template Editor" + Back
 *   Center: Photo with draggable regions
 *   Bottom: Label selector + Save button (single bar)
 */
@Composable
fun TemplateEditorScreen(
    bitmap: Bitmap?,
    regions: List<OcrRegion>,
    selectedId: String?,
    editingTemplateId: Long? = null,
    existingTemplateName: String = "",
    onPhotoRequested: () -> Unit,
    onRegionCreated: (RectF) -> Unit,
    onRegionSelected: (String?) -> Unit,
    onRegionDeleted: (String) -> Unit,
    onChangeLabel: (String, RegionLabel) -> Unit,
    onSaveTemplate: (String) -> Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit
) {
    var templateName by remember { mutableStateOf(existingTemplateName) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var duplicateError by remember { mutableStateOf(false) }
    var zoomScale by remember { mutableStateOf(1f) }
    var zoomOX by remember { mutableStateOf(0f) }
    var zoomOY by remember { mutableStateOf(0f) }

    Box(Modifier.fillMaxSize().background(PaperCream)) {
        if (bitmap == null) {
            Column(Modifier.align(Alignment.Center).padding(32.dp).padding(bottom = if (regions.isNotEmpty()) 70.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (regions.isNotEmpty()) {
                    Text("${regions.size} regions", fontSize = 18.sp, color = InkPrimary, fontWeight = FontWeight.Bold)
                    // Scrollable region list with edit controls
                    LazyColumn(Modifier.fillMaxWidth().weight(1f, fill = false).padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(regions.size) { i ->
                            val r = regions[i]
                            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(if (r.id == selectedId) StampTeal.copy(alpha = 0.08f) else CardWhite)
                                .clickable { onRegionSelected(r.id) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(r.label.displayName, fontSize = 14.sp, color = InkPrimary, fontWeight = FontWeight.Medium)
                                Text("(${(r.rect.left*100).toInt()}%, ${(r.rect.top*100).toInt()}%)",
                                    fontSize = 11.sp, color = InkMuted)
                            }
                        }
                    }
                } else {
                    Text("📷", fontSize = 48.sp)
                    Text("Take a photo of the exam paper", fontSize = 16.sp, color = InkPrimary, fontWeight = FontWeight.Medium)
                    Text("Then draw regions and label each field", fontSize = 14.sp, color = InkMuted)
                }
                Button(onClick = onPhotoRequested,
                    colors = ButtonDefaults.buttonColors(containerColor = GradeRed, contentColor = CardWhite),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.height(44.dp)) {
                    Text("📷  Take Photo", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            // Edit bar when no photo but regions exist
            if (regions.isNotEmpty()) {
                Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp, start = 12.dp, end = 12.dp)
                    .clip(RoundedCornerShape(16.dp)).background(CardWhite.copy(alpha = 0.95f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    CompactBtn("✕ Del", selectedId != null, GradeRed) { selectedId?.let { onRegionDeleted(it) } }
                    CompactBtn("↩", canUndo, InkMuted) { onUndo() }
                    CompactBtn("↪", canRedo, InkMuted) { onRedo() }
                    Text("${regions.size}", color = InkMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Button(onClick = { showSaveDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StampTeal, contentColor = CardWhite),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                        Text("Save", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        } else {
            RegionCanvas(
                bitmap = bitmap, regions = regions, selectedId = selectedId,
                zoomScale = zoomScale, zoomOX = zoomOX, zoomOY = zoomOY,
                onZoomChange = { s, ox, oy -> zoomScale = s; zoomOX = ox; zoomOY = oy },
                onRegionCreated = onRegionCreated,
                onRegionSelected = onRegionSelected
            )

            // ── Bottom bar — minimal + readable ──
            Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp, start = 8.dp, end = 8.dp)
                .clip(RoundedCornerShape(16.dp)).background(CardWhite.copy(alpha = 0.95f))
                .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically) {

                var labelExpanded by remember { mutableStateOf(false) }
                val curLabel = selectedId?.let { id -> regions.find { it.id == id }?.label }
                Box {
                    CompactBtn(curLabel?.displayName ?: "Label", selectedId != null, StampTeal) { labelExpanded = true }
                    DropdownMenu(expanded = labelExpanded && selectedId != null, onDismissRequest = { labelExpanded = false }) {
                        RegionLabel.entries.forEach { l ->
                            DropdownMenuItem(text = { Text(l.displayName) },
                                onClick = { selectedId?.let { onChangeLabel(it, l) }; labelExpanded = false })
                        }
                    }
                }

                var confirmDel by remember { mutableStateOf(false) }
                CompactBtn(if (confirmDel) "Sure?" else "✕", selectedId != null, GradeRed) {
                    if (confirmDel) { selectedId?.let { onRegionDeleted(it) }; confirmDel = false }
                    else confirmDel = true
                }
                LaunchedEffect(selectedId) { confirmDel = false }

                CompactBtn("↩", canUndo, InkMuted) { onUndo() }
                Text("${regions.size}", color = InkMuted, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                CompactBtn("Save", regions.isNotEmpty(), StampTeal) { showSaveDialog = true }
            }

            // Zoom — top right
            Column(Modifier.align(Alignment.TopEnd).padding(top = 60.dp, end = 8.dp)
                .clip(RoundedCornerShape(8.dp)).background(CardWhite.copy(alpha = 0.9f))) {
                Text("＋", Modifier.clickable { zoomScale = (zoomScale * 1.3f).coerceIn(0.5f, 4f); zoomOX = 0f; zoomOY = 0f }
                    .padding(horizontal = 10.dp, vertical = 4.dp), color = InkPrimary, fontSize = 16.sp)
                Text("${(zoomScale * 100).toInt()}%", color = InkMuted, fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp))
                Text("－", Modifier.clickable { zoomScale = (zoomScale / 1.3f).coerceIn(0.5f, 4f); zoomOX = 0f; zoomOY = 0f }
                    .padding(horizontal = 10.dp, vertical = 4.dp), color = InkPrimary, fontSize = 16.sp)
            }

            // Region count badge
            if (regions.isNotEmpty()) {
                Text("${regions.size} fields", Modifier.align(Alignment.TopCenter)
                    .padding(top = 12.dp).clip(RoundedCornerShape(20.dp))
                    .background(CardWhite.copy(alpha = 0.9f)).padding(horizontal = 14.dp, vertical = 6.dp),
                    color = InkPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }

        // Top bar
        Row(Modifier.align(Alignment.TopStart).padding(16.dp).statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.clip(RoundedCornerShape(10.dp))
                .background(CardWhite.copy(alpha = 0.85f)).clickable(onClick = onBack)
                .padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text("← Back", fontSize = 14.sp, color = InkPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }

    // Save dialog
    if (showSaveDialog) {
        AlertDialog(onDismissRequest = { showSaveDialog = false; duplicateError = false },
            title = { Text(if (editingTemplateId != null) "Rename Template" else "Save Template", color = InkPrimary) },
            text = {
                Column {
                    OutlinedTextField(value = templateName, onValueChange = { templateName = it; duplicateError = false },
                        placeholder = { Text("e.g. Math Midterm") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        isError = duplicateError,
                        shape = RoundedCornerShape(10.dp))
                    if (duplicateError) {
                        Text("A template with this name already exists.", color = GradeRed, fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val name = templateName.trim()
                    if (name.isNotBlank()) {
                        if (onSaveTemplate(name)) {
                            templateName = ""; showSaveDialog = false; duplicateError = false
                        } else {
                            duplicateError = true
                        }
                    }
                },
                    colors = ButtonDefaults.buttonColors(containerColor = StampTeal),
                    shape = RoundedCornerShape(10.dp)) { Text("Save") }
            },
            dismissButton = {
                Button(onClick = { showSaveDialog = false; duplicateError = false },
                    shape = RoundedCornerShape(10.dp)) { Text("Cancel") }
            })
    }
}

// ── Canvas ──

@Composable
private fun RegionCanvas(
    bitmap: Bitmap, regions: List<OcrRegion>, selectedId: String?,
    zoomScale: Float, zoomOX: Float, zoomOY: Float,
    onZoomChange: (Float, Float, Float) -> Unit,
    onRegionCreated: (RectF) -> Unit, onRegionSelected: (String?) -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }
    var previewRect by remember { mutableStateOf<RectF?>(null) }
    var localZoom by remember { mutableStateOf(zoomScale) }
    var localOX by remember { mutableStateOf(zoomOX) }
    var localOY by remember { mutableStateOf(zoomOY) }

    // Sync with parent zoom state
    if (zoomScale != localZoom || zoomOX != localOX || zoomOY != localOY) {
        localZoom = zoomScale; localOX = zoomOX; localOY = zoomOY
    }

    fun toNorm(sx: Float, sy: Float): Pair<Float, Float> {
        val nx = ((sx - localOX) / (localZoom * canvasSize.width)).coerceIn(0f, 1f)
        val ny = ((sy - localOY) / (localZoom * canvasSize.height)).coerceIn(0f, 1f)
        return nx to ny
    }

    Canvas(Modifier.fillMaxSize().padding(8.dp).onSizeChanged { canvasSize = it }
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                localZoom = (localZoom * zoom).coerceIn(0.5f, 4f)
                localOX += pan.x; localOY += pan.y
                onZoomChange(localZoom, localOX, localOY)
            }
        }
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    val (nx, ny) = toNorm(offset.x, offset.y)
                    val tapped = regions.findLast { it.rect.contains(nx, ny) }
                    if (tapped != null) { onRegionSelected(tapped.id); previewRect = null }
                    else { onRegionSelected(null); previewRect = RectF(nx, ny, nx, ny) }
                },
                onDrag = { change, _ ->
                    previewRect?.let {
                        val (nx, ny) = toNorm(change.position.x, change.position.y)
                        previewRect = RectF(minOf(it.left, nx), minOf(it.top, ny), maxOf(it.left, nx), maxOf(it.top, ny))
                    }; change.consume()
                },
                onDragEnd = {
                    previewRect?.let { r -> if (r.width() > 0.02f && r.height() > 0.02f) onRegionCreated(r) }
                    previewRect = null
                },
                onDragCancel = { previewRect = null }
            )
        }
    ) {
        val s = localZoom; val ox = localOX; val oy = localOY
        drawImage(bitmap.asImageBitmap(),
            dstOffset = IntOffset(ox.toInt(), oy.toInt()),
            dstSize = IntSize((size.width * s).toInt(), (size.height * s).toInt()))

        for (r in regions) {
            val rect = r.rect
            val l = rect.left * size.width * s + ox; val t = rect.top * size.height * s + oy
            val w = rect.width() * size.width * s; val h = rect.height() * size.height * s
            val sel = r.id == selectedId
            val accent = if (sel) StampTeal else GradeRed
            drawRect(accent.copy(alpha = 0.08f), Offset(l, t), Size(w, h))
            drawRect(accent, Offset(l, t), Size(w, h), style = Stroke(if (sel) 3f else 2f))
            // Label
            val label = r.label.displayName
            val tp = Paint().apply { color = 0xFF1E1A16.toInt(); textSize = 26f; isFakeBoldText = true }
            val tw = tp.measureText(label); val ph = 24f; val pp = 8f
            val py = if (t > ph + 8f) t - ph - 4f else t + h + 4f
            drawRoundRect(CardWhite, Offset(l, py), Size(tw + pp * 2, ph), CornerRadius(5f, 5f))
            drawRect(accent.copy(alpha = 0.4f), Offset(l, py), Size(tw + pp * 2, ph), style = Stroke(1f))
            drawContext.canvas.nativeCanvas.drawText(label, l + pp, py + ph - 6f, tp)
        }
        previewRect?.let { pr ->
            drawRect(Amber.copy(alpha = 0.1f), Offset(pr.left * size.width * s + ox, pr.top * size.height * s + oy),
                Size(pr.width() * size.width * s, pr.height() * size.height * s))
            drawRect(Amber, Offset(pr.left * size.width * s + ox, pr.top * size.height * s + oy),
                Size(pr.width() * size.width * s, pr.height() * size.height * s), style = Stroke(2f))
        }
    }
}

@Composable
private fun CompactBtn(text: String, enabled: Boolean, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Box(Modifier.clip(RoundedCornerShape(10.dp))
        .background(if (enabled) color.copy(alpha = 0.1f) else Color.Transparent)
        .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(text, color = if (enabled) color else InkFaint, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
