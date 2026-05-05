# 🎯 HobbyHabit

**CS501 — Boston University — Spring 2026**  
Maria Diaz Garrido · Yasemin Nurluoglu

HobbyHabit is an Android app built with Kotlin and Jetpack Compose that helps users discover, track, and grow personal hobbies through habit tracking, session logging, local event discovery, a visual calendar, and streak analytics.

---

## Architecture

HobbyHabit follows **MVVM (Model-View-ViewModel)** with four clearly separated layers:

| Layer | Package | Responsibility |
|---|---|---|
| UI / View | `ui/screens` | Jetpack Compose screens, stateless, driven by StateFlow |
| ViewModel | `ui/viewmodel` | Business logic, exposes StateFlow/Flow, survives rotation |
| Repository | `data/repository` | Single source of truth, combines Room and Retrofit |
| Data | `data/local` + `data/remote` | Room entities/DAOs and Retrofit API definitions |

**Key decisions:**
- `StateFlow + collectAsState()` — all UI state is reactive; screens never hold mutable state directly
- `AndroidViewModel` — both `HobbyViewModel` and `EventViewModel` access application context without leaking Activity references
- Repository pattern — screens never call DAOs or APIs directly, keeping the UI layer testable
- Single `NavGraph` with a sealed `Screen` class; bottom nav uses `popUpTo` to avoid deep back-stacks
- `fallbackToDestructiveMigration()` — chosen for the development timeline over migration scripts

---

## Database Schema

Single Room database (`HobbyDatabase`, version 12) with four entities. `EventConverters` handles the `EventSource` enum as a String for SQLite.

### Hobby
| Column | Type | Description |
|---|---|---|
| id (PK) | Int (auto) | Primary key |
| name | String | User-chosen hobby name |
| category | String | Ticketmaster classification (e.g. Music) |
| weeklyGoal | Int | Target sessions per week |

### Session
| Column | Type | Description |
|---|---|---|
| id (PK) | Int (auto) | Primary key |
| hobbyId (FK) | Int | References Hobby.id |
| durationMinutes | Int | Length in minutes |
| notes | String | Free-text notes |
| timestamp | Long | When session occurred (epoch millis) |

### Event
| Column | Type | Description |
|---|---|---|
| id (PK) | Long (auto) | Primary key |
| hobbyId (FK → CASCADE) | Int | References Hobby.id; deletes cascade |
| name | String | Event title |
| location | String | Venue name + city |
| dateTime | Long | Epoch millis — drives past/upcoming split |
| durationMinutes | Int? | Optional duration |
| url | String? | Ticketmaster or custom URL |
| source | EventSource | `USER` or `TICKETMASTER` |
| imageUri | String? | Local photo URI (user-uploaded) |

### User
Profile data (name, bio, avatar URI) for the Profile screen.

**Notable DAO queries:**
- `getSessionCountThisWeek(hobbyId, weekStart)` — weekly progress bar
- `getWeeklyEventCount(hobbyId, weekStart, now)` — counts only past events this week, excluding upcoming
- `getEventsForDay(dayStart, dayEnd)` — powers CalendarScreen day view
- `findEvent(hobbyId, name)` — duplicate guard before Ticketmaster event insert

---

## APIs and Sensors

### Ticketmaster Discovery API v2
Powers the Events tab — finds real upcoming events near the user matching their hobby category.

- **Endpoint:** `GET /events.json` with `classificationName` + lat/lng params
- **Auth:** `BuildConfig.TICKETMASTER_TOKEN` (stored in `gradle.properties`, never committed)
- **Mapping:** `EventMapper.toEvent(hobbyId)` converts the API response to a Room `Event` entity
- **Register flow:** "I Registered ✓" → confirmation dialog → hobby picker → `registerTicketmasterEvent()` → stored to Room → appears in Upcoming or Past Events based on `dateTime` vs `now`

### GPS / Location
`FusedLocationProviderClient` with runtime `ACCESS_COARSE_LOCATION` permission. Falls back to a city picker dialog using Android's `Geocoder` class when permission is denied or location is unavailable.

### Photo Picker
`ActivityResultContracts.GetContent()` with `image/*` launches the system picker. The URI is stored as `imageUri` on the `Event` entity and rendered with Coil's `AsyncImage`.

### WorkManager (Local Notifications)
`SessionReminderWorker` runs every 3 days as a `CoroutineWorker`, posting a randomised motivational notification. Registered with `ExistingPeriodicWorkPolicy.KEEP` so it is only ever scheduled once.

---

## Team Contributions

### Maria Diaz Garrido
- Designed MVVM architecture, Room schema, all DAOs, and repository layer
- Built Ticketmaster API integration end-to-end (Retrofit, EventRepository, EventMapper, register flow)
- Implemented GPS location detection and city-based geocoding in EventsScreen
- Built CalendarScreen (month navigation, event dot indicators, category emoji system)
- Built StatsScreen (streak calculation, monthly breakdown, shareable progress card)
- Implemented HomeScreen features: search, sort, delete confirmation with count warning, streak badge
- Built OnboardingScreen, EmptyState illustrations, and WorkManager notifications
- Led Git branch management and resolved all 10 merge conflicts

### Yasemin Nurluoglu
- Designed and implemented the full visual identity: custom colour palette (BlushPink, CreamPeach, DustyRose, WarmGray, SageGreen), card styling, progress bar aesthetics, and UI theme across all screens
- Built session editing/deletion with `SessionFormDialog` and date/time picker
- Built `EventFormDialog` with full CRUD: name, location, duration, URL, date/time, and image attachment
- Implemented smart routing logic (past date → Session History, future date → Upcoming Events)
- Added `HistoryItem` sealed class merging sessions and past events into one chronological list
- Built the event photo feature (image URI storage, thumbnail rendering, removal flow)
- Implemented `TicketmasterEventCard` with separate View and Register buttons
- Contributed to `BottomBar` with Calendar tab and active route highlighting

---

## AI Reflection

Claude (Anthropic) was used throughout the project as a senior pair programmer — asked to reason about architecture, generate code, debug runtime errors, and resolve merge conflicts.

**Where it influenced the project:**
- Recommended the Repository pattern, `StateFlow` over `LiveData`, and the `HistoryItem` sealed class
- Caught that `location` on `Event` should be non-nullable with a default empty string, and that `EventSource` needed a `TypeConverter`
- Suggested the calendar's `surfaceVariant` shelf, the dynamic section header, and the emoji category system
- Diagnosed the `EventViewModel` crash (multi-parameter constructor incompatible with `by viewModels()`) directly from logcat
- Resolved 10 merge conflicts across critical files in a single session

**What it accelerated:**
- Full screen scaffolding: CalendarScreen, StatsScreen, OnboardingScreen, ShareProgressCard, EmptyState, and SessionReminderWorker generated from scratch
- Runtime crash debugging reduced from hours to minutes via stack trace analysis
- Boilerplate (DAOs, repository methods, ViewModel wrappers) kept consistent across all three repositories

**Where suggestions were rejected:**
- *Smart session routing:* Claude proposed storing future-dated entries as `Session` objects with a flag — rejected to preserve the clean `Session` (completed) vs `Event` (scheduled) separation. Future entries are stored as `Event` with `source = USER` instead.
- *Calendar grid restructuring:* Claude suggested restructuring the `LazyVerticalGrid` when the bottom row was clipped. The actual fix was simpler: adding 20dp padding to the height calculation.
- *Streak algorithm:* Claude's `bestStreak` implementation had a logically incorrect `LocalDate.now()` placeholder inside the loop. Retained as-is since it was non-critical for submission.

**Overall:** AI compressed a semester of work into a realistic scope for a two-person team. Every generated file was reviewed and integrated deliberately. AI accelerated engineering judgment, it did not replace it.
