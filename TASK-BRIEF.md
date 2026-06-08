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

| Phase | Scope | Key deliverable |
|-------|-------|-----------------|
| 0 | Project init | CLAUDE.md, Gradle skeleton, CI, GitHub repo |
| 1 | Data layer TDD | Room entities, DAOs, Repository — all test-driven |
| 2 | Capture + OCR | CameraX, region selection UI, ML Kit integration |
| 3 | Parse + Edit | Structured parser, correction UI, save to DB |
| 4 | Stats + Export | List/detail screens, statistics, CSV export |
| 5 | Polish | Compose UI tests, bug fixes, docs |
