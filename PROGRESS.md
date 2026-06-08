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

**Phase 1 — Data Layer TDD in progress.** Entities, DAOs, Database, Repository
all implemented. 5 test classes with 28 tests total. Awaiting CI confirmation.

```
Status: Phase 1 code complete → verify CI (./gradlew test)
```

## Done

- [x] Phase 0: project init
- [x] Data entities: Student, ExamRecord, QuestionScore, RegionTemplate
- [x] DAOs: StudentDao, ExamRecordDao, QuestionScoreDao, RegionTemplateDao
- [x] Database: MarkLensDatabase (Room, 4 entities, version 1)
- [x] Repository: ExamRepository (getOrCreateStudent, saveExamWithScores, deleteRecord, templates)
- [x] Tests: StudentDaoTest, ExamRecordDaoTest, QuestionScoreDaoTest, RegionTemplateDaoTest, ExamRepositoryTest
- [ ] CI green (pending)

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
