# StudentOS

A production-ready, offline-first Android application — your personal Student Operating System.

## Overview

StudentOS is a comprehensive academic management app designed for university students. It operates entirely offline with all data stored locally on the device. No accounts, no servers, no subscriptions.

## Features

- **Today Dashboard** — See what's happening now, next class, pending tasks, and smart recommendations
- **Schedule Management** — Day/Week/Month views with recurring classes, room assignments, and teacher links
- **Task System** — Full task management with priorities, deadlines, subtasks, and estimated durations
- **Grade Tracking** — Record grades by category, calculate weighted averages, and determine required exam scores
- **Attendance Monitoring** — Track attendance with configurable thresholds and safe-absence calculations
- **Exam Preparation** — Track upcoming exams with preparation progress indicators
- **Smart Planner** — Deterministic recommendation engine that analyzes your deadlines, priorities, and attendance
- **Analytics Dashboard** — Visual breakdowns of attendance, grades, and task completion by subject
- **Notes** — Quick note-taking linked to subjects
- **OCR** — Scan whiteboards/textbooks using on-device ML Kit text recognition
- **Natural Language Input** — Parse commands like "По физике сделать лаб 3 до пятницы"
- **Backup & Restore** — Full JSON backup of all data with version tracking
- **Widgets** — Home screen widgets for next class and task summary
- **Notifications** — Smart reminders for classes, deadlines, and exams
- **Biometric Lock** — Optional PIN/biometric app lock
- **Localization** — Full Russian and English support
- **Dark Mode** — Complete dark theme support

## Architecture

```
UI (Compose)
  ↓
Presentation (ViewModel + StateFlow)
  ↓
Domain (Use Cases)
  ↓
Data (Repository + Room DAO)
  ↓
Local Database (Room/SQLite)
```

- **Clean Architecture** with strict layer separation
- **Hilt** for dependency injection
- **Room** for local database with migrations, indexes, and foreign keys
- **Coroutines + Flow** for reactive data streams
- **Jetpack Compose** for declarative UI
- **Glance** for home screen widgets
- **WorkManager** for background notifications

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material 3 |
| Database | Room 2.6 |
| DI | Hilt 2.51 |
| Navigation | Navigation Compose 2.7 |
| Background | WorkManager 2.9 |
| Preferences | DataStore |
| OCR | ML Kit Text Recognition |
| Widgets | Glance 1.1 |
| Biometric | AndroidX Biometric |
| Testing | JUnit 4 + MockK + Turbine |

## Project Structure

```
app/src/main/java/com/studentos/app/
├── StudentOSApplication.kt
├── MainActivity.kt
├── di/                     # Hilt modules
├── data/
│   ├── local/database/     # Room entities, DAOs, converters
│   └── repository/         # Repository implementations
├── domain/
│   ├── model/              # Domain models
│   ├── repository/         # Repository interfaces
│   └── usecase/            # Business logic use cases
├── ui/
│   ├── theme/              # Design system (colors, typography, shapes)
│   ├── components/         # Reusable UI components
│   ├── navigation/         # Navigation graph
│   └── screen/             # Feature screens
├── ai/                     # AI provider abstraction
├── notifications/          # Notification channels and workers
├── widget/                 # Glance home screen widgets
├── backup/                 # Backup/restore system
├── ocr/                    # On-device text recognition
└── util/                   # Utilities
```

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Requirements

- Android SDK 35 (compileSdk)
- Minimum SDK 26 (Android 8.0)
- JDK 17
- Gradle 8.7

## Privacy

- All data stored locally in Room database
- No network calls for core functionality
- No analytics or tracking
- API keys stored via Android Keystore
- Optional biometric lock
- Backup files stored in app-private external storage

## License

See LICENSE file.
