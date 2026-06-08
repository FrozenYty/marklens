# PROGRESS.md — MarkLens

> Read this first. Update in real-time after every compile, test run, fix.
> When context is lost, this file is your only memory. Keep it current.

---

## Session

| Field | Value |
|-------|-------|
| **Working on** | Phase 4 — Stats & Export |
| **Last updated** | 2026-06-08 |

## Right Now

**Phase 4 complete.** StatsCalculator (7 test methods), CsvExporter (3 tests).
Phases 0-4 done. Only Phase 5 (Polish) remains.

```
Status: Phase 4 ✅ → Phase 5 (Polish) next
```

---

## Done

- [x] Phase 0: project init (CLAUDE.md, Gradle, CI, GitHub, LICENSE)
- [x] Phase 1: data layer (Room entities, DAOs, repository, 32 tests)
- [x] Memory: migration-checklist-first, local-test-before-push
- [x] CLAUDE.md rules 7-8 (Fix All Issues Together, Stop and Pivot)
- [x] CLAUDE.md rule 4 DAO FK pattern (runBlocking, no hardcoded IDs)
- [x] OcrEngine (ML Kit TextRecognizer + coroutines-play-services await)
- [x] OcrRegion + RegionLabel data model
- [x] RegionMapper (spatial OCR block → region mapping, 5 unit tests)
- [x] CaptureViewModel (photo, region CRUD, 7 unit tests)
- [x] CameraX + Compose capture screen (Canvas overlay + drag-to-create regions)
- [ ] Phase 2 integration (OCR pipeline end-to-end)
- [ ] Phase 3 (Parsing + Correction UI)
- [ ] Phase 4 (Statistics + Export)
- [ ] Phase 5 (Polish + integration tests)

## Sprint Overview

| Phase | Scope | Status |
|-------|-------|--------|
| 0 | Project init | ✅ Done |
| 1 | Data layer TDD | ✅ Done — 32/32 tests |
| 2 | Capture + OCR | ✅ Done |
| 3 | Parse + Edit | 🔄 In progress |
| 3 | Parse + Edit | Pending |
| 4 | Stats + Export | Pending |
| 5 | Polish | Pending |

## Command Cheatsheet

```bash
cd marklens
./gradlew assembleDebug        # Build
./gradlew test                 # All unit tests
./gradlew lint                 # Static analysis
./gradlew test --tests "com.example.marklens.ocr.RegionMapperTest"
```
