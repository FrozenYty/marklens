# Phase 5 — Data Visualization Tasks

**Assigned to**: Jianheng Sun (@chemflowers)
**Date**: 2026-06-08
**Prerequisite**: StatsCalculator (Phase 4, already done)

---

## Overview

Add 4 chart composables to the StatsScreen. All rendered with Compose Canvas
(no third-party library needed). Data comes from `StatsCalculator` via
`StatsResult`, already computed in Phase 4.

## Chart Inventory

| # | Chart | Priority | Difficulty | Lines (est.) |
|---|-------|----------|------------|-------------|
| 1 | Score Distribution Histogram | 🔴 P0 | Easy | ~50 |
| 2 | Pass-Rate Donut Ring | 🔴 P0 | Easy | ~40 |
| 3 | Per-Question Score Bar Chart | 🔴 P0 | Easy | ~50 |
| 4 | Error Heatmap (Students × Questions) | 🟡 P1 | Medium | ~80 |

Total: ~220 lines across 4 composables + StatsScreen integration.

---

## Chart 1 — Score Distribution Histogram

### What it shows

Bar chart: X-axis = score buckets (0-29, 30-39, 40-49, ..., 90-100). Y-axis = student count in each bucket. Tallest bar at the mode — teacher instantly sees "most students scored in the 70-79 range."

### Data source

`StatsResult.scoreDistribution: Map<String, Int>`
Example: `{"70-79": 12, "80-89": 8, "60-69": 5, "90-100": 3}`

### Implementation sketch

```kotlin
@Composable
fun ScoreHistogram(
    distribution: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val buckets = listOf("0-29","30-39","40-49","50-59","60-69","70-79","80-89","90-100")
        val maxCount = (distribution.values.maxOrNull() ?: 1).coerceAtLeast(1)
        val barW = size.width / buckets.size * 0.7f
        val gap = size.width / buckets.size * 0.3f

        buckets.forEachIndexed { i, bucket ->
            val count = distribution[bucket] ?: 0
            val barH = (count.toFloat() / maxCount) * (size.height - 24f)
            val x = i * (barW + gap) + gap / 2
            val y = size.height - barH - 20f

            // Bar
            drawRect(SoftGreen, Offset(x, y), Size(barW, barH))
            // Label
            drawContext.canvas.nativeCanvas.drawText(
                bucket.take(2), // "0-", "30", etc.
                x, size.height - 4f,
                android.graphics.Paint().apply { color = White; textSize = 20f }
            )
            // Count on top
            if (count > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "$count", x, y - 4f,
                    android.graphics.Paint().apply { color = White; textSize = 18f }
                )
            }
        }
    }
}
```

### Key decisions
- Bar width = 70% of slot width, 30% gap
- Show count labels only when count > 0
- X-axis labels: abbreviated (first 2 chars of bucket name)

---

## Chart 2 — Pass-Rate Donut Ring

### What it shows

Circular ring: green arc = pass rate (≥60), red arc = fail rate. Center text shows pass percentage and total count. Quick overview — "82% passed, 34 students."

### Data source

- `StatsResult.passRate: Double` (0.0–1.0)
- `StatsResult.totalRecords: Int`

### Implementation sketch

```kotlin
@Composable
fun PassRateDonut(
    passRate: Double,
    totalRecords: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 24f
            val radius = (size.minDimension - strokeW) / 2
            val topLeft = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
            val arcSize = Size(radius * 2, radius * 2)

            // Background ring (red — fail)
            drawArc(Slate.copy(alpha = 0.3f), 0f, 360f, false,
                topLeft = topLeft, size = arcSize, style = Stroke(strokeW))

            // Pass arc (green)
            drawArc(SoftGreen, -90f, (passRate * 360).toFloat(), false,
                topLeft = topLeft, size = arcSize, style = Stroke(strokeW))
        }
        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${(passRate * 100).toInt()}%", color = White, fontSize = 22.sp, fontWeight = Bold)
            Text("$totalRecords students", color = Slate, fontSize = 12.sp)
        }
    }
}
```

### Key decisions
- Ring thickness = 24dp
- Start arc at -90° (12 o'clock position)
- Center text layered on top of Canvas via Box stacking

---

## Chart 3 — Per-Question Score Bar Chart

### What it shows

Horizontal bar chart. Each row = one question. Bar length = average score / max score ratio. Color from green (≥80%) to yellow (60-80%) to red (<60%). Teacher spots "Q4 is the problem question" at a glance.

### Data source

`StatsResult.perQuestionStats: List<QuestionStat>`
Each `QuestionStat` has: `questionNumber`, `maxScore`, `averageScore`, `errorRate`, `totalAttempts`

### Implementation sketch

```kotlin
@Composable
fun QuestionBarChart(
    stats: List<QuestionStat>,
    modifier: Modifier = Modifier
) {
    val rowH = 36.dp
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        for (stat in stats) {
            Row(
                modifier = Modifier.fillMaxWidth().height(rowH).padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Q-number label
                Text("Q${stat.questionNumber}", color = Slate, fontSize = 12.sp,
                    modifier = Modifier.width(32.dp))

                // Bar
                val ratio = (stat.averageScore / stat.maxScore).coerceIn(0f, 1f)
                val barColor = when {
                    ratio >= 0.8f -> SoftGreen
                    ratio >= 0.6f -> Amber
                    else -> MarkRed
                }
                Box(modifier = Modifier.weight(1f).height(20.dp)) {
                    Canvas(Modifier.fillMaxSize()) {
                        // Background
                        drawRoundRect(Slate.copy(alpha = 0.2f), cornerRadius = CornerRadius(4f, 4f))
                        // Fill
                        drawRoundRect(barColor, cornerRadius = CornerRadius(4f, 4f),
                            size = Size(size.width * ratio, size.height))
                    }
                }

                // Score text
                Text("${stat.averageScore.toInt()}/${stat.maxScore.toInt()}",
                    color = Slate, fontSize = 11.sp, modifier = Modifier.width(48.dp))
                // Error rate
                Text("${(stat.errorRate * 100).toInt()}% err",
                    color = MarkRed.copy(alpha = 0.7f), fontSize = 10.sp,
                    modifier = Modifier.width(48.dp))
            }
        }
    }
}
```

### Key decisions
- Horizontal bar avoids X-axis label rotation headaches
- 3-tier color coding: green (good), amber (warning), red (problem)
- Error rate shown as supplementary text — not a separate chart
- `Canvas + drawRoundRect` for the bar fill (clean rounded ends)

---

## Chart 4 — Error Heatmap (Students × Questions)

### What it shows

Grid: rows = students (anonymized: "S1", "S2", ...), columns = questions. Each cell = colored square: green if the student got that question right, red if wrong. Dense red columns = "this question is a universal weak point." Dense red rows = "this student needs intervention."

### Data source

Need to pass raw `Map<Long, List<QuestionScore>>` to this composable (allScores from StatsCalculator input). Each `QuestionScore` has `isWrong: Boolean`.

### Implementation sketch

```kotlin
@Composable
fun ErrorHeatmap(
    allScores: Map<Long, List<QuestionScore>>,
    modifier: Modifier = Modifier
) {
    val entries = allScores.entries.toList()
    if (entries.isEmpty()) return

    val maxQ = entries.flatMap { it.value }.maxOf { it.questionNumber }
    val cellSize = 20.dp

    Column(modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
        // Column header
        Row { (1..maxQ).forEach { q ->
            Text("Q$q", color = Slate, fontSize = 9.sp,
                modifier = Modifier.width(cellSize), textAlign = TextAlign.Center)
        }}

        // Heatmap rows
        for ((idx, entry) in entries.withIndex()) {
            val scores = entry.value.sortedBy { it.questionNumber }
            Row {
                for (score in scores) {
                    Box(modifier = Modifier.size(cellSize).padding(2.dp)) {
                        Canvas(Modifier.fillMaxSize()) {
                            drawRect(
                                if (score.isWrong) MarkRed.copy(alpha = 0.7f)
                                else SoftGreen.copy(alpha = 0.5f),
                                size = size
                            )
                        }
                    }
                }
            }
        }
    }
}
```

### Key decisions
- Fixed cell size (20dp) for predictable layout
- Horizontal scroll for wide question sets
- alpha transparency for visual layering when many cells
- Students labeled by index ("S1", "S2") — real names require Student entity join

---

## Integration: StatsScreen Layout

The `StatsScreen` composable should assemble these four charts:

```kotlin
@Composable
fun StatsScreen(
    statsResult: StatsResult,
    allScores: Map<Long, List<QuestionScore>>,
    subject: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("$subject — Statistics", color = White, fontSize = 20.sp, fontWeight = Bold)

        // Top row: key metrics
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            MetricCard("Average", "${statsResult.averageScore.toInt()}")
            MetricCard("Highest", "${statsResult.maxScore.toInt()}")
            MetricCard("Lowest", "${statsResult.minScore.toInt()}")
        }

        // Pass-rate donut
        SectionCard("Pass Rate") {
            PassRateDonut(statsResult.passRate, statsResult.totalRecords)
        }

        // Score histogram
        SectionCard("Score Distribution") {
            ScoreHistogram(statsResult.scoreDistribution)
        }

        // Per-question chart
        SectionCard("Question Analysis") {
            QuestionBarChart(statsResult.perQuestionStats)
        }

        // Heatmap
        if (allScores.isNotEmpty()) {
            SectionCard("Error Heatmap") {
                ErrorHeatmap(allScores)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = White, fontSize = 24.sp, fontWeight = Bold)
        Text(label, color = Slate, fontSize = 12.sp)
    }
}
```

---

## Files to Create

| File | Content |
|------|---------|
| `ui/stats/ScoreHistogram.kt` | Chart 1 composable |
| `ui/stats/PassRateDonut.kt` | Chart 2 composable |
| `ui/stats/QuestionBarChart.kt` | Chart 3 composable |
| `ui/stats/ErrorHeatmap.kt` | Chart 4 composable |
| `ui/stats/StatsScreen.kt` | Assembly screen with all charts + metric cards |
| `ui/stats/StatsViewModel.kt` | ViewModel — loads data from ExamRepository, calls StatsCalculator |

## Testing

| Test | Type | Location |
|------|------|----------|
| `StatsViewModel` state management | Unit | `app/src/test/.../ui/StatsViewModelTest.kt` |
| Histogram renders without crash | Compose UI | `app/src/androidTest/.../ui/` (needs emulator) |
| Donut renders correct arc angle | Compose UI | `app/src/androidTest/.../ui/` |

## Dependencies

No new libraries. All charts use:
- `androidx.compose.foundation.Canvas`
- `androidx.compose.ui.geometry.Offset` / `Size`
- `androidx.compose.ui.graphics.drawscope.Stroke`
- `androidx.compose.ui.graphics.nativeCanvas` (for text labels)
- MarkLens theme colors from `com.example.marklens.ui.theme`

---

## Acceptance Criteria

- [ ] 3 of 4 charts render with correct data on emulator
- [ ] StatsScreen scrolls vertically without layout errors
- [ ] Charts handle edge cases: empty data (0 records), single record, all perfect scores, all failing scores
- [ ] Color coding consistent with design system (SoftGreen/MarkRed/Amber/Slate)
- [ ] Screenshots of StatsScreen included in final deliverable
