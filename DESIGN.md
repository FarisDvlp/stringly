---
name: Stringly
description: Guitarist Mobile Design System — tactile studio hardware meets clean Material 3 mobile utility.
colors:
  primary-deep: "#9E2016"
  primary-red: "#C0392B"
  electric-orange: "#FF6B35"
  amber-gold: "#FF8C00"
  stage-success: "#27AE60"
  surface-light: "#F9F9FB"
  surface-card: "#FFFFFF"
  surface-variant-light: "#EEEEF0"
  on-surface-light: "#1A1C1D"
  graphite-mute: "#6B6B6B"
  surface-dark: "#14171D"
  surface-variant-dark: "#20242D"
  on-surface-variant-dark: "#A0A5B1"
  charcoal-hardware: "#1A1A1A"
typography:
  display:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "30sp"
    fontWeight: 700
    lineHeight: "36sp"
  headline:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "24sp"
    fontWeight: 700
    lineHeight: "30sp"
  title:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: "22sp"
  body:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "15sp"
    fontWeight: 400
    lineHeight: "22sp"
  label:
    fontFamily: "Roboto, system-ui, sans-serif"
    fontSize: "11sp"
    fontWeight: 600
    lineHeight: "14sp"
rounded:
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  full: "9999dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "16dp"
  lg: "20dp"
components:
  button-primary:
    backgroundColor: "{colors.primary-deep}"
    textColor: "{colors.surface-card}"
    rounded: "{rounded.md}"
    padding: "12dp 20dp"
  card-surface:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.on-surface-light}"
    rounded: "{rounded.lg}"
    padding: "16dp"
  nav-bar:
    backgroundColor: "{colors.surface-card}"
    textColor: "{colors.primary-red}"
    height: "80dp"
---

# Design System: Stringly

## Overview

**Creative North Star: "The Stage Amp"**

Stringly pairs the tactile confidence of studio guitar hardware with clean, distraction-free Material 3 mobile utility. Surfaces stay crisp and high-contrast so players can read chord diagrams, tuner needles, and metronome beats at a glance—whether practicing at a desk or in a dimly lit rehearsal room.

Bold crimson and electric orange accents are reserved for active practice states, primary actions, and live instrument feedback, while neutral studio surfaces carry the bulk of the interface.

**Key Characteristics:**
- Tactile, hardware-inspired contrast across light (`#F9F9FB`) and dark stage (`#14171D`) modes
- Tonal surface layering instead of heavy drop shadows
- Clear, scannable typographic hierarchy using system sans-serif (`FontFamily.Default`)
- Confident, instrument-in-hand hit targets and immediate state feedback

## Colors

A high-contrast studio palette anchored by deep crimson and electric orange accents over clean neutral surfaces.

### Primary
- **Deep Crimson** (`#9E2016`): Primary action color in light mode (`LightColorScheme.primary`) and primary container in dark mode. Used for key CTAs, active tabs, and brand emphasis.
- **Stage Red** (`#C0392B`): Light-mode `primaryContainer` and active navigation indicator (`NavigationBar` selected state).
- **Electric Orange** (`#FF6B35`): Secondary accent in light mode and primary action color in dark mode (`DarkColorScheme.primary`), ensuring high visibility against dark stage surfaces.

### Secondary
- **Amber Gold** (`#FF8C00`): Secondary accent in dark mode (`DarkColorScheme.secondary`) for highlights, badges, and active instrument states.
- **Stage Success** (`#27AE60`): Semantic confirmation color for in-tune pitch indicators and completed practice states.

### Neutral
- **Studio Mist** (`#F9F9FB`): Light-mode app background and base surface (`SurfaceLight`).
- **Pure Canvas** (`#FFFFFF`): Elevated card surface (`SurfaceCard`) and light-mode `NavigationBar` container.
- **Soft Ash** (`#EEEEF0`): Light-mode `surfaceVariant` for subtle recessed containers, chips, and dividers.
- **Ink Slate** (`#1A1C1D`): Primary text and icon color on light surfaces (`OnSurfaceLight`).
- **Graphite Mute** (`#6B6B6B`): Secondary/muted text and inactive icon color on light surfaces (`GraphiteMute`).
- **Stage Night** (`#14171D`): Dark-mode app background and base surface (`SurfaceDark`).
- **Amp Chassis** (`#20242D`): Dark-mode `surfaceVariant` for cards and recessed hardware panels.
- **Silvered Steel** (`#A0A5B1`): Dark-mode `onSurfaceVariant` for secondary labels and inactive icons.
- **Charcoal Hardware** (`#1A1A1A`): Deep hardware surface for high-contrast instrument modules.

### Named Rules
**The Stage Contrast Rule.** Keep primary crimson (`#9E2016` / `#C0392B`) and electric orange (`#FF6B35`) focused on active controls, live instrument states, and primary CTAs—never flood large background regions with saturated accent color.

## Typography

**Display & Body Font:** System Sans-Serif (`FontFamily.Default` — Roboto on Android)

**Character:** Clean, utilitarian, and highly legible at arm's length, relying on weight jumps (`Bold` and `ExtraBold` vs. `Normal`) and uppercase micro-labels rather than decorative typefaces.

### Hierarchy
- **Display / Headline Large** (`Bold`, `30sp`, `36sp` line-height): Hero greetings and top-level screen titles (e.g., Sonic Stage greeting).
- **Headline Medium** (`Bold`, `24sp`, `30sp` line-height): Major section headers and feature titles.
- **Headline Small** (`Bold`, `20sp`, `26sp` line-height): Sub-section titles and modal/card headers.
- **Title Medium** (`SemiBold`, `16sp`, `22sp` line-height): Card titles, list item headers, and interactive control labels.
- **Body Large** (`Normal`, `15sp`, `22sp` line-height): Primary reading copy, forum posts, and descriptions.
- **Body Medium** (`Normal`, `14sp`, `20sp` line-height): Secondary descriptions, metadata, and subtitles.
- **Label Small** (`SemiBold`, `11sp`, `14sp` line-height): Eyebrow labels (often uppercase with `1.5sp` letter-spacing), badges, and bottom navigation labels.

## Layout

Single-column scrollable mobile layouts (`fillMaxSize()` with `verticalScroll`) using `20dp` horizontal screen padding, `16dp` vertical screen padding, and `20dp` vertical spacing (`Arrangement.spacedBy(20.dp)`) between major dashboard sections. Content is framed for one-handed portrait operation above a persistent 5-destination bottom `NavigationBar`.

## Elevation & Depth

Stringly uses **tonal layering with crisp surface contrast** rather than heavy drop shadows. In light mode, pure white cards (`#FFFFFF`) sit cleanly over the cool-neutral Studio Mist (`#F9F9FB`) canvas, while recessed controls use Soft Ash (`#EEEEF0`). The bottom `NavigationBar` uses `6.dp` tonal elevation to separate persistent navigation from scrolling content.

### Named Rules
**The Tonal Layer Rule.** Convey hierarchy through surface value shifts (`#F9F9FB` → `#FFFFFF`, or `#14171D` → `#20242D`) rather than stacking drop shadows.

## Shapes

Consistent Material 3 rounded geometry: `16dp` (`RoundedCornerShape(16.dp)`) for primary dashboard and content cards, `12dp` for buttons and interactive tiles, `8dp` for compact chips and badges, and `CircleShape` (`9999dp`) for beat pips, tuner indicators, and avatar/icon badges.

## Components

Tactile and confident: bold primary accents, clear hit targets, and immediate stage-ready legibility.

### Buttons
- **Shape:** Rounded rectangle (`12dp` radius) or pill (`CircleShape`) for compact icon/transport controls.
- **Primary:** Deep Crimson (`#9E2016`) in light mode or Electric Orange (`#FF6B35`) in dark mode with high-contrast `onPrimary` text (`#FFFFFF` / `#000000`).
- **Secondary / Ghost:** Soft Ash (`#EEEEF0`) or transparent surface with `1dp` subtle border and `onSurface` text.

### Cards / Containers
- **Corner Style:** `16dp` rounded corners (`RoundedCornerShape(16.dp)`).
- **Background:** Pure Canvas (`#FFFFFF`) in light mode; Amp Chassis (`#20242D`) in dark mode.
- **Shadow Strategy:** Tonal layering (flat or minimal `1dp–2dp` tonal elevation).
- **Internal Padding:** `16dp` standard (`20dp` for hero/featured cards).

### Navigation
- **Bottom Navigation Bar:** Persistent 5-item Material 3 `NavigationBar` (`Home`, `Tuner`, `Chords`, `Tabs`, `Jam Spot`) with `#FFFFFF` container, `6.dp` tonal elevation, and Stage Red (`#C0392B`) active state. Hidden on `Login` and `SignUp` routes.

## Do's and Don'ts

### Do:
- **Do** use `MaterialTheme.colorScheme` semantic tokens (`primary`, `surface`, `surfaceVariant`, `onSurface`) so screens adapt cleanly between light and dark stage modes.
- **Do** preserve `20dp` horizontal screen margins and `16dp–20dp` section spacing for comfortable mobile scanning.
- **Do** use `Stage Success` (`#27AE60`) strictly for positive instrument/practice feedback (e.g., in-tune pitch).

### Don't:
- **Don't** add heavy drop shadows to cards; rely on tonal surface contrast (`#F9F9FB` vs. `#FFFFFF`).
- **Don't** hardcode light-only colors on screens that should support `DarkColorScheme`.
- **Don't** overcrowd the bottom `NavigationBar` beyond 5 top-level destinations.
