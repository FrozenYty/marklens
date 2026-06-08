# Phase 4 — Statistics & Export

**Date**: 2026-06-08
**Author**: Tianyu Yao
**Status**: Done

---

## Overview

Phase 4 provides data analysis and export: computed statistics, score
distribution, per-question error rates, and CSV export.

## Files Created

| File | Type | Description |
|------|------|-------------|
| `util/StatsCalculator.kt` | Logic | Average, min/max, pass rate, score distribution, per-question stats |
| `util/CsvExporter.kt` | Logic | Records + scores → CSV with dynamic Q columns |

## Tests (9 methods)

| File | Tests | Key scenarios |
|------|-------|--------------|
| `util/StatsCalculatorTest.kt` | 6 | empty, single, multiple, pass rate, distribution buckets, per-question error rates |
| `util/CsvExporterTest.kt` | 3 | empty, single record, with question columns |

## Design Decisions

| # | Decision | Detail |
|---|----------|--------|
| 1 | Pass threshold = 60 | Standard academic pass mark |
| 2 | Score distribution in 10-point buckets | 90-100, 80-89, ..., 0-29 |
| 3 | CSV dynamic columns | Max question count across all records determines Q1..QN columns |
| 4 | Student info columns empty in CSV | Student name/ID/class requires join with Student entity — caller fills |

## Lessons Learned

| # | Pitfall | Resolution |
|---|---------|------------|
| 15 | Empty records → division by zero in average/pass rate | Short-circuit return `StatsResult()` when records.isEmpty() |
| 16 | CSV header: dynamic question columns | Use `maxOfOrNull` to find max question number, generate Q1..QN |
