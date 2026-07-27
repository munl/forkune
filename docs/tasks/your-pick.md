---
screen: Your pick
slug: your-pick
purpose: Result screen showing the chosen restaurant with actions to get directions, roll again, favorite, or dismiss.
source: docs/foodpicker app.pdf — screen 4 (Your pick)
navigable: yes (pushed from Shuffling)
persistence: preferences (favorites, last pick write-through)
domain_model: none new (reuses Restaurant from shuffling.md)
use_case: no
---

# Tracking: Your pick screen

The payoff screen: "TONIGHT'S PICK" header with close + favorite icons, a large
restaurant hero card (accent block + initials, name, rating·reviews·price·distance,
Open-now + hours chips), and two actions — **Get directions** (primary) and **Roll
again** (secondary, returns to shuffling). Selecting a pick also writes it as the
Home "last pick".

Cross-screen dependency: reuses the `Restaurant` domain model and `PlacesRepository`
from `shuffling.md`, and writes `lastPick*` via the preferences task in `home.md`.

## Task: Preferences — favorites
labels: layer:data, screen:your-pick
depends-on: none

Persist favorited places (toggled by the heart icon) as a simple primitive set via the
preferences wrapper (§2b) — encode as a delimited string through the existing
`setString` / `getStringOrNull` accessors. Also write the chosen place through to the
Home last-pick properties.

### Acceptance criteria
- [ ] Favorite toggle persists across restart.
- [ ] Selecting a pick updates the Home last-pick preferences (`lastPickName` + `lastPickAt`) — this is the only writer of those keys, which the Home ViewModel already reads.
- [ ] `AppPreferencesInterface` carries only the primitive accessors actually in use — if a new type is genuinely needed, add the getter/setter pair to the interface, both platform actuals (`AndroidPreferences`, `IosPreferences`) **and** the `commonTest` fake in the same change.

## Task: YourPickViewModel
labels: layer:viewmodel, screen:your-pick
depends-on: Preferences — favorites

Resolves the chosen `Restaurant` from `PlacesRepository` using the id handed in via nav
args (§8: destinations carry serializable primitives, not domain objects), exposes it
and its favorite state as `StateFlow`, and provides `toggleFavorite()` plus a directions
payload. No Compose or platform imports.

### Acceptance criteria
- [ ] Exposes the pick + favorite state via `.asStateFlow()`; backing `MutableStateFlow`s are private.
- [ ] Resolves the pick from `PlacesRepository` by id rather than receiving a `Restaurant` through navigation.
- [ ] `toggleFavorite()` persists via `AppPreferences`, and confirming the pick writes `lastPickName` / `lastPickAt`.
- [ ] Provides the data needed to launch directions (name/coords/address).
- [ ] Constructor-injected dependencies only.

## Task: Strings — Your pick
labels: layer:strings, screen:your-pick
depends-on: none

Add copy to `strings.xml` under `<!-- Your pick -->`: "TONIGHT'S PICK", rating/review
template, price·distance template, "Open now", "Until {time}", Get directions, Roll
again.

### Acceptance criteria
- [ ] No hard-coded literals in the View; templated values use placeholders.
- [ ] Every placeholder is **indexed** (`%1$s`, `%2$s`, …), never bare `%s` / `%d`. Compose Resources only substitutes indexed placeholders — a bare one renders as the literal text "%s" at runtime, with nothing failing at compile time. The multi-arg templates here (rating·reviews, price·distance) need indices regardless.
- [ ] No `<item quantity="zero">` in any plural. English ICU rules have no `zero` category — 0 selects `other` — so the item is dead and a place with no reviews renders "0 reviews". If 0 needs different copy, give it its own string and branch in the View.

## Task: YourPickView (stateless UI)
labels: layer:view, screen:your-pick
depends-on: Strings — Your pick

Stateless hero card (accent block + initials, cuisine tag, name, rating·reviews·price·
distance row, Open-now + hours chips) plus the header (close + favorite) and the two
action buttons.

Styling: `AppTheme` installs MaterialTheme, so colors and type come from
`MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*`, brand accents from
`Variables.Colors.*`, and all spacing/sizing from `Variables.Dimensions.*`. Text via
`stringResource` / `pluralStringResource`. The card's accent block uses
`Restaurant.accentColorArgb` + `initials`.

### Acceptance criteria
- [ ] Matches the mock: hero card, meta row, status chips, two stacked action buttons.
- [ ] `onGetDirectionsClicked` / `onRollAgainClicked` / `onFavoriteToggled` / `onCloseClicked` typealiases.
- [ ] Favorite heart reflects the current favorite state.
- [ ] No hard-coded colors, dimensions or string literals — `MaterialTheme` / `Variables.Colors` / `Variables.Dimensions` / `stringResource` only.

## Task: YourPickScreen (wiring)
labels: layer:screen, screen:your-pick
depends-on: YourPickViewModel

Wires `YourPickViewModel` to `YourPickView`; Roll again navigates back to Shuffling,
Get directions launches the platform maps URI (via `LocalUriHandler`), close returns
to Home.

### Acceptance criteria
- [ ] Roll again re-enters the shuffle; close returns Home.
- [ ] Get directions opens a maps URI for the pick.

## Task: Navigation — Your pick route
labels: layer:navigation, screen:your-pick
depends-on: YourPickScreen

Add `ScreenDestinations.YourPick` as a `@Serializable data class` carrying the chosen
place's id/args as serializable primitives, register it as a `subclass(...)` in
`navSavedStateConfiguration`, and render it from the Navigation3 `entryProvider` in
`MainNavGraph.kt` via `entry<ScreenDestinations.YourPick> { key -> YourPickScreen(...) }`.
Reached from Shuffling on stop.

Args are read straight off the typed key — Navigation3 has no `toRoute()`. Navigation is
`backStack.add(...)` / `backStack.removeLastOrNull()`.

### Acceptance criteria
- [ ] Reachable from Shuffling with the chosen place passed as serializable primitives on the typed key.
- [ ] Registered in `navSavedStateConfiguration` and round-tripped by `ScreenDestinationsSerializationTest`.
- [ ] Roll again / close manipulate the back stack directly (`add` / `removeLastOrNull`) rather than a `navController`.

## Task: DI — register YourPickViewModel
labels: layer:di, screen:your-pick
depends-on: YourPickViewModel

Register `YourPickViewModel` in the single `commonMain` `viewModelModule` as
`viewModelOf(::YourPickViewModel)` (clean-architecture §7). Koin's `viewModelOf` is
multiplatform — no `expect`/`actual` split.

### Acceptance criteria
- [ ] Registered once in `commonMain/…/di/ViewModelModule.kt` via `viewModelOf(::YourPickViewModel)`; no platform actuals.
- [ ] Retrieved only via `koinViewModel<YourPickViewModel>()`.

## Task: Tests — YourPickViewModel
labels: layer:test, screen:your-pick
depends-on: YourPickViewModel

Test favorite toggling and last-pick write-through with a fake preferences store and a
known `Restaurant`.

### Acceptance criteria
- [ ] `toggleFavorite()` flips and persists state.
- [ ] Selecting the pick writes the Home last-pick preference.
