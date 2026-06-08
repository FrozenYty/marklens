# Screenshots — MarkLens

Final deliverable screenshots for course submission. Taken on Android emulator (1080×2400) or physical device.

## Required List

| # | File | Screen | Status |
|---|------|--------|--------|
| 1 | `01-region-selection.png` | CaptureScreen with regions drawn | ⬜ Pending |
| 2 | `02-region-label-picker.png` | Label picker (if implemented) | ⬜ Pending |
| 3 | `03-review-screen.png` | ReviewScreen with parsed data | ⬜ Pending |
| 4 | `04-review-edit.png` | ReviewScreen mid-edit | ⬜ Pending |
| 5 | `05-record-list.png` | RecordListScreen | ⬜ Pending |
| 6 | `06-stats-screen.png` | StatsScreen with charts | ⬜ Pending |

## Optional

| # | File | Screen | Status |
|---|------|--------|--------|
| 7 | `07-save-confirmed.png` | "Saved ✓" state | ⬜ Pending |
| 8 | `08-csv-export.png` | CSV export / share sheet | ⬜ Pending |

## Capture Command

```bash
# Emulator screenshot via adb
adb exec-out screencap -p > screenshots/XX-description.png

# Or use emulator toolbar camera button
```

## Quality Checklist

- [ ] 1080p resolution minimum
- [ ] Realistic test data (not "Test", "asdf")
- [ ] No debug overlay or system notifications visible
- [ ] File naming follows `XX-description.png` pattern
