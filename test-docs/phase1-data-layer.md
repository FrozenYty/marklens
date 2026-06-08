# Phase 1 — Data Layer Implementation

**Date**: 2026-06-08
**Author**: Tianyu Yao
**Status**: Implemented, awaiting CI verification

---

## Overview

The data layer provides Room-based persistence for the MarkLens exam paper
digitization pipeline. All data access goes through `ExamRepository`, which
orchestrates four DAOs.

## Architecture

```
ExamRepository
    ├── StudentDao          → students table
    ├── ExamRecordDao       → exam_records table (FK → students)
    ├── QuestionScoreDao    → question_scores table (FK → exam_records)
    └── RegionTemplateDao   → region_templates table

MarkLensDatabase (Room, version 1)
```

## Entity Relationship Diagram

```
Student 1 ──── N ExamRecord 1 ──── N QuestionScore
                          │
RegionTemplate (standalone, JSON blob)
```

## Files Created

### Main source (11 files)

| File | Type | Description |
|------|------|-------------|
| `data/entity/Student.kt` | Entity | Name, studentId, className |
| `data/entity/ExamRecord.kt` | Entity | FK→Student, subject, totalScore, imageUri |
| `data/entity/QuestionScore.kt` | Entity | FK→ExamRecord, questionNumber, score, maxScore, isWrong |
| `data/entity/RegionTemplate.kt` | Entity | Name, regionsJson (JSON blob) |
| `data/dao/StudentDao.kt` | DAO | CRUD + getByStudentId + getByClass + getAll (Flow) |
| `data/dao/ExamRecordDao.kt` | DAO | CRUD + getById + getBySubject + getByStudentId + getAll (Flow) |
| `data/dao/QuestionScoreDao.kt` | DAO | insertAll + update + getByExamRecord (Flow) + getByExamRecordOnce (suspend) + deleteByExamRecord |
| `data/dao/RegionTemplateDao.kt` | DAO | insert + getAll (Flow) + delete |
| `data/MarkLensDatabase.kt` | Database | Room DB, 4 entities, exportSchema = false |
| `data/ExamRepository.kt` | Repository | Business logic: getOrCreateStudent, saveExamWithScores, deleteRecord cascade, templates |

### Tests (5 files, 28 test methods)

| File | Tests | Key scenarios |
|------|-------|--------------|
| `data/dao/StudentDaoTest.kt` | 6 | insert/query/update/delete/class filter |
| `data/dao/ExamRecordDaoTest.kt` | 7 | insert/query/subject filter/student filter/update/delete/getAll |
| `data/dao/QuestionScoreDaoTest.kt` | 5 | insertAll/ordering/oneShot/update/delete cascade |
| `data/dao/RegionTemplateDaoTest.kt` | 3 | insert/getAll/delete |
| `data/ExamRepositoryTest.kt` | 9 | getOrCreateStudent (new+existing), saveExamWithScores (atomic), deleteRecord (cascade), subject filter, templates CRUD, getAll, notFound |

## Test Configuration

- **Framework**: JUnit 5 (Jupiter) via `useJUnitPlatform()`
- **Database**: `Room.inMemoryDatabaseBuilder` — real DB, no mocking
- **Coroutines**: `kotlinx.coroutines.test.runTest`
- **Flow assertions**: Turbine (`app.cash.turbine:turbine`)
- **Assertions**: `org.junit.jupiter.api.Assertions`

## Key Design Decisions

1. **`getByExamRecordOnce`**: Added a suspend one-shot query in `QuestionScoreDao`
   alongside the reactive `getByExamRecord` (Flow). This avoids the anti-pattern
   of collecting a Flow inside a suspend function.

2. **Delete cascade**: `deleteRecord` removes scores first, then the record.
   Room's `onDelete = ForeignKey.CASCADE` handles the FK side, but the DAO
   delete ensures data consistency before the FK triggers.

3. **`getOrCreateStudent`**: Idempotent — returns existing student if `studentId`
   matches, inserts new otherwise. Prevents duplicate students.

4. **No Hilt/Dagger**: Manual constructor injection via `ExamRepository(daos...)`.
   Sufficient for a single-module project.

## Dependencies Used

| Library | Version | Purpose |
|---------|---------|---------|
| Room (runtime, ktx, compiler) | 2.8.4 | ORM |
| KSP | 2.3.7 | Room annotation processing |
| Coroutines | 1.9.0 | Async DAO operations |
| Turbine | 1.2.0 | Flow testing |
| JUnit Jupiter | 5.11.4 | Test framework |
