# VaultMind build fixes

This package keeps the app modules and features intact. The fixes target the recurring Gradle/Kotlin/KAPT blockers seen on Windows.

## Fixed areas

- Removed the invalid relative JVM temp setting that caused Gradle sync to fail before project configuration.
- Added an absolute Windows temp path for the Gradle daemon and SQLite JDBC extraction.
- Kept a project-local `.gradle/tmp` directory for Room/KAPT task-level native extraction.
- Replaced the AndroidX Hilt Worker annotation-processor path with a normal injected `WorkerFactory`.
- Kept `BackupReminderWorker` and `RecentActivityCleanupWorker` as real WorkManager workers.
- Kept Room, Hilt, WorkManager, Compose, DataStore, and all feature modules.
- Updated deprecated `hiltViewModel` imports to the new package.
- Updated deprecated auto-mirrored Material icons.

## Important clean build command

Run this once after extracting:

```bash
./gradlew --stop
./gradlew clean :app:assembleDebug --no-build-cache
```

In Android Studio, also use **File > Invalidate Caches / Restart** if old generated KAPT stubs remain.

## v7 navigation compile fix

The v6 build reached `:app:compileDebugKotlin` after all feature/core modules compiled, then failed in `VaultMindApp.kt` because several screen calls used positional trailing lambdas while the screen functions keep their ViewModel parameter after the callback parameter with a default value.

Example of the wrong call shape:

```kotlin
SearchScreen { nav.navigate("card/${it.id}") }
```

Kotlin treated that trailing lambda as the last parameter (`viewModel`) instead of `onOpenCard`.

The calls were changed to named arguments, for example:

```kotlin
SearchScreen(onOpenCard = { nav.navigate("card/${it.id}") })
```

The same named-argument fix was applied for Cards, Search, Pinned, Settings, Tags, Folders, Collections, Backup, and Activity routes.
