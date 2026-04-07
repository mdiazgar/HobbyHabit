# HobbyHabit 🌱

**Bridging habit formation and local hobby discovery.**
HobbyHabit is an Android app that helps university students and young adults build hobbies consistently by combining personal habit tracking with nearby hobby-related event discovery.

## The Problem

Students want to build hobbies like pottery, photography, or dance but struggle with two things: staying consistent and finding beginner-friendly local events. Existing apps either track habits *or* show events, never both. HobbyHabit connects the two.

## Features

### Core
- **Add hobbies** with a custom name and weekly session goal
- **Log practice sessions** with duration, optional notes, and date/time selection
- **Weekly progress bar** showing sessions completed vs. goal on every hobby card
- **Edit and delete sessions** — full session management from the detail screen
- **Session history** displayed per hobby with timestamps

### Event Discovery
- **Find local events** for any hobby via the Eventbrite API integration
- **Location-aware search** using GPS coordinates to filter nearby events
- Graceful fallback with mock events when API token is not yet configured

## Architecture

The app follows **MVVM** (Model-View-ViewModel) with a clean layered structure:
UI Screens (Compose)
ViewModel  —  exposes StateFlow / Flow
Repository
Room (local DB)    Retrofit (Eventbrite API)


## Screens

**Home** — Lists all hobbies with a live weekly progress bar for each. FAB navigates to Add Hobby.
**Add Hobby** — Form to create a new hobby with a name and a weekly session goal. Input validation included.
**Hobby Detail** — Shows weekly progress card, full session history, and options to log, edit, or delete sessions. Calendar and time pickers for session date/time. Tap the event icon in the top bar to find local events.
**Events** — Requests location permission, fetches nearby hobby-related events from the Eventbrite API, and displays them in a list with date and venue information.

## AI Disclaimer

AI assistance (Claude by Anthropic) was used during the development of this project. It helped with generating the initial MVVM structure, debugging Gradle build errors, and AndroidManifest resource linking issues.
Claude also helped generate the initial versions of compose screens, which we then edited to match our specific app requirements (adding date/time pickers, session editing, and custom session card layouts).
We reviewed each file, understood the patterns, and verified they matched what was taught in class before including them.
