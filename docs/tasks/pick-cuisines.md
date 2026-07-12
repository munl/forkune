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
selection survives navigation and restart. Encode as a delimited string or key-per-flag.

### Acceptance criteria
- [ ] Selected cuisine keys read/written via the existing preferences wrapper.
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

## Task: PickCuisinesView (stateless UI)
labels: layer:view, screen:pick-cuisines
depends-on: Strings — Pick cuisines

Stateless grid of selectable cuisine cards (name + spot count + selected checkmark),
back button, and the bottom Surprise-me CTA with the selected-count badge. Styles via
`AppTheme`; selected vs unselected states match the mock (filled orange vs outline).

### Acceptance criteria
- [ ] Two-column grid; selected cards render filled with a checkmark.
- [ ] `onCuisineToggled` / `onBackClicked` / `onSurpriseClicked` callbacks as typealiases.
- [ ] CTA shows the live selected count.

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

Add the route to `ScreenDestinations.kt` and wire it in the nav graph; reachable from
Home's "Pick cuisines" button.

### Acceptance criteria
- [ ] Navigable from Home; back returns to Home.

## Task: DI — register PickCuisinesViewModel
labels: layer:di, screen:pick-cuisines
depends-on: PickCuisinesViewModel

Register in both `viewModelModule` actuals (android + ios).

### Acceptance criteria
- [ ] Registered in androidMain and iosMain; `get()` count matches constructor.

## Task: Tests — PickCuisinesViewModel
labels: layer:test, screen:pick-cuisines
depends-on: PickCuisinesViewModel

Test selection toggling and `selectedCount` with a fake preferences store.

### Acceptance criteria
- [ ] Toggling a cuisine updates selection and count.
- [ ] Selection persists across ViewModel re-creation (fake store round-trip).
