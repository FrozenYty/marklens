# Test Plan — MarkLens

> **Current test status (2026-06-10):** 81 pass / 0 fail.
> OCR end-to-end tested on 200 generated exam papers — 100% success rate.

## 1. Test Strategy

Test-driven development (TDD) throughout. Every feature starts with a
failing test before implementation.

**Unit tests** use JUnit 5 (Jupiter platform) and run on the JVM.
**Instrumented tests** use the AndroidJUnitRunner (JUnit 4 based) and
require a device or emulator.

### Testing Methods

| Method | Tool | Scope |
|--------|------|-------|
| Unit — DAO | Room in-memory + Robolectric + `runTest` | CRUD, queries, relationships |
| Unit — Repository | Mockito / fake DAOs | Business logic, error handling |
| Unit — ViewModel | Fake Repository + Turbine | UiState transitions, user actions |
| Unit — Parser | Pure JUnit | String → entity mapping accuracy |
| Unit — OCR | RegionMapper + mock blocks | Spatial block-to-region mapping |
| UI Integration | Compose Testing + `createComposeRule` | Screen rendering, user interaction |
| E2E OCR | `generate_papers.py` + batch import | Full pipeline accuracy on 200 papers |
| Manual | Checklist + device | Gallery picker, ML Kit on real photos |

### Test Priorities

| Priority | Layer | Why |
|----------|-------|-----|
| P0 | DAO | Data integrity is foundational |
| P0 | Repository | Gatekeeper between UI and data |
| P0 | Parser | Correctness of OCR→entity mapping |
| P1 | ViewModel | UiState logic, user action handling |
| P1 | RegionMapper | Spatial matching is core to accuracy |
| P2 | Compose UI | Visual correctness, interaction flow |
| P3 | Manual | Hardware-dependent (camera, gallery) |

## 2. Test Files

| File | Scope | Tests |
|------|-------|-------|
| `ExamRecordDaoTest.kt` | DAO CRUD + queries | 9 |
| `QuestionScoreDaoTest.kt` | DAO CRUD + FK constraints | 8 |
| `RegionTemplateDaoTest.kt` | DAO insert/update/delete | 5 |
| `StudentDaoTest.kt` | DAO insert/query | 3 |
| `ExamRepositoryTest.kt` | Repository orchestration | 7 |
| `RegionMapperTest.kt` | Spatial block mapping | 7 |
| `ScoreParserTest.kt` | Score extraction (single + table) | 9 |
| `StudentInfoParserTest.kt` | Name/ID/class extraction | 4 |
| `RecordListViewModelTest.kt` | List + filter + delete | 5 |
| `ReviewViewModelTest.kt` | Save pipeline | 7 |
| `StatsViewModelTest.kt` | Statistics calculation | 4 |
| `CsvExporterTest.kt` | CSV format validation | 3 |
| `StatsCalculatorTest.kt` | Average/max/min/pass rate | 6 |
| `ScreenTests.kt` | Compose UI (instrumented) | — |

**Total: 13 test classes, 81 unit tests passing.**

## 3. E2E OCR Validation

The `generate_papers.py` script creates 200 exam papers (40 students × 5 subjects)
with known data. The batch import tool runs the full OCR pipeline on each:

```
Gallery photo → ML Kit full-page OCR → RegionMapper → ScoreParser + StudentInfoParser → Room
```

**Results (2026-06-10):** 201/201 papers processed, 0 failures.
- Student names: correctly extracted (label prefix stripped)
- Student IDs: correctly extracted
- Classes: correctly extracted
- Subjects: correctly extracted
- Total scores: correctly parsed (handles "76 I100" → 76)
- Question scores: correctly parsed from column-oriented table OCR output

## 4. Environment

### Unit Tests (`app/src/test/`)

| Item | Value |
|------|-------|
| Language | Kotlin 2.3 |
| Test runner | JUnit 5 (Jupiter) via `useJUnitPlatform()` |
| Coroutines | `kotlinx.coroutines.test.runTest` + `TestDispatcher` |
| Mock framework | Mockito-Kotlin |
| Flow testing | Turbine |
| Room testing | `Room.inMemoryDatabaseBuilder` + Robolectric |

### Instrumented Tests (`app/src/androidTest/`)

| Item | Value |
|------|-------|
| Test runner | `AndroidJUnitRunner` (JUnit 4 based) |
| Compose testing | `createComposeRule` + `ComposeTestRule` |

### Commands

| Job | Command |
|-----|---------|
| Unit tests | `./gradlew test` |
| Build | `./gradlew assembleDebug` |
| Install | `./gradlew installDebug` |
| Generate test papers | `python generate_papers.py` |
