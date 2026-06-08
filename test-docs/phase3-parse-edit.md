# Phase 3 — Parse & Edit

**Date**: 2026-06-08
**Author**: Tianyu Yao
**Status**: In progress (parsers + ViewModel done, ReviewScreen next)

---

## Overview

Phase 3 converts raw OCR text into structured entities and provides a
correction UI for manual fixes before saving.

## Files Created

### Main Source

| File | Type | Description |
|------|------|-------------|
| `parser/ScoreParser.kt` | Logic | OCR text → QuestionScore list ("8", "8/10" formats) |
| `parser/StudentInfoParser.kt` | Logic | OCR text → ParsedStudentInfo (name, ID, class) |
| `ui/review/ReviewViewModel.kt` | ViewModel | Editable review state, field-by-field correction |

### Tests (17 methods)

| File | Tests | Key scenarios |
|------|-------|--------------|
| `parser/ScoreParserTest.kt` | 8 | empty, single, multiple, non-numeric, negative, "x/y" format, full marks, totalScore |
| `parser/StudentInfoParserTest.kt` | 4 | all fields, missing field, empty, whitespace trim |
| `ui/ReviewViewModelTest.kt` | 5 | initial state, setParsedData, updateName, updateScore, markSaveComplete |

## Design Decisions

1. **"x/y" score format**: If OCR text contains "/", split into score/maxScore
   (e.g., "8/10" → score=8.0, maxScore=10.0). Otherwise, score=extracted number,
   maxScore defaults to 10.0.

2. **isWrong flag**: Set when score < maxScore. A negative score counts as wrong.

3. **Trim on parse**: StudentInfoParser trims whitespace. OCR often produces
   leading/trailing spaces from bounding box padding.

4. **ScoreField uses String**: Scores stored as String for easy inline editing.
   Numeric validation happens in ScoreParser, not in the ViewModel.

## Lessons Learned

| # | Pitfall | Resolution |
|---|---------|------------|
| 9 | OCR text may have trailing whitespace | Always `.trim()` rawText before parsing |
| 10 | `toDoubleOrNull()` for non-numeric OCR output | Returns 0.0 for garbage text — don't crash on bad OCR |
| 11 | ScoreField.score is String for editability | Keep as String in UiState; parse to Double at save time |

## Next

- ReviewScreen composable (editable form with region thumbnails)
- Save pipeline integration (ReviewViewModel → ExamRepository.saveExamWithScores)
