# Task Brief — MarkLens

Exam paper digitization app. Photograph paper exam sheets, extract student
info and scores via OCR with user-defined region selection, and review
structured results with statistics.

## Core Pipeline

```
Photo → Region Selection → OCR → Structured Parse → Manual Correction → Statistics
```

## Feature Modules

### Module 1: Capture & Region Selection

| Feature | Detail |
|---------|--------|
| Camera capture | CameraX, take photo of exam paper |
| Gallery import | Select existing photo from device |
| Region selection | User draws bounding boxes on the image, labels each region (name, student ID, question 1 score, total score, etc.) |
| Region presets | Save/load region templates for repeated use (same exam format) |

### Module 2: OCR & Parsing

| Feature | Detail |
|---------|--------|
| Text recognition | Google ML Kit Text Recognition v16, offline |
| Region-to-text | Crop image by selected regions → OCR each region → raw text per field |
| Structured parsing | Regex + heuristics to map raw text → `ExamRecord` (student info + question scores + total) |

### Module 3: Review & Edit

| Feature | Detail |
|---------|--------|
| Result review screen | Show parsed fields side-by-side with cropped region images |
| Manual correction | Edit any misrecognized field inline |
| Save to database | Room persistence |

### Module 4: Browse & Statistics

| Feature | Detail |
|---------|--------|
| Record list | All saved exam records, filter by class/subject |
| Record detail | Full view of one record: student info, all question scores, original photo |
| Class statistics | Average, max, min, pass rate, score distribution chart |
| Per-question analysis | Average score per question, error concentration (which questions most people got wrong) |
| Export CSV | Export records as CSV file for external analysis |

## Tech Stack

| Layer | Choice |
|-------|--------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository |
| Database | Room 2.8 |
| OCR | Google ML Kit Text Recognition v16 (offline) |
| Camera | CameraX 1.5 + camera-compose |
| Build | Gradle Kotlin DSL + Version Catalog |
| Min SDK | 26 |
| Target SDK | 36 |
| Language | Kotlin 2.3 |

## Phases

| Phase | Scope | Key deliverable | Status |
|-------|-------|-----------------|--------|
| 0 | Project init | CLAUDE.md, Gradle skeleton, CI, GitHub repo | ✅ Done |
| 1 | Data layer TDD | Room entities, DAOs, Repository — all test-driven | ✅ Done |
| 2 | Capture + OCR | Region selection UI, ML Kit integration, full-page OCR | ✅ Done |
| 3 | Parse + Edit | Structured parser, correction UI, save to DB | ✅ Done |
| 4 | Stats + Export | List/detail screens, statistics, CSV export | ✅ Done |
| 5 | Polish | Integration tests, bug fixes, docs, screenshots | ✅ Done |

## Phase 5 — Polish (Completed)

| # | Task | Status |
|---|------|--------|
| 1 | CameraX integration | ❌ Removed — gallery picker only |
| 2 | Save pipeline — ReviewScreen → ExamRepository | ✅ Done |
| 3 | Screenshots | ✅ Done (8 screenshots) |
| 4 | RecordListScreen | ✅ Done |
| 5 | StatsScreen — 4 charts | ✅ Done |
| 6 | Region label picker — dropdown | ✅ Done |
| 7 | Template save/load | ✅ Done (with update + duplicate check) |
| 8 | Compose UI tests | ⚠️ Partial (ScreenTests.kt exists, not CI-verified) |
| 9 | Full-page OCR + block mapping | ✅ Done |
| 10 | ScoreParser table mode | ✅ Done |
| 11 | CSV export wired to UI | ✅ Done |
| 12 | Undo/Redo dual-stack | ✅ Done |
| 13 | 200 test papers + E2E validation | ✅ Done (100% pass rate) |
