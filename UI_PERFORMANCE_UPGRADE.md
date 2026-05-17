# VaultMind UI + Fluency Upgrade

This upgrade focuses on making the app feel smoother, more premium, and more scalable when the vault grows.

## What changed

- Added a richer Material 3 design system in `core:designsystem`.
- Rebuilt the reusable card row with stronger typography, better spacing, card-type icons, attachment indicators, folder chips, pinned/favorite indicators, and smoother content sizing.
- Added reusable premium components:
  - `VaultScreen`
  - `VaultHeroCard`
  - `VaultMetricCard`
  - `VaultSearchPill`
  - `VaultSectionHeader`
  - `VaultActionChip`
  - `VaultInput`
- Upgraded Dashboard UI with a premium hero, offline search pill, metrics grid, quick actions, horizontal pinned section, top tags, and activity cards.
- Upgraded Knowledge list, Detail, and Create/Edit screens.
- Upgraded Search UI with a polished ranking explanation, search box, filters, history, loading and empty states.
- Upgraded Tags, Folders, Collections, Pinned, Backup, Settings, and Recent Activity screens.
- Improved bottom navigation so it only appears on top-level screens, not detail/edit/management screens.

## Fluency/performance decisions

- Lazy lists now use stable keys and content types.
- Lazy list states are remembered where useful.
- Large detail/edit pages use vertical scroll while large card collections use `LazyColumn`.
- Heavy work is still outside composables through ViewModels, repositories, Room, DataStore, and use cases.
- Card previews use concise text and avoid rendering full bodies inside list rows.
- Attachment previews use Coil only for image/screenshot rows.
- Bottom navigation uses state restoration for top-level destinations.
- UI components are reusable and centralized to avoid duplicated recomposition-heavy layouts across features.

## Next professional upgrades

For an even more production-grade app later, add:

- Paging 3 for extremely large vaults.
- Room FTS4/FTS5 virtual tables for very fast full-text search.
- Baseline Profiles for faster startup and smoother scrolling in release builds.
- Macrobenchmark module for startup/scroll performance testing.
- Real file picker integration using `ActivityResultContracts.OpenDocument`.
- Thumbnail generation worker for PDFs/images.
