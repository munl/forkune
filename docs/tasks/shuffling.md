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

## Task: ShufflingView (stateless UI)
labels: layer:view, screen:shuffling
depends-on: Strings — Shuffling

Stateless animated card stack (current candidate name + cuisine·rating·distance), a
page-dot indicator, title/subtitle, and a full-screen tap target that calls
`onStopClicked`. Animation matches the mock's shuffle feel.

### Acceptance criteria
- [ ] Renders the current candidate card and dots from parameters.
- [ ] Tapping anywhere invokes `onStopClicked` (typealias).
- [ ] No business logic; animation state driven by inputs.

## Task: ShufflingScreen (wiring)
labels: layer:screen, screen:shuffling
depends-on: ShufflingViewModel

Wires `ShufflingViewModel` to `ShufflingView`; starts the spin in `LaunchedEffect`,
and on stop navigates to Your-pick with the chosen `Restaurant`.

### Acceptance criteria
- [ ] Starts spinning on entry; navigates to Your-pick on stop.
- [ ] Passes the selected-cuisines argument through to the repository query.

## Task: Navigation — Shuffling route
labels: layer:navigation, screen:shuffling
depends-on: ShufflingScreen

Add the route (accepts the selected-cuisines argument as serializable primitives) and
wire it in the nav graph; reachable from Home (Surprise me) and Pick cuisines.

### Acceptance criteria
- [ ] Reachable from both Surprise-me entry points.
- [ ] Passes selected cuisines as serializable args.

## Task: DI — register ShufflingViewModel & PlacesRepository
labels: layer:di, screen:shuffling
depends-on: ShufflingViewModel

Register `ShufflingViewModel` in both `viewModelModule` actuals and `PlacesRepository`
as a `single` in `appModule`.

### Acceptance criteria
- [ ] VM registered android + ios; repository registered in `appModule`.

## Task: Tests — ShufflingViewModel
labels: layer:test, screen:shuffling
depends-on: ShufflingViewModel

Test the spin/stop logic with a fake `PlacesRepository` emitting a known candidate
list; assert `stop()` selects the current candidate and the empty case is handled.

### Acceptance criteria
- [ ] Drives spin, calls `stop()`, asserts the selected `Restaurant`.
- [ ] Empty-candidate list does not crash.
