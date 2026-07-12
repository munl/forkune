---
screen: Home
slug: home
purpose: App entry screen — "What should you eat tonight?" with the primary Surprise-me action, location, and shortcuts to cuisines/filters.
source: docs/foodpicker app.pdf — screen 1 (Home)
navigable: yes (start destination)
persistence: preferences (last pick, selected location)
domain_model: none new (reuses Restaurant from shuffling.md)
use_case: no
---

# Tracking: Home screen

Landing screen for Forked. Shows the user's location, a headline count of nearby
places, a large **Surprise me** button that spins the whole nearby menu, secondary
**Pick cuisines** / **Filters** buttons, and a "Last pick" footer. This epic tracks
the presentation, preference, and wiring tasks for the Home screen.

Cross-screen dependency: the "Last pick" footer and the place count reuse the
`Restaurant` domain model and `PlacesRepository` introduced in `shuffling.md` —
land those first (or in parallel).

## Task: Preferences — last pick & location
labels: layer:data, screen:home
depends-on: none

Add typed properties to the app preferences wrapper (per clean-architecture §2b) for
the small primitives Home persists: the currently selected location label and the
"last pick" summary (name + timestamp). Store primitives only; no Room.

### Acceptance criteria
- [ ] `AppPreferences` exposes `selectedLocation: String`, `lastPickName: String?`, `lastPickAt: Long?` (sentinel-encoded nullables).
- [ ] No new preference store is created; properties added to the existing wrapper.
- [ ] Values survive app restart.

## Task: HomeViewModel
labels: layer:viewmodel, screen:home
depends-on: Preferences — last pick & location

`HomeViewModel` (clean-architecture §3): exposes `StateFlow` for the location label,
the nearby-place count, and the last-pick footer text; reads them from preferences
and `PlacesRepository`. No Compose/platform imports.

### Acceptance criteria
- [ ] State exposed as `.asStateFlow()`; backing `MutableStateFlow` is private.
- [ ] `load()` populates location, place count ("42 places within a mile"), and last-pick footer.
- [ ] Constructor-injected dependencies only (prefs, places repo).

## Task: Strings — Home
labels: layer:strings, screen:home
depends-on: none

Add all user-facing Home copy to `composeResources/values/strings.xml` under a
`<!-- Home -->` section: headline, subtitle, Surprise-me label + caption, Pick
cuisines, Filters, last-pick template.

### Acceptance criteria
- [ ] No hard-coded UI literals remain in the View.
- [ ] Placeholders used for dynamic count/last-pick values.

## Task: HomeView (stateless UI)
labels: layer:view, screen:home
depends-on: Strings — Home

Stateless `HomeView` (clean-architecture §4): location chip + profile icon row,
headline + subtitle, the primary Surprise-me card, secondary Pick cuisines / Filters
buttons, last-pick footer. Inputs are plain data + callbacks; styles via `AppTheme` +
`Variables.Dimensions`, text via `stringResource`.

### Acceptance criteria
- [ ] Holds no ViewModel and no business logic; all data/callbacks are parameters.
- [ ] Callbacks declared as typealiases (`onSurpriseClicked`, `onPickCuisinesClicked`, `onFiltersClicked`, `onProfileClicked`).
- [ ] Matches the mock layout (headline, prominent CTA, two secondary buttons, footer).

## Task: HomeScreen (wiring)
labels: layer:screen, screen:home
depends-on: HomeViewModel

`HomeScreen` (clean-architecture §5): obtains `HomeViewModel` via `koinViewModel`,
collects its flows with `collectAsState`, triggers `load()` in `LaunchedEffect`, and
passes state + navigation callbacks down to `HomeView`.

### Acceptance criteria
- [ ] Navigation lambdas (to Pick cuisines, Filters, spin/shuffle, profile) passed in from the nav graph.
- [ ] No business logic in the Screen; only wiring.

## Task: Navigation — Home route (start destination)
labels: layer:navigation, screen:home
depends-on: HomeScreen

Add the Home route to `navigation/destinations/ScreenDestinations.kt` as a
`@Serializable data object`, set it as the start destination, and wire a
`composable<...>` block in the nav graph that renders `HomeScreen` with its
navigation lambdas.

### Acceptance criteria
- [ ] Home is the app's start destination.
- [ ] Surprise me / Pick cuisines / Filters navigate to their destinations.

## Task: DI — register HomeViewModel
labels: layer:di, screen:home
depends-on: HomeViewModel

Register `HomeViewModel` in BOTH `viewModelModule` actuals (android `viewModel { ... }`
+ ios `factoryOf(::HomeViewModel)`), keeping the `get()` count in sync with the
constructor.

### Acceptance criteria
- [ ] Registered in androidMain and iosMain actuals.
- [ ] Retrieved only via `koinViewModel<HomeViewModel>()`.

## Task: Tests — HomeViewModel
labels: layer:test, screen:home
depends-on: HomeViewModel

Unit-test `HomeViewModel` in `commonTest` with a fake preferences + fake places repo
that emit controlled values; assert the exposed `StateFlow`s (location, count,
last-pick footer).

### Acceptance criteria
- [ ] Test builds the ViewModel with fakes, drives `load()`, advances the dispatcher, asserts state.
- [ ] Covers the empty/first-run case (no last pick).
