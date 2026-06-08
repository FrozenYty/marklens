# Handover Guide — MarkLens

**To**: Jianheng Sun (@chemflowers)
**From**: Tianyu Yao (@FrozenYty)
**Date**: 2026-06-08

---

## 1. Project Status

| Phase | Status | Tests | Key Files |
|-------|--------|-------|-----------|
| 0 — Init | ✅ Done | — | Gradle, CI, docs |
| 1 — Data Layer | ✅ Done | 32 | Room entities, DAOs, Repository |
| 2 — Capture + OCR | ✅ Done | 12 | OcrEngine, RegionMapper, CaptureScreen |
| 3 — Parse + Edit | ✅ Done | 17 | ScoreParser, StudentInfoParser, ReviewScreen |
| 4 — Stats + Export | ✅ Done | 9 | StatsCalculator, CsvExporter |
| 5 — Polish | 🔜 You | — | Integration, bugs, docs |

**Total: 73 unit tests passing (all green on CI).**

---

## 2. Architecture at a Glance

```
Compose UI        ViewModel         Repository          Room DB
─────────        ─────────         ──────────          ───────
CaptureScreen →  CaptureViewModel
                            ↘
ReviewScreen   →  ReviewViewModel →  ExamRepository →  SQLite
                            ↗
StatsScreen    →  (not yet)                   ↑
                                        4 DAOs
```

### Package Map

```
com.example.marklens/
├── data/
│   ├── entity/     Student, ExamRecord, QuestionScore, RegionTemplate
│   ├── dao/        4 DAO interfaces (all suspend, Flow for queries)
│   ├── MarkLensDatabase.kt    Room DB, version 1
│   └── ExamRepository.kt      Business logic entry point
├── ocr/
│   ├── OcrRegion.kt           Region data model, RegionLabel enum
│   ├── OcrEngine.kt           ML Kit TextRecognizer wrapper
│   └── RegionMapper.kt        OCR blocks → regions (first-match-wins)
├── parser/
│   ├── ScoreParser.kt         "8/10" → QuestionScore
│   └── StudentInfoParser.kt   OCR text → name/ID/class
├── ui/
│   ├── capture/               CaptureScreen + CaptureViewModel
│   ├── review/                ReviewScreen + ReviewViewModel
│   └── theme/Theme.kt         7-token color system
└── util/
    ├── StatsCalculator.kt     Averages, pass rate, distribution
    └── CsvExporter.kt         Records → CSV string
```

---

## 3. Design System

> **Full spec**: [test-docs/design-spec.md](test-docs/design-spec.md) — color tokens, typography, spacing scale, component patterns, chart colors, state handling, checklist. Read it before writing any UI code.

### Quick Reference

| Token | Hex | Usage |
|-------|-----|-------|
| `Ink` | `#1C1C2E` | Dark canvas background |
| `Paper` | `#FAF8F5` | Light surfaces / cards |
| `MarkRed` | `#E63946` | Region boxes / destructive actions |
| `SoftGreen` | `#2A9D8F` | Selected regions / confirm actions |
| `Amber` | `#F4A261` | Drag preview |
| `Slate` | `#64748B` | Secondary text / disabled |
| `InkTranslucent` | `#CC1C1C2E` | Semi-transparent overlays |
| `SurfaceWhite` | `#FFFFFF` | Cards, text fields |

**Direction**: *Industrial Precision × Academic Warmth*. Dark canvas, paper-white cards, red/green marking. All colors from `com.example.marklens.ui.theme`.

---

## 4. Testing Standards

### Running Tests

```bash
cd marklens

# All unit tests (runs in CI on every push)
./gradlew test

# Specific test class
./gradlew test --tests "com.example.marklens.data.dao.StudentDaoTest"

# Full build check (CI does this too)
./gradlew lint test assembleDebug
```

### Test File Locations

| Test Type | Source Set | Runner |
|-----------|-----------|--------|
| DAO / Repository / Parser | `app/src/test/` | `@RunWith(RobolectricTestRunner)` |
| ViewModel (pure logic) | `app/src/test/` | No runner needed |
| Compose UI | `app/src/androidTest/` | `AndroidJUnit4` (needs emulator) |
| Camera / ML Kit | Manual | On-device only |

### Required Runner Rules

```
Any test using android.graphics.* types (RectF, Bitmap, Canvas)
  → MUST have @RunWith(RobolectricTestRunner)

Any test using Room.inMemoryDatabaseBuilder
  → MUST have @RunWith(RobolectricTestRunner)
  → FK parent rows MUST be created in @Before using runBlocking
  → app/src/test/resources/robolectric.properties MUST set sdk=34
```

### Test Naming

```
File:   <TestedClass>Test.kt
Method: methodName_scenario_expectedBehavior

Example:
  StudentDaoTest.kt
  insert_shouldReturnNonZeroId
  getByStudentId_notFound_shouldReturnNull
```

### Every Test Class Must Have

```kotlin
@RunWith(RobolectricTestRunner::class)  // if touching android.* or Room
@Test fun ...                           // org.junit.Test (JUnit 4 style for vintage engine)
assertEquals(...)                       // org.junit.jupiter.api.Assertions
```

---

## 5. Code Conventions

| Rule | Detail |
|------|--------|
| Language | **English only** — code, comments, docs, commits |
| @author | Every Kotlin file: `@author Your Name` in KDoc |
| Commit | `<type>: <short description>\n\nAuthor: Your Name` |
| Types | `feat`, `fix`, `test`, `docs`, `refactor`, `chore` |
| Package | `com.example.marklens` |
| Don't modify | `app/src/main/` except new feature files |
| Don't use | `git add -A` or `git add .` — stage files individually |
| Always | `git pull` before committing. Push directly to `main`. |

---

## 6. Development Setup

```bash
# 1. Clone
git clone git@github.com:FrozenYty/marklens.git
cd marklens

# 2. JDK 21
export JAVA_HOME=<path-to-jdk-21>

# 3. Android SDK
export ANDROID_HOME=<path-to-android-sdk>

# 4. Verify
./gradlew test    # 73 tests should pass
./gradlew lint    # no warnings
```

If `./gradlew test` fails on your machine, check:
1. `JAVA_HOME` points to JDK 21
2. `robolectric.properties` has `sdk=34`
3. Gradle wrapper is executable (`chmod +x gradlew`)

---

## 7. CI Pipeline

Every push to `main` triggers (`.github/workflows/ci.yml`):
1. `./gradlew lint`
2. `./gradlew test`
3. `./gradlew assembleDebug`

If CI fails → fix the issue → commit → push. CI runs in ~2 minutes.

---

## 8. Future Development — Phase 5 & Beyond

### Phase 5: Polish (Priority)

| # | Task | Detail | Effort |
|---|------|--------|--------|
| 🔴 1 | CameraX integration | Wire up real camera to CaptureScreen. Use `CameraXViewfinder` from `camera-compose` dependency. | Medium |
| 🔴 2 | Save pipeline | Connect ReviewScreen → ExamRepository.saveExamWithScores. Parse String scores to Doubles, call repo. | Small |
| 🔴 3 | **Screenshots for deliverable** | Capture 6-8 screenshots showing full pipeline. See Section 11 below. | Small |
| 🟡 4 | RecordListScreen | List all saved records with subject filter. Tap → detail view. | Medium |
| 🟡 5 | StatsScreen + 4 charts | Score histogram, pass-rate donut, per-question bars, error heatmap. See [visualization spec](test-docs/phase5-data-visualization.md). | Medium |
| 🟢 6 | Compose UI tests | Add instrumented tests for CaptureScreen and ReviewScreen (needs emulator). | Medium |
| 🟢 7 | Template save/load | Save region presets to RegionTemplateDao. Load template → auto-create regions. | Small |
| 🟢 8 | Region label picker | Dropdown or bottom sheet to select label when creating regions. | Small |

### Beyond Phase 5 (Nice-to-Have)

| Task | Detail |
|------|--------|
| Image rotation handling | Normalize EXIF orientation before OCR |
| Multi-page support | Swipe between multiple photos for the same exam |
| Physical device testing | Run OCR pipeline on real device with sample exam papers |
| TalkBack accessibility | Screen reader support for region editing |
| Batch processing | Queue multiple exams for processing |

---

## 9. Known Issues & Gotchas

| # | Issue | Detail |
|---|-------|--------|
| 1 | Robolectric doesn't support compileSdk 36 | Test SDK fixed at 34 via `robolectric.properties`. Don't change this. |
| 2 | AGP 9.x built-in Kotlin | `kotlin-android` plugin is REMOVED. Don't add it back. |
| 3 | Room FK cascade | Deleting a Student cascades to ExamRecords, which cascade to QuestionScores. Always use ExamRepository methods, never DAOs directly. |
| 4 | Canvas labels | Compose Canvas has no text API. Use `drawContext.canvas.nativeCanvas.drawText()` with Android Paint for region labels. |
| 5 | Gradle 9.4.1 required | AGP 9.2.0 requires Gradle ≥ 9.4.1. Don't downgrade. |
| 6 | JUnit Launcher | Gradle 9.x no longer auto-includes `junit-platform-launcher`. We added it explicitly. Don't remove. |
| 7 | Bitmap recycle | `CaptureViewModel.onCleared()` recycles the bitmap. Create new bitmaps for each test, don't share. |
| 8 | `StateFlow.update` is synchronous | ViewModels don't need coroutine dispatchers for simple state updates. |

---

## 10. Key Files Reference

| File | Purpose |
|------|---------|
| `.claude/CLAUDE.md` | Project rules (TDD, architecture, conventions, team) |
| `API-SPEC.md` | All contracts: entities, DAOs, UiStates, routes |
| `TASK-BRIEF.md` | Feature requirements and phase plan |
| `PROGRESS.md` | Real-time progress tracker |
| `README.md` | Project overview and setup |
| `test-docs/test-plan.md` | Testing strategy |
| `test-docs/phase1-4-*.md` | Per-phase implementation docs with lessons learned |
| `app/src/test/resources/robolectric.properties` | Robolectric SDK config (don't change) |
| `gradle/libs.versions.toml` | All dependency versions |

---

## 11. Final Deliverable — Screenshots

The course requires UI interface screenshots showing the full pipeline.
Capture these on an emulator or physical device.

### Required Screenshots (minimum 6)

| # | Screen | What to show |
|---|--------|-------------|
| 1 | `screenshots/01-region-selection.png` | CaptureScreen with photo loaded + 4-5 regions drawn with labels |
| 2 | `screenshots/02-region-label-picker.png` | Label selection dropdown/bottom sheet (if implemented) |
| 3 | `screenshots/03-review-screen.png` | ReviewScreen with parsed student info + scores |
| 4 | `screenshots/04-review-edit.png` | ReviewScreen mid-edit (typing a correction) |
| 5 | `screenshots/05-record-list.png` | RecordListScreen with saved records |
| 6 | `screenshots/06-stats-screen.png` | StatsScreen with charts/distribution |

### Optional (if time permits)

| # | Screen |
|---|--------|
| 7 | `screenshots/07-save-confirmed.png` | "Saved ✓" confirmation state |
| 8 | `screenshots/08-csv-export.png` | CSV export result or share sheet |

### How to Capture

Emulator:
```bash
# Take screenshot via adb
adb exec-out screencap -p > screenshots/01-region-selection.png

# Or use emulator's built-in screenshot button (camera icon in toolbar)
```

Physical device:
- Use Android Studio's Logcat tab → Screenshot button
- Or device's hardware buttons (Power + Volume Down)

### Screenshot Quality Rules

- **Resolution**: Use a 1080p emulator (1080×2400) for consistent sizing
- **Content**: Use realistic test data (e.g., "Alice Wang", "2024001", "Math", scores 8/10, 7.5/10, 9/10)
- **Clean state**: No debug overlays, no system notifications visible
- **Naming**: `screenshots/XX-description.png` in the `screenshots/` folder

---

## Quick Answers

**Q: Tests fail with SQLiteConstraintException?**
→ Missing FK parent row. Add parent in `@Before` using `runBlocking`.

**Q: Tests fail with NoClassDefFoundError for android.graphics.RectF?**
→ Missing `@RunWith(RobolectricTestRunner)`.

**Q: CI fails with "kotlin-android plugin no longer required"?**
→ Someone added it back. Remove `alias(libs.plugins.kotlin.android)` from both build files.

**Q: How do I add a new dependency?**
→ Add version to `[versions]` in `libs.versions.toml`, add library to `[libraries]`, add to `app/build.gradle.kts`.

**Q: How do I create a new screen?**
→ 1. Create `XxxViewModel` in `ui/xxx/`. 2. Create `XxxScreen` composable. 3. Follow pattern from CaptureScreen or ReviewScreen. 4. Use theme colors from `ui/theme/Theme.kt`.
