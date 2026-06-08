package com.example.marklens.ui.capture

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel
import com.example.marklens.ui.theme.Amber
import com.example.marklens.ui.theme.Ink
import com.example.marklens.ui.theme.InkTranslucent
import com.example.marklens.ui.theme.MarkRed
import com.example.marklens.ui.theme.Paper
import com.example.marklens.ui.theme.Slate
import com.example.marklens.ui.theme.SoftGreen
import com.example.marklens.ui.theme.SurfaceWhite

@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onPhotoCaptured: (Bitmap) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
    ) {
        when {
            uiState.capturedBitmap != null -> {
                RegionOverlay(
                    bitmap = uiState.capturedBitmap!!,
                    regions = uiState.regions,
                    selectedId = uiState.selectedRegionId,
                    onRegionCreated = { rect ->
                        viewModel.addRegion(OcrRegion(label = RegionLabel.CUSTOM, rect = rect))
                    },
                    onRegionSelected = { id -> viewModel.selectRegion(id) },
                    onRegionDeleted = { id -> viewModel.deleteRegion(id) },
                    onClear = viewModel::clearRegions,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                // Empty state
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "📷",
                        fontSize = 48.sp
                    )
                    Text(
                        "No image selected",
                        color = Slate,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Capture or select a photo to begin",
                        color = Slate.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Top bar — region count badge
        if (uiState.regions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(InkTranslucent)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${uiState.regions.size}",
                    color = SurfaceWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text("regions", color = Slate, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun RegionOverlay(
    bitmap: Bitmap,
    regions: List<OcrRegion>,
    selectedId: String?,
    onRegionCreated: (RectF) -> Unit,
    onRegionSelected: (String) -> Unit,
    onRegionDeleted: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }
    var previewRect by remember { mutableStateOf<RectF?>(null) }
    val hasRegions = regions.isNotEmpty()

    // Pulse animation for empty-state draw hint
    val pulseAlpha by rememberInfiniteTransition().animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse)
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val nx = (offset.x / canvasSize.width).coerceIn(0f, 1f)
                            val ny = (offset.y / canvasSize.height).coerceIn(0f, 1f)
                            val tapped = regions.findLast { it.rect.contains(nx, ny) }
                            if (tapped != null) {
                                onRegionSelected(tapped.id)
                                previewRect = null
                            } else {
                                previewRect = RectF(nx, ny, nx, ny)
                            }
                        },
                        onDrag = { change, _ ->
                            previewRect?.let {
                                val nx = (change.position.x / canvasSize.width).coerceIn(0f, 1f)
                                val ny = (change.position.y / canvasSize.height).coerceIn(0f, 1f)
                                previewRect = RectF(
                                    minOf(it.left, nx), minOf(it.top, ny),
                                    maxOf(it.left, nx), maxOf(it.top, ny)
                                )
                            }
                            change.consume()
                        },
                        onDragEnd = {
                            previewRect?.let { r ->
                                if (r.width() > 0.02f && r.height() > 0.02f) {
                                    onRegionCreated(r)
                                }
                            }
                            previewRect = null
                        },
                        onDragCancel = { previewRect = null }
                    )
                }
        ) {
            // Background photo
            drawImage(bitmap.asImageBitmap(), dstSize = Size(size.width, size.height))

            // Empty-state hint — pulsing crosshair
            if (!hasRegions) {
                val cx = size.width / 2
                val cy = size.height / 2
                val len = 40f
                drawLine(Color.White.copy(alpha = pulseAlpha), Offset(cx - len, cy), Offset(cx + len, cy), 2f)
                drawLine(Color.White.copy(alpha = pulseAlpha), Offset(cx, cy - len), Offset(cx, cy + len), 2f)
                drawCircle(Color.White.copy(alpha = pulseAlpha * 0.5f), 12f, Offset(cx, cy))
            }

            // Existing regions
            for (region in regions) {
                val rect = region.rect
                val l = rect.left * size.width
                val t = rect.top * size.height
                val w = rect.width() * size.width
                val h = rect.height() * size.height

                val isSelected = region.id == selectedId
                val color = if (isSelected) SoftGreen else MarkRed
                val strokeW = if (isSelected) 3f else 2f

                // Semi-transparent fill
                drawRect(
                    color.copy(alpha = 0.12f),
                    Offset(l, t), Size(w, h)
                )
                // Stroke border
                drawRect(
                    color, Offset(l, t), Size(w, h),
                    style = Stroke(strokeW)
                )
                // Corner accents (selected only)
                if (isSelected) {
                    val corner = 16f
                    listOf(Offset(l, t), Offset(l + w, t), Offset(l, t + h), Offset(l + w, t + h))
                        .forEach { c ->
                            drawRect(color, c - Offset(4f, 4f), Size(corner, 4f))
                            drawRect(color, c - Offset(4f, 4f), Size(4f, corner))
                        }
                }
                // Label pill
                val label = region.label.displayName
                val textPaint = android.graphics.Paint().apply {
                    this.color = android.graphics.Color.WHITE
                    textSize = 32f
                    isFakeBoldText = true
                    setShadowLayer(3f, 0f, 1f, android.graphics.Color.BLACK)
                }
                val textW = textPaint.measureText(label)
                val pillPad = 12f
                drawRoundRect(
                    Color.Black.copy(alpha = 0.65f),
                    Offset(l, t - 36f),
                    Size(textW + pillPad * 2, 32f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
                drawContext.canvas.nativeCanvas.drawText(
                    label, l + pillPad, t - 10f, textPaint
                )
            }

            // Preview rect during drag
            previewRect?.let { r ->
                drawRect(
                    Amber.copy(alpha = 0.15f),
                    Offset(r.left * size.width, r.top * size.height),
                    Size(r.width() * size.width, r.height() * size.height)
                )
                drawRect(
                    Amber,
                    Offset(r.left * size.width, r.top * size.height),
                    Size(r.width() * size.width, r.height() * size.height),
                    style = Stroke(2f)
                )
            }
        }

        // Bottom action bar
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(InkTranslucent)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionChip(
                label = "Clear",
                color = MarkRed,
                onClick = onClear,
                enabled = hasRegions
            )
            // Region count
            Text(
                if (hasRegions) "${regions.size} region${if (regions.size > 1) "s" else ""}"
                else "Drag to create region",
                color = if (hasRegions) SurfaceWhite else Slate,
                fontSize = 13.sp,
                modifier = Modifier.weight(1f)
            )
            ActionChip(
                label = "Label",
                color = SoftGreen,
                onClick = {
                    selectedId?.let { id ->
                        val current = regions.find { it.id == id }
                        if (current != null) {
                            val labels = RegionLabel.entries
                            val nextIdx = (labels.indexOf(current.label) + 1) % labels.size
                            // Cycle label — handled in ViewModel
                        }
                    }
                },
                enabled = selectedId != null
            )
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    color: Color,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = if (enabled) 0.25f else 0.1f),
            contentColor = if (enabled) color else Slate,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Slate.copy(alpha = 0.3f)
        ),
        modifier = Modifier.height(36.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 0.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
