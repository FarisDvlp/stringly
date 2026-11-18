# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

Beginner-to-intermediate guitarists using Stringly as an all-in-one daily practice and tone companion during home practice and rehearsal sessions.

## Product Purpose

Stringly brings together essential guitar practice utilities, tone-shaping tools, and peer sharing in a single Android mobile application so guitarists do not have to switch between separate tuner, metronome, chord/tab, and community apps while practicing.

## Positioning

Combines daily practice tools (chromatic tuner, precision metronome, chord library, song chord & lyric sheets) with community-driven sharing—specifically the Jam Spot forum for technique and lick discussions and the Pedalboard community for sharing gear setups (guitars, pedals, and parameter settings) for specific songs.

## Operating Context

- **Sonic Stage (Home):** Daily practice dashboard surfacing real-time tuner and metronome status, a Chord of the Day with interactive fretboard diagrams, and song practice challenges.
- **Practice Utilities:**
  - **Chromatic Tuner:** Radial pitch gauge, cent-offset needle display, standard guitar peg selection, and microphone listener.
  - **Precision Metronome:** Pendulum animation, tempo markings (Largo to Prestissimo), time signatures, and beat pips.
  - **Chord Library:** 3,200+ chord voicings, root note selector, fretboard diagrams, and audio strum previews.
  - **Songs:** Song catalog for searching chords used in specific songs alongside lyrics, tuning, time signature/tempo, and difficulty (similar to Ultimate Guitar).
- **Tone & Community:**
  - **Pedalboard (Gear & Tone Sharing):** Community forum for sharing guitar setups—which guitar, pedals, and parameter settings players use for specific songs.
  - **Jam Spot Forum:** Community feed for sharing guitar licks, technique inquiries, and gear setups.
  - **Accounts:** Login and Sign Up flows (`LoginScreen.kt`, `SignUpScreen.kt`).

## Capabilities and Constraints

- **Target Platform:** Android (`minSdk 26`, `targetSdk 35`), built with Kotlin 2.0, Jetpack Compose (Material 3), and Jetpack Navigation Compose (`app/build.gradle.kts`).
- **Web Simulator:** `index.html` is a standalone interactive simulator used solely to preview Stitch screens; it is not a maintained product surface.
- **Open / Undecided:** Backend API integration details (authentication, Jam Spot persistence, pedalboard setup sharing) and hardware audio/microphone integration details are outside the current frontend codebase and not yet specified here.

## Evidence on Hand

- Android source screens in `app/src/main/java/com/stringly/app/ui/screens/` (`HomeScreen.kt`, `TunerScreen.kt`, `MetronomeScreen.kt`, `ChordsScreen.kt`, `TabsScreen.kt`, `PedalboardScreen.kt`, `JamSpotScreen.kt`, `LoginScreen.kt`, `SignUpScreen.kt`).
- Theme files in `app/src/main/java/com/stringly/app/ui/theme/` (`Color.kt`, `Theme.kt`, `Type.kt`).
- Reference exports in `stitch_screens/html/` and `stitch_screens/screenshots/`, plus `index.html` simulator.

## Product Principles

1. **Practice-First Continuity:** Keep core tools (tuner, metronome, chords, songs) immediately reachable from the Sonic Stage so players stay focused on playing rather than navigating menus.
2. **Tone Meets Song Context:** Connect pedalboard signal chains and settings directly to the songs and styles players are practicing and sharing.
3. **Community Around the Instrument:** Ground Jam Spot and pedalboard sharing in practical guitar workflows—licks, technique questions, and reproducible gear settings.
4. **Native Android Craft:** Respect Android Material 3 and Jetpack Compose conventions for touch targets, tactile feedback, and mobile ergonomics during instrument-in-hand use.
