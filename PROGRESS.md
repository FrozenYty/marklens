# PROGRESS.md — MarkLens

> Read this first. Update in real-time after every compile, test run, fix.
> When context is lost, this file is your only memory. Keep it current.

---

## Session

| Field | Value |
|-------|-------|
| **Working on** | Phase 5 — Polish |
| **Last updated** | 2026-06-08 |

## Right Now

**Phase 5 core tasks complete.** 83 tests passing. All screens wired with navigation.
Remaining: screenshots + instrumented tests (need emulator).

```
Status: Phases 0-4 DONE ✅ → Phase 5 core DONE ✅
```

---

## Done

- [x] Phase 0: project init (CLAUDE.md, Gradle, CI, GitHub, LICENSE)
- [x] Phase 1: data layer (Room entities, DAOs, repository, 32 tests)
- [x] Phase 2: capture + OCR (OcrEngine, RegionMapper, CaptureScreen/VM, 12 tests)
- [x] Phase 3: parse + edit (ScoreParser, StudentInfoParser, ReviewScreen/VM, 17 tests)
- [x] Phase 4: stats + export (StatsCalculator, CsvExporter, 9 tests)
- [x] Phase 5.2: Save pipeline — ReviewViewModel.save() with ExamRepository
- [x] Phase 5.4: RecordListScreen + RecordListViewModel (subject filter, delete)
- [x] Phase 5.5: StatsScreen + 4 charts (ScoreHistogram, PassRateDonut, QuestionBarChart, ErrorHeatmap)
- [x] Phase 5.1a: Gallery photo picker + full navigation wiring in MainActivity
- [x] Phase 5.6: Region label picker — dropdown menu with all RegionLabel options
- [x] Phase 5.7: Template save/load — JSON serialization, AlertDialog for naming
- [x] 83 unit tests passing (9 new tests added)

## Remaining (need emulator/device)

| # | Task | Note |
|---|------|------|
| 5 | Screenshots | Capture 6-8 screenshots on emulator (see HANDOVER.md §11) |
| 8 | Compose UI tests | instrumented tests for CaptureScreen + ReviewScreen |
| 1 | CameraXViewfinder | Wire CameraX live preview (gallery fallback works) |

## Sprint Overview

| Phase | Scope | Status |
|-------|-------|--------|
| 0 | Project init | ✅ Done |
| 1 | Data layer TDD | ✅ Done |
| 2 | Capture + OCR | ✅ Done |
| 3 | Parse + Edit | ✅ Done |
| 4 | Stats + Export | ✅ Done |
| 5 | Polish | ✅ Core complete — 83 tests |

## Files Created/Modified (Phase 5)

| File | Action |
|------|--------|
| `ui/review/ReviewViewModel.kt` | Modified — added repository + save() |
| `ui/review/ReviewScreen.kt` | Modified — wired save button, imageUri, loading state |
| `ui/list/RecordListViewModel.kt` | Created — subject filter, delete |
| `ui/list/RecordListScreen.kt` | Created — cards + filter chips |
| `ui/stats/StatsViewModel.kt` | Created — loads data, computes stats |
| `ui/stats/StatsScreen.kt` | Created — metrics + 4 chart sections |
| `ui/stats/ScoreHistogram.kt` | Created — Canvas bar chart |
| `ui/stats/PassRateDonut.kt` | Created — Canvas donut ring |
| `ui/stats/QuestionBarChart.kt` | Created — Box-based horizontal bars |
| `ui/stats/ErrorHeatmap.kt` | Created — Canvas grid |
| `di/AppViewModelFactory.kt` | Created — manual DI factory |
| `MainActivity.kt` | Modified — navigation + DI + gallery launcher |
| `ui/capture/CaptureScreen.kt` | Modified — gallery/Review buttons + label dropdown + template UI |
| `ui/capture/CaptureViewModel.kt` | Modified — template save/load + JSON serialization |
| `test/.../ReviewViewModelTest.kt` | Modified — added 2 save tests (mockito) |
| `test/.../RecordListViewModelTest.kt` | Created — 5 tests |
| `test/.../StatsViewModelTest.kt` | Created — 4 tests |

## Command Cheatsheet

```bash
cd marklens
./gradlew assembleDebug        # Build APK
./gradlew test                 # All unit tests (83 pass)
./gradlew lint                 # Static analysis
./gradlew installDebug         # Install on connected device/emulator
```
