# Phase 2 — Capture & OCR

**Date**: 2026-06-08
**Author**: Tianyu Yao
**Status**: In progress (region logic done, Camera UI next)

---

## Overview

Phase 2 implements the image capture pipeline: CameraX photo, OCR region
selection overlay, and ML Kit text recognition with spatial mapping.

## Files Created

### Main Source

| File | Type | Description |
|------|------|-------------|
| `ocr/OcrRegion.kt` | Model | Region data class, RegionLabel enum, normalized [0,1] coords |
| `ocr/RegionMapper.kt` | Logic | Maps OCR TextBlocks to OcrRegions by spatial intersection |
| `ocr/OcrEngine.kt` | Integration | ML Kit TextRecognizer wrapper, `Task.await()` via coroutines |
| `ui/capture/CaptureViewModel.kt` | ViewModel | Photo + region CRUD state management |

### Tests (12 methods)

| File | Tests | Key scenarios |
|------|-------|--------------|
| `ocr/RegionMapperTest.kt` | 5 | empty regions, empty blocks, single block, multiple blocks, multiple regions, overlapping regions |
| `ui/CaptureViewModelTest.kt` | 7 | initial state, setPhoto, addRegion, deleteRegion, moveRegion, changeLabel, clearRegions |

## Architecture

```
CameraXViewfinder (Compose)
        │
        ▼
Bitmap ──→ CaptureViewModel (regions state)
        │
        ▼
OcrEngine.recognize(bitmap) ──→ ML Kit Text
        │
        ▼
RegionMapper.mapBlocksToRegions(blocks, regions) ──→ List<OcrRegion>
        │
        ▼
CaptureViewModel (rawText filled)
```

## Design Decisions

1. **Normalized coordinates**: Region rects use [0,1] relative to image dimensions.
   Handles rotation, different resolutions, and device variations transparently.

2. **TextBlock abstraction**: Instead of coupling RegionMapper to ML Kit's
   `Text.TextBlock`, we define a simple `TextBlock(text, boundingBox)` data class.
   This makes RegionMapper a pure unit-testable function.

3. **Coroutines + Google Tasks**: `kotlinx-coroutines-play-services` provides
   `Task.await()` to bridge Google Play Services' `Task<T>` with Kotlin coroutines.

4. **Region label selection**: RegionLabel enum drives a dropdown when adding new
   regions. Labels include STUDENT_NAME, STUDENT_ID, CLASS_NAME, SUBJECT,
   QUESTION_SCORE, TOTAL_SCORE, and CUSTOM.

## Dependencies Added

| Library | Version | Purpose |
|---------|---------|---------|
| kotlinx-coroutines-play-services | 1.9.0 | `Task.await()` for ML Kit |

## Lessons Learned

| # | Pitfall | Resolution |
|---|---------|------------|
| 1 | Tests using `android.graphics.RectF`/`Bitmap` fail on JVM without Robolectric | Always add `@RunWith(RobolectricTestRunner)` when the class touches any `android.graphics.*` type, even if no DB or Context is involved |
| 2 | `MutableStateFlow.update {}` is synchronous — no coroutine dispatcher needed | Don't use `Dispatchers.setMain()` for ViewModel tests unless the ViewModel launches coroutines explicitly |
| 3 | Two overlapping OCR regions both capture the same TextBlock with independent `filter{}` | Use `BooleanArray` index tracking + `for` loop for first-match-wins semantics |
| 4 | Google Play Services `Task<T>` needs `kotlinx-coroutines-play-services` for `Task.await()` | Dependency added: `org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0` |

## UI Layer Notes

| # | Caveat | Detail |
|---|--------|--------|
| 5 | Canvas label drawing uses `nativeCanvas.drawText()` | Compose Canvas has no built-in text API — use `drawContext.canvas.nativeCanvas` for pill-shaped region labels |
| 6 | `detectDragGestures` for region creation | onDragStart (tap=select, empty=start), onDrag (update preview), onDragEnd (finalize if > 2% of image) |
| 7 | Normalized coords [0,1] ↔ pixels | Multiply by canvas size for display; divide by canvas size for normalization. Coerce to [0,1] |
| 8 | Camera preview not tested in CI | CameraX + Compose UI requires device/emulator; unit tests cover ViewModel logic only |

## Design System

| Token | Color | Usage |
|-------|-------|-------|
| `Ink` | #1C1C2E | Dark canvas background, immersive feel |
| `Paper` | #FAF8F5 | Light surfaces, exam-paper warmth |
| `MarkRed` | #E63946 | Region box stroke, grading accent |
| `SoftGreen` | #2A9D8F | Selected region highlight |
| `Amber` | #F4A261 | Drag preview overlay |
| `Slate` | #64748B | Secondary text, disabled state |
| `InkTranslucent` | #CC1C1C2E | Semi-transparent overlay bars |

Direction: *Industrial Precision × Academic Warmth* — dark immersive canvas with paper-white dialogs, red/green marking colors, pulsing "+" drag hint on empty state.

## Next

- CameraX + Compose capture screen with region drawing overlay
- Template save/load integration with RegionTemplateDao
- Phase 2 pipeline integration test (photo → OCR → regions)
