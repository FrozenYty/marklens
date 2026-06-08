# PROGRESS.md — MarkLens

> Read this first. Update in real-time after every compile, test run, fix.
> When context is lost, this file is your only memory. Keep it current.

---

## Session

| Field | Value |
|-------|-------|
| **Working on** | Phase 0 — Project Initialization |
| **Last updated** | 2026-06-08 |

## Right Now

**Phase 0 complete.** Full project skeleton. All dependencies verified against
Context7 (2026-06-08): AGP 9.2, Kotlin 2.3.21, Compose BOM 2026.05.00, Room 2.8.4,
CameraX 1.5 + camera-compose, ML Kit 16.0.1. CI passes lint + test + build.
Team: Tianyu Yao (@FrozenYty), Jianheng Sun (@chemflowers).

```
Status: Phase 0 DONE → entering Phase 1 (Data Layer TDD)
```

---

## Done

- [x] CLAUDE.md (project-level rules: TDD, MVVM, Compose/Room conventions)
- [x] TASK-BRIEF.md (feature requirements, 5-phase plan)
- [x] API-SPEC.md (entity, DAO, repository, UiState, parser, OCR contracts)
- [x] README.md (overview, tech stack, structure)
- [x] test-docs/test-plan.md (testing strategy, TC outlines)
- [x] Gradle project skeleton (Kotlin DSL, version catalog, Compose, Room, CameraX, ML Kit)
- [x] CI workflow (lint + unit tests + debug build)
- [x] GitHub repo (FrozenYty/marklens)
- [x] LICENSE (MIT)

## Sprint Overview

| Phase | Scope | Status |
|-------|-------|--------|
| 0 | Project init | Done |
| 1 | Data layer TDD | Next |
| 2 | Capture + OCR | Pending |
| 3 | Parse + Edit | Pending |
| 4 | Stats + Export | Pending |
| 5 | Polish | Pending |

## Command Cheatsheet

```bash
# Build
cd marklens
./gradlew assembleDebug

# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Run specific test class
./gradlew test --tests "com.example.marklens.data.dao.StudentDaoTest"
```
