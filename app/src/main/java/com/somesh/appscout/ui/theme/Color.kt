package com.somesh.appscout.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paddock visual system.
 *
 * The palette is intentionally narrow: a near-black canvas, charcoal layers, a single
 * racing-red accent, and a small set of signal colours used only for status.
 */

// ---- Canvas ----
val Asphalt = Color(0xFF06070A)          // deepest background
val Carbon = Color(0xFF0C0E12)           // primary surface
val Graphite = Color(0xFF15181E)         // raised surface
val Slate = Color(0xFF1E222A)            // hairline / dividers
val PitLaneGray = Color(0xFF111111)

// ---- Accents ----
val RacingRed = Color(0xFFE10600)        // signature accent
val RacingRedDeep = Color(0xFF8A0300)
val RacingRedGlow = Color(0xFFFF3B2F)
val EmberOrange = Color(0xFFFF6B00)
val PodiumGold = Color(0xFFF5C14E)
val Silver = Color(0xFFB8BEC9)
val Bronze = Color(0xFFC87B45)
val TrackLime = Color(0xFFC8F53C)        // "live / go" signal

// ---- Ink ----
val Chalk = Color(0xFFF7F8FA)
val Smoke = Color(0xFF9BA1AC)
val Ash = Color(0xFF6B7280)

// ---- Backwards-compatible aliases (earlier revisions referenced these names) ----
val TrackBlack = Asphalt
val CheckeredWhite = Chalk
val SteelGray = Smoke
val CarbonFiber = Graphite
val HazardOrange = EmberOrange
val WarningYellow = PodiumGold
val WindowBlack = Color(0xFF000000)

val PaddockCard = Carbon
val PaddockSurface = Asphalt
val PaddockSurfaceVariant = Graphite
val PaddockOnSurface = Chalk
val PaddockOnSurfaceVariant = Smoke

/**
 * Livery colours keyed by the Ergast/Jolpica `constructorId`.
 * Unknown constructors fall back to a deterministic accent so nothing renders colourless.
 */
private val LiveryByConstructorId: Map<String, Color> = mapOf(
    "red_bull" to Color(0xFF3671C6),
    "ferrari" to Color(0xFFE8002D),
    "mercedes" to Color(0xFF27F4D2),
    "mclaren" to Color(0xFFFF8000),
    "aston_martin" to Color(0xFF229971),
    "alpine" to Color(0xFF00A1E8),
    "williams" to Color(0xFF1868DB),
    "rb" to Color(0xFF6C98FF),
    "alphatauri" to Color(0xFF5E8FAA),
    "sauber" to Color(0xFF52E252),
    "kick_sauber" to Color(0xFF52E252),
    "audi" to Color(0xFF00E701),
    "haas" to Color(0xFFB6BABD),
    "cadillac" to Color(0xFFD4AF37),
    "lotus" to Color(0xFFFFB800),
    "renault" to Color(0xFFFFD800),
    "racing_point" to Color(0xFFF596C8),
    "force_india" to Color(0xFFF596C8),
    "toro_rosso" to Color(0xFF469BFF),
    "manor" to Color(0xFFE6002B)
)

private val FallbackLiveries = listOf(
    Color(0xFFE10600),
    Color(0xFF3671C6),
    Color(0xFF27F4D2),
    Color(0xFFFF8000),
    Color(0xFFB6BABD),
    Color(0xFF00A1E8),
    Color(0xFFF5C14E),
    Color(0xFF52E252)
)

fun liveryColor(constructorId: String?, teamName: String? = null): Color {
    val key = constructorId?.lowercase()?.trim().orEmpty()
    LiveryByConstructorId[key]?.let { return it }
    val seed = key.ifEmpty { teamName?.lowercase()?.trim().orEmpty() }.ifEmpty { "paddock" }
    val index = (seed.hashCode().toLong() and 0x7FFFFFFF).mod(FallbackLiveries.size.toLong()).toInt()
    return FallbackLiveries[index]
}
