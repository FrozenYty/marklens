# Test Plan — MarkLens

## 1. Test Strategy

Test-driven development (TDD) throughout. Every feature starts with a
failing test before implementation.

**Unit tests** use JUnit 5 (Jupiter platform) and run on the JVM.
**Instrumented tests** use the AndroidJUnitRunner (JUnit 4 based) and
require a device or emulator. See [API-SPEC.md](../API-SPEC.md) for the
contracts each test verifies.

### Testing Methods

| Method | Tool | Scope |
|--------|------|-------|
| Unit — DAO | Room in-memory + `kotlinx.coroutines.test.runTest` | CRUD, queries, relationships |
| Unit — Repository | Mockito / fake DAOs | Business logic, error handling |
| Unit — ViewModel | Fake Repository + Turbine | UiState transitions, user actions |
| Unit — Parser | Pure JUnit | String → entity mapping accuracy |
| UI Integration | Compose Testing + `createComposeRule` | Screen rendering, user interaction |
| Manual | Checklist + device | Camera capture, ML Kit OCR quality |

### Test Priorities

| Priority | Layer | Why |
|----------|-------|-----|
| P0 | DAO | Data integrity is foundational |
| P0 | Repository | Gatekeeper between UI and data |
| P0 | Parser | Correctness of OCR→entity mapping |
| P1 | ViewModel | UiState logic, user action handling |
| P2 | Compose UI | Visual correctness, interaction flow |
| P3 | Manual | Hardware-dependent (camera) |

## 2. Test Case Outline

### Phase 1 — Data Layer

| TC-ID | Target | Method |
|-------|--------|--------|
| DATA-001 | `StudentDao` — insert & query | Unit (Room in-memory) |
| DATA-002 | `ExamRecordDao` — insert & query with relations | Unit (Room in-memory) |
| DATA-003 | `QuestionScoreDao` — CRUD & foreign key | Unit (Room in-memory) |
| DATA-004 | `RegionTemplateDao` — save & load templates | Unit (Room in-memory) |
| DATA-005 | `ExamRepository` — orchestrate DAOs | Unit (mock DAOs) |
| DATA-006 | Database migration test | Unit (MigrationTestHelper) |

### Phase 2 — Capture & OCR

| TC-ID | Target | Method |
|-------|--------|--------|
| OCR-001 | `RegionSelector` — draw/edit/delete bounding boxes | Compose UI |
| OCR-002 | `OcrEngine` — extract text from cropped bitmap | Manual (ML Kit on device) |
| OCR-003 | `RegionMapper` — map OCR text blocks to region labels | Unit |

### Phase 3 — Parse & Edit

| TC-ID | Target | Method |
|-------|--------|--------|
| PARSE-001 | `ScoreParser` — parse numeric scores from OCR text | Unit |
| PARSE-002 | `StudentInfoParser` — parse name/ID/class from OCR text | Unit |
| PARSE-003 | `ReviewViewModel` — state for correction workflow | Unit (ViewModel) |
| PARSE-004 | `ReviewScreen` — display parsed fields, allow edit | Compose UI |

### Phase 4 — Stats & Export

| TC-ID | Target | Method |
|-------|--------|--------|
| STAT-001 | `StatsCalculator` — average, max, min, pass rate | Unit |
| STAT-002 | `StatsCalculator` — per-question score distribution | Unit |
| STAT-003 | `RecordListViewModel` — filter by class/subject | Unit (ViewModel) |
| STAT-004 | `CsvExporter` — generate valid CSV from records | Unit |
| STAT-005 | `StatsScreen` — render charts and summaries | Compose UI |

## 3. Environment

### Unit Tests (`app/src/test/`)

| Item | Value |
|------|-------|
| Language | Kotlin 2.3 |
| Camera | CameraX 1.5 + camera-compose (CameraXViewfinder) |
| Test runner | JUnit 5 (Jupiter) via `useJUnitPlatform()` |
| Coroutines | `kotlinx.coroutines.test.runTest` + `TestDispatcher` |
| Mock framework | Mockito-Kotlin |
| Flow testing | Turbine |
| Room testing | `Room.inMemoryDatabaseBuilder` |

### Instrumented Tests (`app/src/androidTest/`)

| Item | Value |
|------|-------|
| Test runner | `AndroidJUnitRunner` (JUnit 4 based) |
| Compose testing | `createComposeRule` + `ComposeTestRule` |
| Espresso | For non-Compose system interactions |

### CI

| Job | Command |
|-----|---------|
| Lint | `./gradlew lint` |
| Unit tests | `./gradlew test` |
| Build | `./gradlew assembleDebug` |
