package com.somesh.appscout.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.Instant
import java.util.Locale

/**
 * Domain models. Every field beyond the handful the API guarantees is nullable so the
 * UI can degrade gracefully instead of inventing values.
 */

data class Driver(
    val position: Int,
    val name: String,
    val code: String,
    val team: String,
    val points: Int,
    val number: Int,
    val country: String,
    val bio: String,
    /**
     * Stable Ergast/Jolpica identifier (for example "max_verstappen"). This is the key the
     * portrait pipeline uses, so a driver keeps the same photograph even if the display name
     * or their position in the standings changes.
     */
    val driverId: String? = null,
    val constructorId: String? = null,
    val wins: Int = 0,
    val dateOfBirth: String? = null,
    val permanentNumber: String? = null
) {
    /** "Max VERSTAPPEN" -> "VERSTAPPEN" style short label for dense rows. */
    val familyName: String by lazy { name.split(" ").lastOrNull() ?: name }
}

data class Constructor(
    val position: Int,
    val name: String,
    val points: Int,
    val drivers: String,
    val constructorId: String? = null,
    val nationality: String? = null,
    val wins: Int = 0
)

enum class RaceStatus { NEXT, COMPLETED, UPCOMING }

data class Race(
    val round: Int,
    val name: String,
    val venue: String,
    val country: String,
    val date: String,
    val status: RaceStatus,
    val locality: String? = null,
    val circuitId: String? = null,
    val season: String? = null,
    val time: String? = null,
    /** Present only when the API exposes a session schedule for the weekend. */
    val sessionDates: Map<String, String?> = emptyMap()
) {
    /** ISO-8601 local date of the race, when parseable. */
    val localDate: LocalDate? by lazy { date.toLocalDateOrNull() }

    /** Instant of the race start, when both date and UTC time are available. */
    val startInstant: Instant? by lazy {
        val d = localDate
        if (d == null) null
        else LocalDateTime.of(d, time.toLocalTimeOrNull() ?: LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC)
    }

    val dateLabel: String by lazy {
        localDate?.format(DAY_MONTH) ?: date
    }

    val weekdayLabel: String by lazy {
        localDate?.format(WEEKDAY)?.uppercase(Locale.ENGLISH) ?: ""
    }

    val monthLabel: String by lazy {
        localDate?.format(MONTH)?.uppercase(Locale.ENGLISH) ?: ""
    }

    val dayLabel: String by lazy { localDate?.dayOfMonth?.toString() ?: "" }

    val longDateLabel: String by lazy {
        localDate?.format(LONG_DATE) ?: date
    }

    /** Short country label used when the API only returns a full country name. */
    val countryLabel: String get() = country

    /** Grand Prix name without the trailing "Grand Prix" for display in tight spots. */
    val shortName: String get() = name.removeSuffix(" Grand Prix").trim().ifEmpty { name }

    val place: String get() = listOfNotNull(locality?.takeIf { it.isNotBlank() }, country.takeIf { it.isNotBlank() })
        .joinToString(", ")
        .ifEmpty { venue }
}

data class RaceResult(
    val position: Int,
    val positionText: String,
    val driverName: String,
    val driverCode: String,
    val driverId: String?,
    val constructorName: String,
    val constructorId: String?,
    val points: Double,
    val grid: Int?,
    val laps: Int?,
    val status: String,
    val timeMillis: Long?
) {
    val isPodium: Boolean get() = position in 1..3
    val displayName: String by lazy {
        driverName.split(" ").lastOrNull()?.uppercase(Locale.ENGLISH)
            ?: driverName.uppercase(Locale.ENGLISH)
    }
}

/*
 * Formatters are immutable and thread-safe, so a single shared instance is reused instead of
 * allocating a new one on every label read.
 */
private val DAY_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
private val WEEKDAY: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)
private val MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
private val LONG_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

internal fun String?.toLocalDateOrNull(): LocalDate? {
    if (this.isNullOrBlank()) return null
    return try {
        LocalDate.parse(this.trim())
    } catch (_: DateTimeParseException) {
        null
    }
}

internal fun String?.toLocalTimeOrNull(): LocalTime? {
    if (this.isNullOrBlank()) return null
    val raw = this.trim().removeSuffix("Z")
    return try {
        LocalTime.parse(raw)
    } catch (_: DateTimeParseException) {
        null
    }
}

/** Local-zone render of a UTC race start, used for the countdown and session rows. */
fun Instant.asLocalDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    LocalDateTime.ofInstant(this, zone)
