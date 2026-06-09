# PROGRESS.md — MarkLens

> **Handoff document.** Read this first to understand what's built, what's broken,
> and what needs doing. Updated 2026-06-10.

---

## Quick Status

```
Build: ✅ assembleDebug passes
Tests: ✅ 81 pass / 0 fail
APK:   ✅ installs and runs on emulator
OCR:   ✅ full-page OCR + block mapping — tested on 200 papers, 100% success rate
```

---

## 1. What's Built

### Data Layer
| Component | Status |
|-----------|--------|
| Room DB — 4 entities (Student, ExamRecord, QuestionScore, RegionTemplate) | ✅ |
| 4 DAOs with CRUD + query methods | ✅ |
| ExamRepository — getOrCreateStudent, saveExamWithScores, deleteRecord | ✅ |
| RegionTemplateDao — insert, update, getByName, delete | ✅ |

### Navigation
| Route | Flow |
|-------|------|
| **Home → Scan Paper** | Pick template → gallery photo → OCR → Review (verify/correct) → Save → Records |
| **Home → Templates** | List templates → Edit (modify regions) or Delete (with confirmation) |
| **Home → Records** | List saved exam records → tap to review/edit → Stats per subject |

### Screens
| Screen | Purpose | Theme |
|--------|---------|-------|
| Home | 3-button entry (Scan Paper / Templates / Records) | ✅ PaperCream |
| TemplateListScreen | List templates, Edit/Delete, + New Template | ✅ PaperCream |
| TemplateEditorScreen | Photo + region drawing + labels + save | ✅ PaperCream |
| RecordListScreen | Saved records list, subject filter, CSV export | ✅ PaperCream |
| ReviewScreen | Verify/correct OCR results, original photo preview | ✅ PaperCream |
| StatsScreen | 4 charts (histogram, donut, bar, heatmap) | ✅ PaperCream |

### OCR Pipeline
```
Gallery photo → ML Kit full-page OCR → RegionMapper (spatial block mapping)
→ StudentInfoParser / ScoreParser → ReviewScreen (verify) → save to Room
```

### Templates
- Create: draw regions on photo → label each → name → save (duplicate name rejected)
- Edit: load template → modify regions → save (updates existing)
- Delete: confirmation dialog
- Undo/Redo: dual-stack undo/redo for region add/delete

### Stats Charts
- ScoreHistogram — Canvas bar chart
- PassRateDonut — Canvas ring with centered percentage
- QuestionBarChart — Horizontal bars (Box-based)
- ErrorHeatmap — Canvas grid of wrong-answer counts

---

## 2. Resolved Issues

| # | Issue | Resolution |
|---|-------|-----------|
| C1 | OCR accuracy near zero | Full-page OCR + RegionMapper block mapping |
| C2 | No full-page OCR fallback | `runScanOcr` uses `recognizeBlocks()` + `RegionMapper` |
| H1 | ReviewScreen old dark theme | Migrated to PaperCream theme |
| H2 | No photo on ReviewScreen | AsyncImage (Coil) displays original photo |
| H3 | 4 unit tests fail | Stubbed `studentDao.getAllOnce()` |
| H4 | Editor doesn't pre-fill template name | `existingTemplateName` parameter |
| M4 | CSV export not wired | RecordListScreen → FileProvider share intent |
| M6 | Build warnings (unnecessary casts) | Removed |
| L1 | RegionMapper dead code | Now used in `runScanOcr()` |

---

## 3. Remaining Issues

| # | Issue | Priority |
|---|-------|----------|
| C3 | Template regions don't adapt to different photo framing | Medium |
| M1 | No CameraX — gallery picker only | Low |
| M2 | No handwriting OCR (Cloud Vision) | Low |
| M3 | No batch scanning loop | Low |
| M5 | No landscape layout support | Low |

---

## 4. Project Structure

```
marklens/
├── app/src/main/java/com/example/marklens/
│   ├── MainActivity.kt              — Navigation + OCR flow
│   ├── data/
│   │   ├── ExamRepository.kt        — Data access layer
│   │   ├── MarkLensDatabase.kt      — Room DB (v1, 4 entities)
│   │   ├── dao/                      — ExamRecord, QuestionScore, RegionTemplate, Student
│   │   └── entity/                   — Room entities
│   ├── di/AppViewModelFactory.kt     — Manual DI
│   ├── ocr/
│   │   ├── OcrEngine.kt             — ML Kit wrapper
│   │   ├── OcrProvider.kt           — Pluggable interface + MlKitOcrProvider
│   │   ├── OcrRegion.kt             — Region data class + RegionLabel enum
│   │   └── RegionMapper.kt          — Block-to-region spatial mapper
│   ├── parser/
│   │   ├── ScoreParser.kt           — Table + single score parsing
│   │   └── StudentInfoParser.kt     — Name/ID/class extraction
│   ├── ui/
│   │   ├── editor/TemplateEditorScreen.kt
│   │   ├── list/RecordListScreen.kt + RecordListViewModel.kt
│   │   ├── review/ReviewScreen.kt + ReviewViewModel.kt
│   │   ├── stats/StatsScreen.kt + StatsViewModel.kt + 4 chart composables
│   │   ├── templates/TemplateListScreen.kt
│   │   └── theme/Theme.kt
│   └── util/
│       ├── CsvExporter.kt
│       └── StatsCalculator.kt
├── app/src/test/java/               — 13 test files, 81 tests
├── app/src/androidTest/java/        — ScreenTests.kt (instrumented)
├── screenshots/                     — 8 screenshots for README
├── test-papers/                     — 200 generated exam papers (PNG)
├── test-docs/test-plan.md           — Test strategy document
├── generate_papers.py               — Python script to generate test papers
└── gradle/libs.versions.toml        — Version catalog
```

---

## 5. Commands

```bash
./gradlew assembleDebug        # Build APK
./gradlew test                 # Unit tests (81 pass)
./gradlew installDebug         # Install on emulator/device
python generate_papers.py      # Generate 200 test papers
```
