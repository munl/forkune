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
preferences wrapper (§2b). Also write the chosen place through to the Home `lastPick*`
properties.

### Acceptance criteria
- [ ] Favorite toggle persists across restart.
- [ ] Selecting a pick updates the Home last-pick preference.

## Task: YourPickViewModel
labels: layer:viewmodel, screen:your-pick
depends-on: Preferences — favorites

Holds the chosen `Restaurant` (passed via nav args or resolved from the repository),
exposes it and its favorite state as `StateFlow`, and provides `toggleFavorite()` and
a directions intent payload. No Compose imports.

### Acceptance criteria
- [ ] Exposes the pick + favorite state via `.asStateFlow()`.
- [ ] `toggleFavorite()` persists via preferences.
- [ ] Provides the data needed to launch directions (name/coords/address).

## Task: Strings — Your pick
labels: layer:strings, screen:your-pick
depends-on: none

Add copy to `strings.xml` under `<!-- Your pick -->`: "TONIGHT'S PICK", rating/review
template, price·distance template, "Open now", "Until {time}", Get directions, Roll
again.

### Acceptance criteria
- [ ] No hard-coded literals in the View; templated values use placeholders.

## Task: YourPickView (stateless UI)
labels: layer:view, screen:your-pick
depends-on: Strings — Your pick

Stateless hero card (accent block + initials, cuisine tag, name, rating·reviews·price·
distance row, Open-now + hours chips) plus the header (close + favorite) and the two
action buttons. Styles via `AppTheme`; favorite icon reflects state.

### Acceptance criteria
- [ ] Matches the mock: hero card, meta row, status chips, two stacked action buttons.
- [ ] `onGetDirectionsClicked` / `onRollAgainClicked` / `onFavoriteToggled` / `onCloseClicked` typealiases.
- [ ] Favorite heart reflects the current favorite state.

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

Add the route (accepts the chosen place id/args as serializable primitives) and wire it
in the nav graph; reachable from Shuffling on stop.

### Acceptance criteria
- [ ] Reachable from Shuffling with the chosen place as serializable args.

## Task: DI — register YourPickViewModel
labels: layer:di, screen:your-pick
depends-on: YourPickViewModel

Register `YourPickViewModel` in both `viewModelModule` actuals.

### Acceptance criteria
- [ ] Registered android + ios; `get()` count matches constructor.

## Task: Tests — YourPickViewModel
labels: layer:test, screen:your-pick
depends-on: YourPickViewModel

Test favorite toggling and last-pick write-through with a fake preferences store and a
known `Restaurant`.

### Acceptance criteria
- [ ] `toggleFavorite()` flips and persists state.
- [ ] Selecting the pick writes the Home last-pick preference.
