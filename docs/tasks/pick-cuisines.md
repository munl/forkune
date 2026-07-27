---
screen: Pick cuisines
slug: pick-cuisines
purpose: Multi-select grid of cuisines (with per-cuisine spot counts) that constrains what the spin will choose from.
source: docs/foodpicker app.pdf — screen 2 (Pick cuisines)
navigable: yes (pushed from Home)
persistence: preferences (selected cuisine keys)
domain_model: Cuisine (new)
use_case: no
---

# Tracking: Pick cuisines screen

Grid of cuisine chips (Tacos, Sushi, Pizza, Thai, Ramen, Chinese, Burgers, Italian),
each showing a spot count and a selectable state (checkmark). A back button returns to
Home; a bottom **Surprise me (N)** CTA shows the count of selected cuisines and starts
the spin constrained to the selection. This epic tracks the domain model, selection
state, and UI for choosing cravings.

## Task: Domain model — Cuisine
labels: layer:domain, screen:pick-cuisines
depends-on: none

Plain `Cuisine` domain model (clean-architecture §1): `name`, `spotCount`, `selected`
(or track selection separately in the ViewModel). No Room annotations. `@Serializable`.

### Acceptance criteria
- [ ] Plain data class in `datamodels/`; no data-layer dependencies.
- [ ] Enumerates the cuisines shown in the mock with their spot counts.

## Task: Preferences — selected cuisines
labels: layer:data, screen:pick-cuisines
depends-on: none

Persist the user's selected cuisine keys as a simple primitive set (per §2b) so the
selection survives navigation and restart. Encode as a delimited string through the
existing `setString` / `getStringOrNull` accessors rather than adding a collection type
to the store.

### Acceptance criteria
- [ ] Selected cuisine keys read/written as one typed property on the existing `AppPreferences` wrapper; no second store.
- [ ] `AppPreferencesInterface` carries only the primitive accessors actually in use — if a new type is genuinely needed, add the getter/setter pair to the interface, both platform actuals (`AndroidPreferences`, `IosPreferences`) **and** the `commonTest` fake in the same change.
- [ ] Default selection matches the mock (Tacos, Sushi, Thai, Chinese selected).

## Task: PickCuisinesViewModel
labels: layer:viewmodel, screen:pick-cuisines
depends-on: Domain model — Cuisine

Exposes the cuisine list with selection state and a derived `selectedCount` as
`StateFlow`; `toggle(cuisine)` flips selection and persists it. No Compose imports.

### Acceptance criteria
- [ ] `selectedCount` drives the "Surprise me (N)" badge.
- [ ] Toggling updates state and persists via preferences.
- [ ] State exposed via `.asStateFlow()`.

## Task: Strings — Pick cuisines
labels: layer:strings, screen:pick-cuisines
depends-on: none

Add copy to `strings.xml` under `<!-- Pick cuisines -->`: title, subtitle
("We'll only spin from what you choose."), spot-count template, Surprise-me label.

### Acceptance criteria
- [ ] No hard-coded literals in the View; spot count uses a plurals/placeholder resource.
- [ ] Every placeholder is **indexed** (`%1$d` / `%1$s`), never bare `%d` / `%s`. Compose Resources only substitutes indexed placeholders — a bare one renders as the literal text "%d" at runtime, with nothing failing at compile time.
- [ ] No `<item quantity="zero">` in any plural. English ICU rules have no `zero` category — 0 selects `other` — so the item is dead and 0 renders as "0 spots". If 0 needs different copy, give it its own string and branch in the View.

## Task: PickCuisinesView (stateless UI)
labels: layer:view, screen:pick-cuisines
depends-on: Strings — Pick cuisines

Stateless grid of selectable cuisine cards (name + spot count + selected checkmark),
back button, and the bottom Surprise-me CTA with the selected-count badge.

Styling: `AppTheme` installs MaterialTheme, so colors and type come from
`MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*`, brand accents from
`Variables.Colors.*`, and all spacing/sizing from `Variables.Dimensions.*`. Text via
`stringResource` / `pluralStringResource`.

### Acceptance criteria
- [ ] Two-column grid; selected cards render filled with a checkmark (selected vs unselected match the mock — filled orange vs outline).
- [ ] `onCuisineToggled` / `onBackClicked` / `onSurpriseClicked` callbacks as typealiases.
- [ ] CTA shows the live selected count.
- [ ] No hard-coded colors, dimensions or string literals — `MaterialTheme` / `Variables.Colors` / `Variables.Dimensions` / `stringResource` only.

## Task: PickCuisinesScreen (wiring)
labels: layer:screen, screen:pick-cuisines
depends-on: PickCuisinesViewModel

Wires `PickCuisinesViewModel` to `PickCuisinesView`; passes navigation lambdas (back,
start shuffle with the current selection).

### Acceptance criteria
- [ ] Obtains VM via `koinViewModel`, collects flows, forwards callbacks.
- [ ] Surprise-me navigates to the Shuffling screen with the selected cuisines.

## Task: Navigation — Pick cuisines route
labels: layer:navigation, screen:pick-cuisines
depends-on: PickCuisinesScreen

`ScreenDestinations.PickCuisines` already exists as a `@Serializable data object` and is
registered in `navSavedStateConfiguration`; this task replaces its interim
`PlaceholderScreen` with the real screen. Render it from the Navigation3 `entryProvider`
in `MainNavGraph.kt` via `entry<ScreenDestinations.PickCuisines> { PickCuisinesScreen(...) }`.

Navigation is back-stack mutation (`backStack.add(...)` /
`backStack.removeLastOrNull()`) — there is no `navController`.

### Acceptance criteria
- [ ] Reachable from Home's "Pick cuisines" button via `backStack.add(ScreenDestinations.PickCuisines)`; back pops with `backStack.removeLastOrNull()`.
- [ ] The `PlaceholderScreen` entry is removed.
- [ ] Still registered in `navSavedStateConfiguration` and asserted by `ScreenDestinationsSerializationTest`.

## Task: DI — register PickCuisinesViewModel
labels: layer:di, screen:pick-cuisines
depends-on: PickCuisinesViewModel

Register `PickCuisinesViewModel` in the single `commonMain` `viewModelModule` as
`viewModelOf(::PickCuisinesViewModel)` (clean-architecture §7). Koin's `viewModelOf` is
multiplatform, so there is no `expect`/`actual` split.

### Acceptance criteria
- [ ] Registered once in `commonMain/…/di/ViewModelModule.kt` via `viewModelOf(::PickCuisinesViewModel)`; no platform actuals.
- [ ] Retrieved only via `koinViewModel<PickCuisinesViewModel>()`.

## Task: Tests — PickCuisinesViewModel
labels: layer:test, screen:pick-cuisines
depends-on: PickCuisinesViewModel

Test selection toggling and `selectedCount` with a fake preferences store.

### Acceptance criteria
- [ ] Toggling a cuisine updates selection and count.
- [ ] Selection persists across ViewModel re-creation (fake store round-trip).
