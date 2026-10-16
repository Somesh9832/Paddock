# Paddock

Paddock is a Formula 1 companion app for Android, built with Kotlin, Jetpack Compose,
Material 3, MVVM and StateFlow.

The visual language is deliberately editorial rather than template-driven: near-black and
charcoal surfaces, a single racing-red accent, oversized tightly-tracked typography,
monospaced numerics, large integrated driver and circuit photography, diagonal accent
shapes and a squared-off component geometry.

## Screens

| Screen | Content |
| --- | --- |
| **Home** | Race-weekend hero for the next round, published session schedule, championship leader card, most recent classification, top-three standings preview and a constructors "team pulse" bar chart |
| **Calendar** | Season timeline with round numbers, dates, circuits and countries, plus a distinct up-next banner and completed/next/scheduled states |
| **Standings** | Drivers/constructors toggle, podium tiles with portraits, full driver classification and constructor classification with livery colours and driver line-ups |
| **Profile** | Favourite-driver hero with championship position, points and wins, plus a driver grid for changing the selection |
| **Driver detail** | Full-bleed portrait, oversized racing number, position/points/wins/number stat strip, identity rows, gap to leader and team-mates |
| **Race detail** | Circuit hero, round identity, venue information, weekend session times and the published classification |

## Data

Live championship data comes from the Ergast-compatible [Jolpica F1 API](https://api.jolpi.ca/ergast/f1/)
(`current.json`, `current/driverStandings.json`, `current/constructorStandings.json`,
`current/drivers.json`, `current/{round}/results.json`).

There is **no bundled sample or fictional data**. When a request fails the UI shows an
honest error with a retry action, and when the feed legitimately has nothing to show (for
example a round that has not run yet) the screen shows an explicit empty state rather than
invented results. Missing fields — session times, race times, date of birth, constructor
nationality — are treated as optional and degrade gracefully.

`CachingPaddockRepository` memoises successful payloads for the session so navigating
between screens does not repeat network round-trips. Failures are never cached.

## Project structure

```
data/       models, Jolpica DTOs, Retrofit service, remote + caching repositories
di/         small manual container
navigation/ destinations and bottom-tab mapping
state/      PaddockUiState and derived championship views
vm/         PaddockViewModel (favourite persistence, loading, results, retry)
view/       Compose screens plus the shared PaddockComponents design system
ui/theme/   colour, typography and Material 3 theme
```

## Build

Requirements: Android SDK and JDK 17 or newer.

```powershell
$env:GRADLE_USER_HOME = "$PWD\.gradle-user-home"
.\gradlew.bat assembleDebug
```

The generated debug APK is located at `app/build/outputs/apk/debug/app-debug.apk`.
Release builds are intentionally unsigned until a signing key is supplied outside source
control.

## Known limitations

- No offline database: without a network connection the app shows an error state, not cached content.
- No live timing, telemetry, notifications or standings history charts.
- Driver and circuit imagery is a shared editorial plate tinted by constructor livery, not licensed per-driver photography.
- Session times are rendered as published in UTC; the app does not convert them to the device time zone.
