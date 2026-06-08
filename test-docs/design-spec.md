# Design Specification — MarkLens

**For**: Jianheng Sun
**Purpose**: Complete visual reference for all UI work. No external tools needed.

---

## 1. Design Direction

```
Industrial Precision × Academic Warmth
```

Imagine a precision measurement tool (calipers, microscope) crossed with the
warmth of paper and red ink grading. Dark, focused, professional — not a toy.

- **Canvas**: Deep ink-dark background. The exam photo is the bright subject;
  everything else recedes.
- **Marking**: Red outlines like a teacher's grading pen. Green for selection/confirmation.
- **Typography**: Monospace for numbers. Sans-serif for labels. Always clean, never playful.
- **Surfaces**: Paper-white cards (alpha 0.95) floating on dark canvas. Dark translucent
  bars for chrome (toolbars, badges).

---

## 2. Color Tokens

File: `com.example.marklens.ui.theme.Theme.kt`

### Dark Mode (Primary)

| Token | Hex | R G B | Usage | Never use for |
|-------|-----|--------|-------|---------------|
| `Ink` | `#1C1C2E` | 28, 28, 46 | Full-screen canvas backgrounds | Text on dark |
| `Paper` | `#FAF8F5` | 250, 248, 245 | Cards, dialogs (on dark bg) | Large solid areas on white |
| `MarkRed` | `#E63946` | 230, 57, 70 | Region outlines, delete actions, error states | Text body |
| `SoftGreen` | `#2A9D8F` | 42, 157, 143 | Selected regions, confirm buttons, pass-rate | Delete/destructive |
| `Amber` | `#F4A261` | 244, 162, 97 | Drag preview, warning states | Final UI (transient only) |
| `Slate` | `#64748B` | 100, 116, 139 | Secondary text, disabled state, borders | Primary actions |
| `InkTranslucent` | `#CC1C1C2E` | — | Overlay bars/toolbars on canvas | Opaque backgrounds |
| `SurfaceWhite` | `#FFFFFFFF` | 255,255,255 | Card interiors, text field backgrounds | Canvas background |

### Semantic Mapping

```
Good/Pass/Correct    → SoftGreen (#2A9D8F)
Bad/Fail/Error/Wrong → MarkRed   (#E63946)
Warning/Preview      → Amber     (#F4A261)
Neutral/Disabled     → Slate     (#64748B)
Background           → Ink       (#1C1C2E)  or  Paper (#FAF8F5)
```

### Opacity Variants

```kotlin
// Use .copy(alpha = ...) on any token — never hardcode a new hex
MarkRed.copy(alpha = 0.25f)     // Disabled destructive button bg
MarkRed.copy(alpha = 0.12f)     // Region box fill
SoftGreen.copy(alpha = 0.5f)    // Heatmap correct cell
Slate.copy(alpha = 0.3f)        // Unfocused text field border
Slate.copy(alpha = 0.2f)        // Chart background bar
```

### When Adding a New Screen

1. Choose background: `Ink` (immersive) or `Paper` (information-dense)
2. Use `SurfaceWhite` cards with alpha 0.95 on dark backgrounds
3. Pick one dominant accent from the semantic mapping
4. Never create new colors — extend the palette only with team discussion

---

## 3. Component Patterns

### 3.1 Screen Shell

```kotlin
// Every screen follows this skeleton:
@Composable
fun XxxScreen(viewModel: XxxViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Column(  // or Box for full-bleed
        modifier = Modifier
            .fillMaxSize()
            .background(Ink)        // immersive dark canvas
            .statusBarsPadding()    // notch-safe
    ) {
        // Content here
    }
}
```

### 3.2 Section Card

```kotlin
// White card on dark background — copy exactly:
@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite.copy(alpha = 0.95f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, color = Ink, fontSize = 15.sp)
        HorizontalDivider(color = Slate.copy(alpha = 0.2f))
        content()
    }
}
```

### 3.3 Text Field (Outlined)

```kotlin
// Consistent field colors — copy from ReviewScreen.fieldColors():
@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    focusedBorderColor = SoftGreen,
    unfocusedBorderColor = Slate.copy(alpha = 0.3f),
    cursorColor = MarkRed,
    focusedContainerColor = SurfaceWhite,
    unfocusedContainerColor = SurfaceWhite
)

// Usage:
OutlinedTextField(
    value = ...,
    onValueChange = ...,
    colors = fieldColors(),
    shape = RoundedCornerShape(8.dp),
    singleLine = true,
    modifier = Modifier.fillMaxWidth()
)
```

### 3.4 Primary Button

```kotlin
Button(
    onClick = ...,
    modifier = Modifier.fillMaxWidth().height(48.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = SoftGreen,   // green = confirm/save
        contentColor = SurfaceWhite
    ),
    shape = RoundedCornerShape(12.dp)
) {
    Text("Save to Database", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
}
```

### 3.5 Destructive/Secondary Button

```kotlin
// For Clear, Delete, Cancel:
Button(
    onClick = ...,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = MarkRed.copy(alpha = 0.25f),
        contentColor = MarkRed
    ),
    modifier = Modifier.height(36.dp),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
) {
    Text("Clear All", fontSize = 13.sp, fontWeight = FontWeight.Medium)
}
```

### 3.6 Disabled State

```kotlin
// When a button should be disabled:
ButtonDefaults.buttonColors(
    disabledContainerColor = Color.Transparent,
    disabledContentColor = Slate.copy(alpha = 0.3f)
)
```

---

## 4. Typography

| Usage | Font | Size | Weight | Color |
|-------|------|------|--------|-------|
| Screen title | System sans-serif | 20sp | Bold | `SurfaceWhite` |
| Section heading | System sans-serif | 15sp | SemiBold | `Ink` |
| Body text | System sans-serif | 14sp | Regular | `Slate` or `Ink` |
| Caption / meta | System sans-serif | 12-13sp | Regular | `Slate` |
| Button label | System sans-serif | 13-16sp | Medium/SemiBold | context-dependent |
| Numbers / data | System monospace | 14-24sp | Bold | `SurfaceWhite` |

Rules:
- Never use italic (reduces readability on small screens)
- Never go below 10sp (accessibility floor)
- Bold only for headings and key numbers
- All text left-aligned except center-aligned in donut/button

---

## 5. Spacing Scale

```kotlin
// Always use these values — never arbitrary numbers.
// Unit: dp

4.dp   // Tight gap (icon-to-text, cell padding)
6.dp   // Inline gap
8.dp   // Standard internal gap (field rows, chip padding)
12.dp  // Card-to-card gap, button corner radius
16.dp  // Screen padding, card internal padding
20.dp  // Section-to-section gap
24.dp  // Wide gap, ring thickness
36.dp  // Button height (standard)
48.dp  // Button height (primary)
```

---

## 6. Corner Radius Scale

```kotlin
8.dp   // Text fields
12.dp  // Cards, buttons, action chips
16.dp  // Bottom sheets, dialogs
20.dp  // Capsule badges
```

---

## 7. Chart Colors (StatsScreen)

For the visualization specs in `phase5-data-visualization.md`:

| Chart Element | Color |
|---------------|-------|
| Bar fill (good ≥80%) | `SoftGreen` |
| Bar fill (warning 60-80%) | `Amber` |
| Bar fill (bad <60%) | `MarkRed` |
| Bar background | `Slate.copy(alpha = 0.2f)` |
| Donut pass arc | `SoftGreen` |
| Donut fail arc | `Slate.copy(alpha = 0.3f)` |
| Heatmap correct cell | `SoftGreen.copy(alpha = 0.5f)` |
| Heatmap wrong cell | `MarkRed.copy(alpha = 0.7f)` |
| Axis labels | `SurfaceWhite` (on dark) |
| Chart title | `Slate`, 13sp |

---

## 8. Iconography

For Material Icons (use `androidx.compose.material.icons.Icons.Default`):

| Action | Icon |
|--------|------|
| Clear/Delete | `Icons.Default.Delete` |
| Save | `Icons.Default.Check` |
| Add | `Icons.Default.Add` |
| Camera | `Icons.Default.CameraAlt` |
| Settings | `Icons.Default.Settings` |
| Back | `Icons.Default.ArrowBack` |
| Search | `Icons.Default.Search` |

If Material Icons don't cover it, use Unicode emoji as text:
`Text("📷", fontSize = 48.sp)` for camera placeholder.

---

## 9. State Handling

Every screen handles these states:

| State | Visual | Example |
|-------|--------|---------|
| **Loading** | Centered `CircularProgressIndicator(color = SoftGreen)` | Fetching records |
| **Empty** | Centered icon + gray text | "No records yet" |
| **Error** | Amber banner with retry button | "Failed to load" |
| **Success** | Green confirmation text | "Saved ✓" |

```kotlin
// Loading pattern
if (uiState.isLoading) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = SoftGreen)
    }
}

// Empty pattern
if (uiState.records.isEmpty() && !uiState.isLoading) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.Center,
           verticalArrangement = Arrangement.Center) {
        Text("📋", fontSize = 48.sp)
        Text("No records found", color = Slate)
    }
}
```

---

## 10. Quick Reference Checklist

Before committing any UI change, verify:

- [ ] All colors from `Theme.kt` tokens (no raw hex values)
- [ ] Spacing from the scale (no `13.dp`, `27.dp`, etc.)
- [ ] Screen has `statusBarsPadding()` (notch-safe)
- [ ] Handles loading + empty + error states
- [ ] Button has `RoundedCornerShape` matching the radius scale
- [ ] Dark background screens use `background(Ink)`, not `background(Color.Black)`
- [ ] Text field uses `fieldColors()` (not default Material)
- [ ] `@Preview(showSystemUi = true)` on top-level screen composable
