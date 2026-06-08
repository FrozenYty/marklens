# MarkLens

Exam paper digitization — photograph paper exam sheets, extract student info
and scores via OCR, and review statistics.

## Overview

MarkLens lets teachers digitize paper exam results by:

1. **Photograph** a paper exam sheet via CameraX or pick from gallery
2. **Select regions** by drawing bounding boxes (name, student ID, question scores, total)
3. **OCR** each region with Google ML Kit (offline, free)
4. **Review & correct** parsed results before saving
5. **Browse & analyze** with built-in statistics and CSV export

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository |
| Database | Room |
| OCR | Google ML Kit Text Recognition v16 |
| Camera | CameraX |
| Build | Gradle Kotlin DSL |
| Min SDK | 26 |

## Project Structure

```
marklens/
├── .claude/CLAUDE.md        # Project-level AI collaboration rules
├── .github/workflows/ci.yml # CI: lint + unit tests + build
├── API-SPEC.md              # Full API contracts (entities, DAOs, UiState, routes)
├── TASK-BRIEF.md            # Feature requirements + 5-phase plan
├── README.md                # You are here
├── PROGRESS.md              # Session state & progress tracking
├── LICENSE                  # MIT (Tianyu Yao, Jianheng Sun)
├── test-docs/
│   ├── test-plan.md         # Testing strategy + TC outlines
│   ├── test-cases.md        # TC specs (append as implemented)
│   └── test-summary-report.md  # Final report
├── gradle/
│   ├── libs.versions.toml   # Version catalog
│   └── wrapper/             # Gradle wrapper
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/java/com/example/marklens/
│       │   ├── data/        # Room entities, DAOs, Database, Repository
│       │   ├── ocr/         # ML Kit wrapper, region mapping
│       │   ├── parser/      # OCR text → structured entities
│       │   ├── ui/          # Compose screens + ViewModels
│       │   └── di/          # Manual dependency injection
│       ├── test/            # Unit tests (JUnit 5: DAO, Repository, ViewModel, Parser)
│       └── androidTest/     # Instrumented tests (JUnit 4: Compose UI, Room integration)
└── screenshots/             # Test evidence
```

## Getting Started

```bash
# Set up environment
export JAVA_HOME=<path-to-jdk-21>
export ANDROID_HOME=<path-to-android-sdk>

# Build
cd marklens
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## Team

| Name | GitHub | Role |
|------|--------|------|
| Tianyu Yao | [FrozenYty](https://github.com/FrozenYty) | Project Lead |
| Jianheng Sun | [chemflowers](https://github.com/chemflowers) | Developer |

## License

MIT — see [LICENSE](LICENSE)
