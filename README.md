# VaultMind

VaultMind is a native Android, offline-first personal knowledge vault and document organizer built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, Hilt, WorkManager, Firebase Analytics/Crashlytics, GitHub Actions, and Fastlane.

## Open in Android Studio

1. Open this folder in Android Studio.
2. Use JDK 17.
3. Sync Gradle.
4. Replace `app/google-services.json` with a real Firebase file before production release. The included file is a placeholder so the project structure is complete.
5. Run the `app` debug variant.

## Generate a wrapper locally

This archive intentionally does not include a binary Gradle wrapper jar. On a development machine with Gradle installed, run:

```bash
gradle wrapper --gradle-version 9.4.1
```

Then use:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew ktlintCheck
./gradlew detekt
```

## Important integration points

- File picker: connect `ActivityResultContracts.OpenDocument` in `feature:knowledge` and persist URI permissions.
- Backup export/import: replace the placeholder repository in `core:database` with JSON serialization of Room tables inside transactions.
- Firebase: replace placeholder config and connect a real Firebase project.
- Future AI/OCR/encryption/cloud sync/collaboration: add new modules without changing Compose screens directly.
