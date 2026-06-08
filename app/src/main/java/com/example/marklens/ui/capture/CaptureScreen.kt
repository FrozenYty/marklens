package com.example.marklens.ui.capture

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.marklens.ocr.OcrRegion
import com.example.marklens.ocr.RegionLabel

@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel,
    onPhotoCaptured: (Bitmap) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            CaptureBottomBar(
                onCapture = onPhotoCaptured,
                onClearRegions = viewModel::clearRegions
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (uiState.capturedBitmap != null) {
                RegionOverlay(
                    bitmap = uiState.capturedBitmap!!,
                    regions = uiState.regions,
                    selectedId = uiState.selectedRegionId,
                    onRegionCreated = { rect ->
                        viewModel.addRegion(OcrRegion(label = RegionLabel.CUSTOM, rect = rect))
                    },
                    onRegionSelected = { id -> viewModel.selectRegion(id) },
                    onRegionDeleted = { id -> viewModel.deleteRegion(id) }
                )
            } else {
                Text(
                    "No image selected",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
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
    onRegionDeleted: (String) -> Unit
) {
    var canvasSize by remember { mutableStateOf(IntSize(1, 1)) }
    // Preview rect during drag
    var previewRect by remember { mutableStateOf<RectF?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (canvasSize.width <= 0) return@detectDragGestures
                        val normX = (offset.x / canvasSize.width).coerceIn(0f, 1f)
                        val normY = (offset.y / canvasSize.height).coerceIn(0f, 1f)
                        val tapped = regions.findLast { it.rect.contains(normX, normY) }
                        if (tapped != null) {
                            onRegionSelected(tapped.id)
                            previewRect = null
                        } else {
                            previewRect = RectF(normX, normY, normX, normY)
                        }
                    },
                    onDrag = { change, _ ->
                        previewRect?.let { rect ->
                            val nx = (change.position.x / canvasSize.width).coerceIn(0f, 1f)
                            val ny = (change.position.y / canvasSize.height).coerceIn(0f, 1f)
                            previewRect = RectF(
                                minOf(rect.left, nx), minOf(rect.top, ny),
                                maxOf(rect.left, nx), maxOf(rect.top, ny)
                            )
                        }
                        change.consume()
                    },
                    onDragEnd = {
                        previewRect?.let { rect ->
                            if (rect.width() > 0.01f && rect.height() > 0.01f) {
                                onRegionCreated(rect)
                            }
                        }
                        previewRect = null
                    },
                    onDragCancel = { previewRect = null }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Captured photo as background
            drawImage(
                image = bitmap.asImageBitmap(),
                dstSize = Size(size.width, size.height)
            )

            // Existing regions
            for (region in regions) {
                val left = region.rect.left * size.width
                val top = region.rect.top * size.height
                val w = region.rect.width() * size.width
                val h = region.rect.height() * size.height
                val color = if (region.id == selectedId) Color.Green else Color(0xFFFF4444)
                val strokeW = if (region.id == selectedId) 4f else 2f
                drawRect(color, Offset(left, top), Size(w, h), style = Stroke(strokeW))
                // Label text
                drawContext.canvas.nativeCanvas.drawText(
                    region.label.displayName,
                    left, top - 8f,
                    android.graphics.Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 36f
                        isFakeBoldText = true
                        setShadowLayer(4f, 1f, 1f, android.graphics.Color.BLACK)
                    }
                )
            }

            // Preview rect during drag
            previewRect?.let { rect ->
                drawRect(
                    Color.Yellow,
                    Offset(rect.left * size.width, rect.top * size.height),
                    Size(rect.width() * size.width, rect.height() * size.height),
                    style = Stroke(3f)
                )
            }
        }
    }
}

@Composable
private fun CaptureBottomBar(
    onCapture: (Bitmap) -> Unit,
    onClearRegions: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Button(onClick = onClearRegions, Modifier.fillMaxWidth()) {
            Text("Clear All Regions")
        }
    }
}
