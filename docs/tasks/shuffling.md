---
screen: Shuffling
slug: shuffling
purpose: Animated "finding your spot" state that shuffles through nearby candidate places, tap-to-stop, then routes to the pick.
source: docs/foodpicker app.pdf — screen 3 (Shuffling)
navigable: yes (pushed when a spin starts)
persistence: none (in-memory during the spin)
domain_model: Restaurant + PlacesRepository (new; shared across screens)
use_case: no
---

# Tracking: Shuffling screen

Transient screen shown while the app "spins" the nearby menu: a stacked/animated card
cycles through candidate restaurants ("Shuffling 30 places near you"), a page-dot
indicator, and "Tap anywhere to stop the wheel." On stop it lands on a `Restaurant`
and navigates to the Your-pick screen.

This epic also introduces the **shared** `Restaurant` domain model and
`PlacesRepository` (ktor-backed nearby-places source) that Home and Your-pick reuse —
build these first.

## Task: Domain model — Restaurant
labels: layer:domain, screen:shuffling
depends-on: none

Shared `Restaurant` domain model (clean-architecture §1): `name`, `cuisine`,
`rating`, `reviewCount`, `priceLevel`, `distanceMi`, `isOpenNow`, `openUntil`, plus a
display accent/initials for the card. `@Serializable`, no data-layer coupling. Reused
by Home (last pick), Shuffling (candidates), and Your-pick (result).

### Acceptance criteria
- [ ] Plain data class in `datamodels/` covering every field shown across the mocks.
- [ ] No Room/ktor annotations on the domain model.

## Task: Data — PlacesRepository (nearby places)
labels: layer:data, screen:shuffling
depends-on: Domain model — Restaurant

Repository that supplies nearby places (the "42 places within a mile" / "30 places
near you" set), filtered by selected cuisines. Uses the existing ktor client; maps
network/DTO ↔ `Restaurant` at its boundary; exposes results as `StateFlow`.

### Acceptance criteria
- [ ] Exposes a way to fetch/observe nearby places filtered by cuisine selection.
- [ ] Mapping to `Restaurant` happens only in the repository.
- [ ] Registered as a `single` in `appModule`.

## Task: ShufflingViewModel
labels: layer:viewmodel, screen:shuffling
depends-on: Data — PlacesRepository (nearby places)

Drives the shuffle: pulls candidates from `PlacesRepository`, cycles a "current card"
on an interval, exposes the candidate count and current card as `StateFlow`, and a
`stop()` that freezes on the current `Restaurant` and signals the result.

### Acceptance criteria
- [ ] Emits a changing "current candidate" while spinning.
- [ ] `stop()` selects the current `Restaurant` and exposes it for navigation.
- [ ] Handles the empty-candidates case gracefully.

## Task: Strings — Shuffling
labels: layer:strings, screen:shuffling
depends-on: none

Add copy to `strings.xml` under `<!-- Shuffling -->`: "Finding your spot…", the
"Shuffling N places near you" template, and "Tap anywhere to stop the wheel."

### Acceptance criteria
- [ ] Count uses a placeholder resource; no hard-coded literals in the View.
- [ ] Every placeholder is **indexed** (`%1$d` / `%1$s`), never bare `%d` / `%s`. Compose Resources only substitutes indexed placeholders — a bare one renders as the literal text "%d" at runtime, with nothing failing at compile time.
- [ ] No `<item quantity="zero">` in any plural. English ICU rules have no `zero` category — 0 selects `other` — so the item is dead and 0 renders as "0 places". If 0 needs different copy, give it its own string and branch in the View.

## Task: ShufflingView (stateless UI)
labels: layer:view, screen:shuffling
depends-on: Strings — Shuffling

Stateless animated card stack (current candidate name + cuisine·rating·distance), a
page-dot indicator, title/subtitle, and a full-screen tap target that calls
`onStopClicked`. Animation matches the mock's shuffle feel.

Styling: `AppTheme` installs MaterialTheme, so colors and type come from
`MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*`, brand accents from
`Variables.Colors.*`, and all spacing/sizing from `Variables.Dimensions.*`. Text via
`stringResource` / `pluralStringResource`.

### Acceptance criteria
- [ ] Renders the current candidate card and dots from parameters.
- [ ] Tapping anywhere invokes `onStopClicked` (typealias).
- [ ] No business logic; animation state driven by inputs.
- [ ] No hard-coded colors, dimensions or string literals — `MaterialTheme` / `Variables.Colors` / `Variables.Dimensions` / `stringResource` only.

## Task: ShufflingScreen (wiring)
labels: layer:screen, screen:shuffling
depends-on: ShufflingViewModel

Wires `ShufflingViewModel` to `ShufflingView`; starts the spin in `LaunchedEffect`, and
on stop navigates to Your-pick. Per clean-architecture §8 the destination carries
**serializable primitives only** — pass the chosen place's id/key, not the `Restaurant`
object, and let Your-pick resolve it from `PlacesRepository`.

### Acceptance criteria
- [ ] Obtains the VM via `koinViewModel`, collects flows with `collectAsState`; no business logic in the Screen.
- [ ] Starts spinning on entry; on stop pushes the Your-pick destination via `backStack.add(...)` with the pick's id as a primitive.
- [ ] Passes the selected-cuisines argument (read off the typed nav key) through to the repository query.

## Task: Navigation — Shuffling route
labels: layer:navigation, screen:shuffling
depends-on: ShufflingScreen

Promote `ScreenDestinations.Shuffling` from a `@Serializable data object` to a
`@Serializable data class` carrying the selected cuisines as serializable primitives
(e.g. `val cuisines: List<String>`), and render it from the Navigation3 `entryProvider`
in `MainNavGraph.kt` via
`entry<ScreenDestinations.Shuffling> { key -> ShufflingScreen(cuisines = key.cuisines, ...) }`,
replacing the interim `PlaceholderScreen`.

Args are read straight off the typed key — Navigation3 has no `toRoute()`. Navigation is
`backStack.add(...)` / `backStack.removeLastOrNull()`.

### Acceptance criteria
- [ ] Reachable from both Surprise-me entry points — Home and Pick cuisines — via `backStack.add(ScreenDestinations.Shuffling(...))`.
- [ ] Selected cuisines carried as serializable primitives on the destination itself.
- [ ] Registration in `navSavedStateConfiguration` updated for the `data class`, and `ScreenDestinationsSerializationTest` round-trips a representative instance (a `data object` instance no longer compiles there).
- [ ] The `PlaceholderScreen` entry is removed.

## Task: DI — register ShufflingViewModel & PlacesRepository
labels: layer:di, screen:shuffling
depends-on: ShufflingViewModel

Register `ShufflingViewModel` in the single `commonMain` `viewModelModule` as
`viewModelOf(::ShufflingViewModel)`, and bind the real `PlacesRepository` as a `single`
in `appModule` (clean-architecture §7). Koin's `viewModelOf` is multiplatform — no
`expect`/`actual` split.

### Acceptance criteria
- [ ] VM registered once in `commonMain/…/di/ViewModelModule.kt` via `viewModelOf(::ShufflingViewModel)`; no platform actuals.
- [ ] `PlacesRepository` bound as `single<PlacesRepository> { … }` in `appModule`, replacing the interim `InMemoryPlacesRepository` binding.
- [ ] Retrieved only via `koinViewModel<ShufflingViewModel>()` / constructor injection.

## Task: Tests — ShufflingViewModel
labels: layer:test, screen:shuffling
depends-on: ShufflingViewModel

Test the spin/stop logic with a fake `PlacesRepository` emitting a known candidate
list; assert `stop()` selects the current candidate and the empty case is handled.

### Acceptance criteria
- [ ] Drives spin, calls `stop()`, asserts the selected `Restaurant`.
- [ ] Empty-candidate list does not crash.
