# VaultMind Project Guide

## 1. Final Android app understanding
VaultMind is an offline-first private knowledge vault for notes, PDFs, screenshots, files, links, ideas, research notes, and code snippets. It is designed as a native modular Android foundation similar in spirit to a small offline Notion/Obsidian-style app.

## 2. Complete feature list
Dashboard, knowledge cards, card create/edit/detail, tags, folders, collections, offline search, related-card suggestions, pinned cards, favorites, recent activity, attachment metadata/previews, backup/restore architecture, settings, WorkManager cleanup/reminders, analytics abstraction, testing, quality gates, CI, and Fastlane.

## 3. Full multi-module Android project structure
Modules: `app`, `core:common`, `core:model`, `core:database`, `core:datastore`, `core:network`, `core:domain`, `core:designsystem`, `core:analytics`, `core:testing`, `feature:dashboard`, `feature:knowledge`, `feature:search`, `feature:tags`, `feature:folders`, `feature:collections`, `feature:pinned`, `feature:backup`, `feature:settings`.

## 4. Architecture explanation
Compose UI talks to focused ViewModels. ViewModels call use cases for complex behavior and repositories for data. Repositories use Room, DataStore, WorkManager, Retrofit, and Firebase behind abstractions. Domain models stay independent from database entities.

## 5. Database design
Room entities include knowledge cards, tags, card-tag cross refs, folders, collections, card-collection cross refs, attachments, recent activity, search history, and backup metadata. Indices are added for title, updated date, card type, pinned, favorite, folder, tag, and collection relations.

## 6. Gradle Kotlin DSL setup
Root, app, core, and feature modules use Kotlin DSL and version catalogs. Android app and library modules are configured with JDK 17, Compose, Hilt, and module dependencies.

## 7. Version Catalog setup
`gradle/libs.versions.toml` centralizes Android Gradle Plugin, Kotlin, Compose BOM, Hilt, Room, DataStore, WorkManager, Retrofit, OkHttp, Moshi, Coil, Firebase, coroutines, testing, ktlint, and detekt versions.

## 8. Core module code
Core modules contain common dispatchers, domain models, repository contracts, Room entities/DAOs/mappers/repositories, DataStore settings, network clients, design system components/theme, analytics abstraction, and test fakes.

## 9. Feature module code
Feature modules implement Dashboard, Knowledge, Search, Tags, Folders, Collections, Pinned, Backup, and Settings screens with dedicated ViewModels.

## 10. ViewModels and UI states
Every major screen has immutable UI state and StateFlow. Create/edit includes one-time saved events. Loading, empty, success, and error states are represented in screens.

## 11. Navigation Compose setup
`app/src/main/java/com/vaultmind/VaultMindApp.kt` owns bottom navigation, FAB, and all feature destinations.

## 12. Repository and use case code
Repositories live behind interfaces in `core:domain`. Complex logic is implemented in use cases such as `SaveKnowledgeCardUseCase`, `SearchKnowledgeVaultUseCase`, `GetDashboardUseCase`, and `GetRelatedKnowledgeCardsUseCase`.

## 13. Room database code
Room database code is in `core:database/src/main/java/com/vaultmind/core/database`. The database is the source of truth for local cards, organization, attachments, history, and backup metadata.

## 14. DataStore code
Settings are stored in `core:datastore/src/main/java/com/vaultmind/core/datastore/SettingsDataStore.kt`.

## 15. WorkManager code
Backup reminder and recent activity cleanup workers live in `feature:backup/src/main/java/com/vaultmind/feature/backup/BackupWorkers.kt`.

## 16. Firebase Analytics and Crashlytics setup
`core:analytics` contains `AnalyticsTracker`, event names, Firebase-backed implementation, and Hilt bindings. The app includes a placeholder `google-services.json` that must be replaced for production.

## 17. Testing setup
Unit tests cover card ViewModel, search ViewModel, related-card use case, repository contract, Flow/Turbine behavior, and settings flow. Compose UI tests cover dashboard, search, and create-to-detail flow.

## 18. ktlint, Detekt, and Android Lint setup
Root build applies ktlint and detekt to subprojects. `.editorconfig`, `config/detekt/detekt.yml`, and Android lint tasks are included.

## 19. GitHub Actions setup
`.github/workflows/android.yml` builds, tests, lints, runs ktlint, and runs detekt using JDK 17 and Gradle 9.4.1.

## 20. Fastlane setup
`fastlane/Fastfile` includes quality, release bundle, and internal track lanes. `fastlane/Appfile` contains package metadata.

## 21. Android Studio run instructions
Open the folder, use JDK 17, sync Gradle, replace Firebase config for production, and run the debug app. Generate the Gradle wrapper locally if desired.

## 22. Final verification checklist
- Native Kotlin Android only.
- Jetpack Compose and Material 3 only.
- Multi-module feature/core architecture.
- Room source of truth.
- DataStore settings.
- Hilt DI.
- WorkManager background workers.
- Coil attachment preview component.
- Firebase analytics abstraction.
- Offline search ranking and related-card use case.
- Testing, ktlint, detekt, Android lint, GitHub Actions, and Fastlane included.


## Gradle 9.4.1 sync fix

This project includes `gradle/wrapper/gradle-wrapper.properties` pinned to `gradle-9.4.1-bin.zip` because Android Gradle Plugin 9.2.0 requires Gradle 9.4.1.
