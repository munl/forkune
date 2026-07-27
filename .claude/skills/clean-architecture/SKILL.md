---
name: clean-architecture
description: >-
  Construct a new feature (or app) following a KMP + Compose Multiplatform clean-
  architecture convention: presentation (Screen → View → ViewModel), optional data
  layer (Room Repository/Dao/RoomModel or a key-value preferences wrapper), domain
  models, Koin DI, navigation, and tests. Use this whenever the user asks to add,
  build, or scaffold a new feature, screen, ViewModel, or repository. ALWAYS start
  with Phase 0 (Discovery) and interview the user thoroughly before writing any
  code, then run the Decision Checklist. Several layers are optional. This guide is
  app-agnostic — resolve the placeholder tokens (see Legend) to the current app's
  real names before generating files.
---

# Clean Architecture Guide (KMP + Compose Multiplatform)

This guide is the source of truth for **how to build a new feature** in a KMP +
Compose Multiplatform app that follows this layering. Follow it whenever the user
asks for a new feature, screen, flow, ViewModel, or data source. Match the target
app's existing idioms — do not introduce a different style.

## Placeholder legend

This guide is written app-agnostically. Before generating code, resolve these
tokens to the **current app's** actual names (discover them from the codebase):

| Token in this guide | Means | How to find it |
|---|---|---|
| `<app.package>` | Root package | The package under `src/commonMain/kotlin/…` |
| `App…` (e.g. `AppDatabase`, `AppPreferences`) | App-level singletons | Find the `@Database` class, the prefs wrapper, the DI modules |
| `Feature` (e.g. `FeatureScreen`) | The feature you're building | Chosen with the user in Phase 0 |
| `Entity` (e.g. `EntityRepository`) | A domain concept | New (from Phase 0) or an existing model |
| `AppTheme` / `Variables` | The app's theme + dimensions/design-system objects | The `theme/` package |

Code snippets use the literal words `App` / `Feature` / `Entity` as stand-ins —
replace them with the real names (e.g. `Feature` → `Settings`, `Entity` → `Note`).
Confirm the real names by opening a sibling feature (see below) before you write.

## Architecture at a glance

```
Compose UI                Domain                     Data (optional)
──────────                ──────                     ───────────────
Screen  ──renders──▶ View        domain model  ◀──map──▶  RoomModel (@Entity)
  │                   (stateless)  (datamodels/)             │
  └─ obtains ─▶ ViewModel ──reads/writes──▶ Repository ──▶ Dao (@Dao)
               (StateFlow)        (optional UseCase)    (AppDatabase)
```

- **Presentation**: `Screen` (nav/entry + wiring) → `View` (stateless, reusable UI) → `ViewModel` (state via `StateFlow`).
- **Domain**: plain `data class` models, decoupled from Room.
- **Data (OPTIONAL)**: `Repository` + `Dao` + `RoomModel` for relational/complex data, OR a key-value preferences wrapper for simple primitives. Only when the feature persists data.
- **DI**: Koin. ViewModels are `factory`/`viewModel`-scoped; repositories/helpers are `single`.
- Keep everything in `commonMain` unless a platform API forces `expect`/`actual`.

> **Match a real sibling, not just these snippets.** Before generating files,
> open the nearest existing feature in the target app (a `ui/<something>/` package
> with a Screen/View/ViewModel trio) and copy its exact idioms — imports, file
> split, state naming, DI style, and the real names behind the placeholders. The
> snippets below are a summary; the live code is the truth and wins if they drift.

**Library versions:** never hard-code them — read `gradle/libs.versions.toml`. Reuse the existing `libs.*` aliases; add a new alias only if a genuinely new dependency is required, and tell the user.

---

## Phase 0: Discovery — interview BEFORE you build

**Do not scaffold anything until you understand the full context.** A one-line
request ("add a stats screen") is never enough. Your first job is to interview
the user until you could implement the feature with no further guesswork.

**How to ask:**
- Ask **one question at a time**, and provide **suggested options / a recommended
  default** with each so the user can answer fast (multiple-choice beats open-ended).
- Prefer the AskUserQuestion tool when available; otherwise a numbered list.
- Start from what you can already infer from the codebase (existing models,
  screens, repositories) and only ask what you genuinely can't determine.
- Stop asking once you can confidently fill in the Decision Checklist and the
  Requirements Summary below — don't interrogate past the point of usefulness.

**Cover these areas (skip any the request already answers):**

1. **Goal & scope** — What problem does this feature solve? What's explicitly
   in scope vs. out of scope for this pass (MVP vs. full)?
2. **User flow** — Walk through it step by step. Where does the user enter from?
   What do they see, tap, and end up at? What's the empty/first-run state?
3. **Data & domain** — What data does it show or capture? Does it introduce a
   new concept (new domain model) or reuse existing ones? What are the fields
   and their types?
4. **Persistence** — Does the data need to survive app restarts as relational/
   complex data (Room), as simple primitives (preferences wrapper), or stay
   in-memory only? Does it read/write existing tables/keys or need new ones?
5. **Reads vs. writes** — Is it read-only (display/derived), or does it create/
   edit/delete data? Should reads be live (observe a `Flow`) or one-shot?
6. **Navigation** — Is it a standalone navigable screen, a tab in the bottom bar,
   a step in an existing flow, or a sub-component of an existing screen? What
   arguments does it need (ids, enums)? Where can the user go from it and back to?
7. **UI specifics** — Is there a design/mockup? Reuse existing components/theme or
   new ones? Any states to handle (loading, empty, error, success)?
8. **Platform** — Common to both Android + iOS (default), or platform-specific
   behavior needing `expect`/`actual`?
9. **Edge cases & rules** — Validation, limits, sorting/filtering, permissions,
   time zones, empty/zero states, concurrency. What should happen when things go
   wrong?
10. **Testing & done** — What does "done" look like? What behavior must the tests
    lock in? Any analytics events to fire?

**Then produce a Requirements Summary** and get a thumbs-up before coding:

> **Feature:** … · **In scope:** … · **Out of scope:** …
> **Entry point / flow:** … · **Domain model(s):** … · **Persistence:** …
> **Navigation:** … · **Key states:** … · **Edge cases:** … · **Tests:** …

Only after the user confirms the summary do you move to the Decision Checklist.

---

## Decision Checklist (run this BEFORE writing any code)

Not every feature needs every layer. Ask the user (or decide from the request) and state your answers before scaffolding:

1. **Does it persist data, and how?**
   - **No** → skip the Data layer. The ViewModel holds in-memory/derived state, or reads an *existing* repository. Create no persistence files.
   - **Yes, relational/complex** → build `RoomModel` + `Dao` + `Repository`, register the entity in `AppDatabase`, register the repo in `appModule`.
   - **Yes, a simple primitive/flag** → add a typed property to the app's preferences wrapper (§2b). No Room.
2. **Does it need a new domain model?** Only if it introduces a new concept not already present. Reuse existing models where possible.
3. **Does the ViewModel need a use-case?** Default **no** — ViewModel talks to the Repository directly. Add a `UseCase` only when logic is (a) shared across multiple ViewModels, or (b) complex multi-repository orchestration worth isolating/testing on its own. When in doubt, skip it.
4. **Is it a navigable screen?** If yes, add a nav route entry and wire it into the nav graph. If it's a sub-component of an existing screen, it may just be a new `View`.
5. **What gets DI-registered?** Every new ViewModel → `viewModelModule` in commonMain. Every new Repository/helper → `appModule`.

Confirm the checklist outcome in one line before generating files, e.g.
_"Feature X: persistence = Room, new domain model = Note, use-case = no, navigable = yes."_

---

## File placement

```
ui/<feature>/               Screen.kt, View.kt, ViewModel.kt   (presentation)
datamodels/                 <Entity>.kt                         (domain)
persistentStorage/<entity>/ <Entity>RoomModel.kt, <Entity>Dao.kt, <Entity>Repository.kt   (data, optional)
persistentStorage/          AppDatabase.kt                      (register entity + dao here)
utilities/…/preferences     AppPreferences.kt                   (simple key-value, optional)
di/                         AppModule.kt (singles), ViewModelModule.kt (commonMain, all targets)
navigation/destinations/    ScreenDestinations.kt               (nav route)
navigation/                 MainNavGraph.kt / BottomBarNavGraph.kt (nav wiring)
```

Group a feature's presentation files in one `ui/<feature>/` package. Deeper flows nest (e.g. `ui/<parent>/<step>/`).

---

## Naming conventions

| Thing | Pattern | Example |
|---|---|---|
| Nav/entry composable | `<Feature>Screen` | `SettingsScreen` |
| Stateless UI composable | `<Feature>View` | `SettingsView` |
| ViewModel | `<Feature>ViewModel` | `SettingsViewModel` |
| Domain model | noun | `Note` |
| Room entity | `<Entity>RoomModel` | `NoteRoomModel` |
| DAO | `<Entity>Dao` | `NoteDao` |
| Repository | `<Entity>Repository` | `NoteRepository` |
| Private backing state | `<name>Internal` (or `_<name>`) | `notesInternal` |
| Click callback type | `on<Thing>Clicked` typealias | `onNoteClicked` |

---

## Layer rules + snippets

> Snippets use `App` / `Feature` / `Entity` as placeholder identifiers — rename to
> the app's real names. Package declarations are omitted; place each file per the
> File-placement map above, under `<app.package>`.

### 1. Domain model (`datamodels/`)

- Plain `@Serializable @Parcelize data class`. **No Room annotations** — the domain model must not depend on the data layer's shape, only the reverse.
- If it maps to a Room entity, implement the app's mapping interface (`BaseDataModel<RoomModel>` or equivalent) and provide both directions: a `constructor(roomModel)` and `toDatabaseObject(id: Long?)`.
- If the feature has no persistence, the model is just a plain data class (no mapping interface).

```kotlin
@Serializable
@Parcelize
data class Entity(
    val title: String,
    val amount: Int
) : Parcelable, BaseDataModel<EntityRoomModel> {

    constructor(model: EntityRoomModel) : this(model.title, model.amount)

    override fun toDatabaseObject(id: Long?) = EntityRoomModel(id, title, amount)
}
```

### 2. Data layer — Room, OPTIONAL (`persistentStorage/<entity>/`)

Only when the Decision Checklist says persistence = relational/complex. Three files + two registrations.

**RoomModel** — the `@Entity`. Give a nullable-id secondary constructor so callers can insert without knowing the auto-generated id.

```kotlin
@Entity
data class EntityRoomModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Int
) {
    constructor(id: Long? = null, title: String, amount: Int)
        : this(id ?: 0, title, amount)
}
```

**Dao** — `@Dao interface`. Use `@Upsert` (returns the id), `@Delete`, and `@Query`. Return `Flow<List<T>>` for observable reads, `suspend` for one-shot reads/writes.

```kotlin
@Dao
interface EntityDao {
    @Upsert suspend fun upsert(entity: EntityRoomModel): Long
    @Delete suspend fun delete(entity: EntityRoomModel)
    @Query("SELECT * FROM entityRoomModel WHERE id = :id LIMIT 1")
    suspend fun getEntity(id: Long): EntityRoomModel?
    @Query("SELECT * FROM entityRoomModel")
    fun getAll(): Flow<List<EntityRoomModel>>
}
```

**Repository** — the ONLY thing that touches the DAO. Takes `AppDatabase` in its constructor, grabs its DAO, exposes observable state as `StateFlow`, and maps RoomModel ↔ domain model at its boundary. Suspends for writes.

```kotlin
class EntityRepository(private val db: AppDatabase) {
    private val dao = db.entityDao()

    private val itemsInternal = MutableStateFlow<List<EntityRoomModel>>(emptyList())
    val items = itemsInternal.asStateFlow()

    fun collectAndEmitFromDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            dao.getAll().collect { itemsInternal.emit(it) }
        }
    }

    suspend fun get(id: Long): Entity? = dao.getEntity(id)?.let(::Entity)
    suspend fun add(entity: Entity): Long = dao.upsert(entity.toDatabaseObject(null))
    suspend fun update(entity: Entity, id: Long) { dao.upsert(entity.toDatabaseObject(id)) }
    suspend fun remove(model: EntityRoomModel) = dao.delete(model)
}
```

**Register the entity** in `AppDatabase`: add the `RoomModel` to the `@Database(entities = [...])` list and add an `abstract fun <entity>Dao(): <Entity>Dao`. Bump `version` / write a migration only if you change an existing schema.

### 2b. Data layer — simple key-value preferences, OPTIONAL

Use this instead of Room when the Decision Checklist says persistence = a simple
primitive or flag (booleans, ids, counts, small enums). The app has a **single
preferences wrapper** (`AppPreferences`) around a platform `expect`/`actual`
key-value interface. To persist a new value, **add a typed property** — don't
create a new store, and don't reach for Room.

```kotlin
class AppPreferences(private val prefs: AppPreferencesInterface) {

    // pattern: private key string + typed var delegating to the interface
    private val featureEnabledKey = "featureEnabledKey"
    var featureEnabled: Boolean
        get() = prefs.getBool(featureEnabledKey, default = true)
        set(value) = prefs.setBool(featureEnabledKey, value)
}
```

- One property per stored value; key string is `private`, property is a public `var`.
- Store primitives only (encode nullables with a sentinel, e.g. `-1` → `null`, as existing properties do). Complex/relational data belongs in Room.
- `AppPreferences` is already a `single` in `appModule`; inject it (`koinInject`/constructor) rather than creating a second store.

### 3. ViewModel (`ui/<feature>/`)

- Extends `androidx.lifecycle.ViewModel`. Dependencies (repositories/use-cases/prefs) come in via the **constructor** — never construct them inside.
- Expose state as **`StateFlow`**: private `MutableStateFlow` backing field named `<name>Internal` (or `_<name>`), public `.asStateFlow()`. UI is read-only.
- Do async work in `viewModelScope.launch { ... }`. Emit via `.emit()` / `.value`.
- No Compose imports, no Android/iOS platform types. Keep it pure Kotlin so it's testable and shared.

```kotlin
class FeatureViewModel(
    private val entityRepository: EntityRepository
) : ViewModel() {

    private val itemsInternal = MutableStateFlow<List<Entity>>(emptyList())
    val items = itemsInternal.asStateFlow()

    fun load() {
        entityRepository.collectAndEmitFromDatabase()
        viewModelScope.launch {
            entityRepository.items.collect { models ->
                itemsInternal.emit(models.map(::Entity))
            }
        }
    }
}
```

### 4. View (`ui/<feature>/`)

- **Stateless and reusable.** Inputs are plain data + callbacks; it holds no ViewModel and no business logic. This is what you preview and reuse.
- Declare click callbacks as a `typealias` (`typealias onNoteClicked = (id: Long) -> Unit`).
- Style via `AppTheme` (`AppTheme.typography.*`, `AppTheme.colors.*`) and `Variables.Dimensions.*`. Never hard-code colors/spacing.
- **All user-facing text is a string resource** — never a hard-coded literal. Before you can use `stringResource(Res.string.my_new_string)`, add the entry to `composeApp/src/commonMain/composeResources/values/strings.xml` (group it under a `<!-- Feature -->` comment like the existing sections). The `Res.string.*` accessor is code-generated on the next build, so add strings *before* referencing them and expect a build to regenerate `Res`.

```kotlin
typealias onNoteClicked = (id: Long) -> Unit

@Composable
fun FeatureView(
    items: List<Entity>,
    onNoteClicked: onNoteClicked
) { /* pure UI reading AppTheme + Variables, text via stringResource */ }
```

### 5. Screen (`ui/<feature>/`)

- The entry/wiring layer. Obtains the ViewModel via `koinViewModel<T>()`, collects its flows with `collectAsState()`, triggers loads in `LaunchedEffect`, and passes plain state + callbacks down to the `View`.
- Owns screen scaffolding/background and navigation callbacks passed in from the nav graph (e.g. `onNoteClicked: (Long) -> Unit`).

```kotlin
@Composable
fun FeatureScreen(onNoteClicked: (id: Long) -> Unit) {
    val vm = koinViewModel<FeatureViewModel>()
    LaunchedEffect(Unit) { vm.load() }
    val items by vm.items.collectAsState()
    FeatureView(items = items, onNoteClicked = onNoteClicked)
}
```

### 6. Use-case — OPTIONAL (`datamodels/` or a `domain/` package)

Only when the checklist flagged it. A `UseCase` is a small class with a single `operator fun invoke(...)`, injected into the ViewModel like a repository, and registered as a `single`/`factory` in `appModule`. Default is to skip it and let the ViewModel call the repository directly.

### 7. Dependency Injection (`di/`)

- **Repositories, helpers, preferences** → `di/AppModule.kt` as `single { XRepository(get()) }`.
- **ViewModels** → `commonMain/…/di/ViewModelModule.kt` as `viewModelOf(::FeatureViewModel)`. One
  registration covers every target — Koin's `viewModelOf` is multiplatform (`koin-core-viewmodel`),
  so there is **no** `expect`/`actual` here and nothing to keep in sync.
- Prefer the constructor-reference form `viewModelOf(::X)` over `viewModel { X(get(), get()) }`.
  It infers the dependencies, so it can't drift out of step with the constructor — positional
  `get()`s silently resolve the wrong instance if two params share a type.
- Retrieve in Compose only via `koinViewModel<T>()` (screens) or `koinInject<T>()` (non-VM singletons). Never `new` a ViewModel or repository.
- **Koin is started once per process, from the platform's app object — never from a screen or
  an Activity.** `startKoin` throws `KoinAppAlreadyStartedException` on a second call, and an
  Activity is recreated on any configuration change its manifest doesn't absorb (system
  dark-mode toggle, font scale) while the process — and Koin's global context — survives.
  - Android: an `Application` subclass calling `initKoin(applicationContext)` in `onCreate()`,
    wired up via `android:name` on `<application>`.
  - iOS: the SwiftUI `@main` struct's `init()`, calling the iOS `initKoin()` — which Kotlin
    exports to Swift as `doInitKoin()` (the ObjC bridge prefixes `init`-family names with `do`).
  - Only the platform-specific bindings (the key-value store, anything needing a `Context`) go
    in the per-platform module passed to the shared `initKoin(platformModule)`.

### 8. Navigation (`navigation/`)

- Add a route to `navigation/destinations/ScreenDestinations.kt` as a `@Serializable data object` (no args) or `data class` (with args). Args must be serializable primitives; convert complex types to ids/strings.
- Wire it in `MainNavGraph.kt` (or `BottomBarNavGraph.kt` for tabbed screens): a `composable<ScreenDestinations.X>` block that reads args via `toRoute()` and renders the `Screen`, passing navigation lambdas that call `navController.navigate(...)`.

### 9. Testing (`commonTest/`)

Include tests for new features. Focus on the **ViewModel** (the logic layer); the View is stateless and the Repository is a thin Room wrapper.

- Location: `composeApp/src/commonTest/kotlin/<app.package>/…` mirroring the feature package.
- Libs: `kotlin.test` (in the catalog as `libs.kotlin.test`). For coroutine/Flow tests add `kotlinx-coroutines-test` to `commonTest` — flag to the user if it's not present yet.
- Pattern: construct the ViewModel with a **fake repository** (a hand-written class implementing the same surface, ideally an extracted interface) that emits controlled `StateFlow` values. Drive the ViewModel, advance the test dispatcher, assert on the exposed `StateFlow`s.

```kotlin
class FeatureViewModelTest {
    @Test
    fun emits_mapped_items_from_repository() = runTest {
        val fakeRepo = FakeEntityRepository(initial = listOf(/* ... */))
        val vm = FeatureViewModel(fakeRepo)
        vm.load()
        advanceUntilIdle()
        assertEquals(expected, vm.items.value)
    }
}
```

To make a repository fakeable, prefer extracting an `interface` (the ViewModel depends on the interface; Koin binds the real impl). If the user prefers no interfaces, use an open class with overridable methods.

---

## Verifying your work

"Done" means it compiles on both targets, tests pass, and — for anything with
runtime behavior — you've seen it actually work. Don't stop at "the code looks right."

Use the project's `gradlew` wrapper. Typical commands (confirm task names per repo):

- **Quick compile (Android variant, includes commonMain):**
  `./gradlew :composeApp:compileDebugKotlinAndroid`
- **iOS compile (catches KMP/native issues Android won't):**
  `./gradlew :composeApp:compileKotlinIosSimulatorArm64`
- **Unit tests:** `./gradlew :composeApp:testDebugUnitTest`
- **Full Android build/APK:** `./gradlew :composeApp:assembleDebug`

Rules of thumb:
- After adding string resources or new Compose resources, run a build so the
  generated `Res` accessors exist — a red `Res.string.x` is usually just "not built yet."
- Compile **both** Android and iOS for shared code; a change can pass on one target
  and fail on the other (`expect`/`actual`, native-only APIs).
- To actually run/drive the app (not just compile), defer to the project's
  **`/run`** and **`/verify`** skills rather than reinventing launch steps here.
- Report honestly: if a target fails to compile or a test fails, say so with the
  output — don't claim done on a red build.

---

## Do / Don't

**Do**
- Run Phase 0 Discovery first; interview one question at a time with options, and confirm the Requirements Summary before writing code.
- Resolve the placeholder tokens to the app's real names by reading a sibling feature first.
- Run the Decision Checklist and state the outcome before generating files.
- Keep the ViewModel free of Compose/platform imports.
- Map RoomModel ↔ domain model only at the Repository boundary.
- Register every new ViewModel in `viewModelModule` (commonMain) via `viewModelOf(::X)`.
- Pull versions/aliases from `libs.versions.toml`.
- Use `AppTheme` + `Variables.Dimensions` + `stringResource` in the View.

**Don't**
- Don't scaffold before the Requirements Summary is confirmed.
- Don't create Room files for a feature that doesn't persist relational data — use the preferences wrapper for simple primitives, or nothing for in-memory state.
- Don't let the View or Screen contain business logic (belongs in the ViewModel).
- Don't touch a DAO outside its Repository.
- Don't expose `MutableStateFlow` publicly — always `.asStateFlow()`.
- Don't add a use-case layer by default; only when shared/complex.
- Don't hard-code colors, dimensions, strings, or library versions.

---

## Build order for a new feature

0. **Phase 0: Discovery** — interview the user (one question at a time, with options), then write the Requirements Summary and get confirmation.
1. Decision Checklist → state outcome. Resolve placeholder names from a sibling feature.
2. Domain model (if new).
3. Data layer (if persisting): Room (RoomModel → Dao → Repository → register in `AppDatabase` → register repo in `appModule`) **or** a preferences property.
4. ViewModel → register in `viewModelModule` (commonMain).
5. String resources → add entries to `strings.xml` for all user-facing text.
6. View (stateless) → Screen (wiring).
7. Navigation route + graph wiring (if navigable).
8. Tests (ViewModel + fake repository).
9. Verify: compile Android **and** iOS, run tests, and drive the app via `/run` or `/verify` for anything with runtime behavior. Report results honestly.

---

## Resolving the placeholders — worked example

This is an illustration of how the tokens map to real names in a given app. The
names below are a **generic example** (a fictional "Notes" app) — replace them
with whatever you discover in the actual codebase you're working in.

| Placeholder | Example resolution |
|---|---|
| `<app.package>` | `com.example.notes` |
| `App` (in `AppDatabase`) | `NotesDatabase` (the `@Database` class in `persistentStorage/`) |
| `App` (in `AppPreferences`) | `NotesPreferences` over a `PreferencesInterface` (`utilities/…/preferences/`) |
| `BaseDataModel` mapping interface | `datamodels/BaseDataModel.kt` |
| `AppTheme` / `Variables` | the design-system objects in `theme/` |
| Nav | the `@Serializable` route sealed class + the nav graph in `navigation/` |
| DI | `appModule` (singles) + `viewModelModule` (commonMain); `koinViewModel<T>()` helper |
| `Feature` | e.g. `NoteList` → `NoteListScreen` / `NoteListView` / `NoteListViewModel` |
| `Entity` | e.g. `Note` → `NoteRoomModel` / `NoteDao` / `NoteRepository` |

To find the real values, open the nearest existing feature (a `ui/<something>/`
Screen/View/ViewModel trio) and its repository/database, and mirror those names.
