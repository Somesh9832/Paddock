package com.somesh.appscout

import com.somesh.appscout.data.Constructor
import com.somesh.appscout.data.Driver
import com.somesh.appscout.data.Race
import com.somesh.appscout.data.RaceStatus
import com.somesh.appscout.state.Championship
import com.somesh.appscout.state.PaddockUiState
import com.somesh.appscout.view.countryCode
import com.somesh.appscout.view.driverVariant
import com.somesh.appscout.view.formatPoints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure model / formatting coverage. Nothing here performs network I/O, so the suite stays
 * deterministic and does not depend on the live championship feed.
 */
class PaddockModelsTest {

    private fun driver(
        position: Int,
        name: String,
        code: String,
        team: String,
        points: Int,
        constructorId: String,
        wins: Int = 0
    ) = Driver(
        position = position,
        name = name,
        code = code,
        team = team,
        points = points,
        number = position * 10,
        country = "Test",
        bio = "",
        constructorId = constructorId,
        wins = wins
    )

    private fun race(round: Int, date: String, status: RaceStatus) = Race(
        round = round,
        name = "Round $round Grand Prix",
        venue = "Circuit $round",
        country = "Testland",
        date = date,
        status = status
    )

    @Test
    fun `state resolves favourite driver and defaults to drivers championship`() {
        val state = PaddockUiState(
            drivers = listOf(
                driver(2, "Noah Hart", "NHA", "Velocity GP", 169, "velocity"),
                driver(1, "Alex Vega", "AVE", "Orion Racing", 182, "orion", wins = 4)
            ),
            favouriteDriverCode = "NHA"
        )

        assertEquals("Noah Hart", state.favourite()?.name)
        assertEquals(Championship.DRIVERS, state.championship)
        assertEquals("Alex Vega", state.leader?.name)
        assertEquals(listOf(1, 2), state.podium.map { it.position })
    }

    @Test
    fun `upcoming race ignores completed rounds and next race falls back to the finale`() {
        val completed = listOf(
            race(1, "2026-03-08", RaceStatus.COMPLETED),
            race(2, "2026-03-15", RaceStatus.COMPLETED)
        )
        val mixed = PaddockUiState(races = completed + race(3, "2026-03-29", RaceStatus.UPCOMING))
        assertEquals(3, mixed.upcomingRace?.round)
        assertEquals(3, mixed.nextRace?.round)

        val finished = PaddockUiState(races = completed)
        assertNull(finished.upcomingRace)
        assertEquals(2, finished.nextRace?.round)
        assertEquals(2, finished.latestCompletedRace?.round)
    }

    @Test
    fun `race exposes parsed date labels and start instant`() {
        val race = Race(
            round = 9,
            name = "British Grand Prix",
            venue = "Silverstone Circuit",
            country = "United Kingdom",
            date = "2026-07-05",
            status = RaceStatus.UPCOMING,
            locality = "Silverstone",
            time = "14:00:00Z"
        )

        assertEquals("British", race.shortName)
        assertEquals("5", race.dayLabel)
        assertEquals("JUL", race.monthLabel)
        assertEquals("SUN", race.weekdayLabel)
        assertEquals("5 July 2026", race.longDateLabel)
        assertEquals("Silverstone, United Kingdom", race.place)
        // Computed from the JVM calendar rather than a hand-derived constant.
        val expected = java.time.LocalDateTime.of(2026, 7, 5, 14, 0)
            .toInstant(java.time.ZoneOffset.UTC)
            .epochSecond
        assertEquals(expected, race.startInstant?.epochSecond)
    }

    @Test
    fun `race with unusable date degrades to raw strings instead of throwing`() {
        val race = Race(
            round = 1,
            name = "Unknown Grand Prix",
            venue = "",
            country = "",
            date = "TBC",
            status = RaceStatus.UPCOMING
        )

        assertNull(race.localDate)
        assertNull(race.startInstant)
        assertEquals("TBC", race.dateLabel)
        assertEquals("", race.dayLabel)
        assertEquals("Unknown", race.shortName)
    }

    @Test
    fun `drivers for constructor filters the championship list`() {
        val state = PaddockUiState(
            drivers = listOf(
                driver(1, "Alex Vega", "AVE", "Orion Racing", 182, "orion"),
                driver(2, "Noah Hart", "NHA", "Velocity GP", 169, "velocity"),
                driver(5, "Theo Laurent", "TLA", "Orion Racing", 106, "orion")
            ),
            constructors = listOf(Constructor(1, "Orion Racing", 288, "", constructorId = "orion"))
        )

        assertEquals(listOf("AVE", "TLA"), state.driversFor("orion").map { it.code })
        assertEquals(emptyList<String>(), state.driversFor(null).map { it.code })
        assertEquals("Orion Racing", state.constructorLeader?.name)
    }

    @Test
    fun `country codes cover nationality spellings and unknown fallbacks`() {
        assertEquals("GB", countryCode("British"))
        assertEquals("GB", countryCode("United Kingdom"))
        assertEquals("NL", countryCode("Dutch"))
        assertEquals("MC", countryCode("Monegasque"))
        assertEquals("", countryCode(null))
        assertEquals("ZZ", countryCode("Zzyzx"))
    }

    @Test
    fun `points formatting drops trailing zeros but keeps halves`() {
        assertEquals("25", formatPoints(25.0))
        assertEquals("1.5", formatPoints(1.5))
        assertEquals("0", formatPoints(0.0))
    }

    @Test
    fun `driver portrait variant is stable per driver and spread across the range`() {
        assertEquals(driverVariant("VER", "Max Verstappen"), driverVariant("VER", "Max Verstappen"))
        assertNotEquals(driverVariant("VER", null), driverVariant("HAM", null))
        val variants = listOf("VER", "HAM", "LEC", "NOR", "PIA", "RUS", "SAI").map { driverVariant(it, null) }
        assertTrue(variants.all { it in 0..6 })
    }
}
