# CLAUDE.md — marklens

Project-level behavioral rules for the MarkLens Android app. All universal
guidelines are inherited from the user-level `~/.claude/CLAUDE.md`.

**Required reading for all collaborators:**
1. This file (`.claude/CLAUDE.md`) — rules and conventions
2. [API-SPEC.md](../API-SPEC.md) — all contracts: entities, DAOs, UiStates, routes
3. [TASK-BRIEF.md](../TASK-BRIEF.md) — feature requirements and phase plan
4. [test-docs/test-plan.md](../test-docs/test-plan.md) — testing strategy and TC outlines

----

## 1. TDD Discipline

**Red → Green → Refactor. Never write implementation first.**

- Every feature starts with a failing test. Write the test, watch it fail (red), implement the minimum code to pass (green), then clean up (refactor).
- Unit tests in `app/src/test/`. Instrumented tests in `app/src/androidTest/`.
- Test naming: `<TestedClass>Test.kt`. Method naming: `method_scenario_expected`.
- Repository tests mock DAOs. DAO tests use Room `inMemoryDatabaseBuilder`. ViewModel tests use `TestDispatcher` + fake Repository.

## 2. Architecture — MVVM + Repository

```
Compose UI → ViewModel → Repository → Room DAO → SQLite
                    ↑                      ↑
                 UiState              (mock in tests)
```

- ViewModels expose `StateFlow<UiState>`. Never expose mutable state directly.
- Repositories are the single data access point. Never call DAOs from ViewModels.
- Manual constructor injection — no Hilt unless complexity justifies it.

## 3. Compose Conventions

- Each screen = one `@Composable fun XxxScreen(viewModel: XxxViewModel)`.
- `@Preview(showSystemUi = true)` on every screen composable.
- Use `collectAsStateWithLifecycle()` for StateFlow in composables.
- Material 3 with custom theme.

## 4. Room Conventions

- Entities in `data/entity/`. DAOs in `data/dao/`. Database in `data/`.
- All DAO write methods are `suspend`. Read methods return `Flow<List<T>>`.
- Schema export: `app/schemas/`. Migration tested with `MigrationTestHelper`.

## 5. Core Pipeline (O2C2R)

```
Camera → ML Kit OCR → Region Mapping → Structured Parser → Manual Correction → Room → Stats
```

- `ocr/`: ML Kit integration, region-to-text mapping.
- `parser/`: regex + heuristics — raw text → domain entities.
- `ui/review/`: correction & confirmation screen.

## 6. Testing Methods

| Layer | Tool | Runner | Location |
|-------|------|--------|----------|
| DAO | Room in-memory + Robolectric + `runTest` | Robolectric + JUnit 5 | `test/.../data/dao/` |
| Repository | Mock DAOs | JUnit 5 | `test/.../data/repository/` |
| ViewModel | Fake Repository + Turbine | JUnit 5 | `test/.../ui/` |
| Parser | Pure JUnit (string → entity) | JUnit 5 | `test/.../parser/` |
| Compose UI | Compose Testing | AndroidJUnitRunner (JUnit 4) | `androidTest/.../ui/` |
| Camera / ML Kit | Manual only | N/A | N/A |

- All unit tests use `tasks.withType<Test> { useJUnitPlatform() }`.
- All `@Test` annotations for unit tests import `org.junit.jupiter.api.Test`.
- Instrumented tests use `androidx.test.ext.junit.runners.AndroidJUnit4`.

## 7. Language & Commit

- **English only** for code, comments, docs, commit messages.
- Commit format:
  ```
  <type>: <short description>

  Author: <Your Name>
  ```
- Types: `feat`, `fix`, `test`, `docs`, `refactor`, `chore`.
- Every Kotlin file must have `@author Your Name` in KDoc.

## 8. Team

| Member | GitHub | Role |
|--------|--------|------|
| Tianyu Yao | FrozenYty | Lead — architecture, TDD, OCR pipeline, CI |
| Jianheng Sun | [chemflowers](https://github.com/chemflowers) | Developer — UI, parser, statistics |
