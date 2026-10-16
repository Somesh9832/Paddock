package com.somesh.appscout.state

import com.somesh.appscout.data.Constructor
import com.somesh.appscout.data.Driver
import com.somesh.appscout.data.Race
import com.somesh.appscout.data.RaceResult
import com.somesh.appscout.data.RaceStatus

enum class Championship { DRIVERS, CONSTRUCTORS }

data class PaddockUiState(
    val drivers: List<Driver> = emptyList(),
    val constructors: List<Constructor> = emptyList(),
    val races: List<Race> = emptyList(),
    val favouriteDriverCode: String? = null,
    val championship: Championship = Championship.DRIVERS,
    val isLoading: Boolean = false,
    /** Non-null only when the live request failed. Never accompanied by invented data. */
    val errorMessage: String? = null,
    val lastUpdatedEpochMillis: Long? = null,
    val selectedRound: Int? = null,
    val raceResults: List<RaceResult> = emptyList(),
    val resultsLoading: Boolean = false,
    val resultsError: String? = null,
    /** Classification for the most recent completed round, preloaded for the Home screen. */
    val homeResults: List<RaceResult> = emptyList()
) {
    /*
     * Derived collections are computed once per state instance. A new PaddockUiState is emitted
     * only when something actually changes, so caching here keeps repeated filtering and sorting
     * out of every recomposition that reads these properties.
     */
    val leader: Driver? by lazy { drivers.minByOrNull { it.position } }
    val constructorLeader: Constructor? by lazy { constructors.minByOrNull { it.position } }

    val completedRaces: List<Race> by lazy { races.filter { it.status == RaceStatus.COMPLETED } }

    /** The next race that has not finished yet, falling back to the last round of the season. */
    val nextRace: Race? by lazy { upcomingRace ?: races.lastOrNull() }

    /** Strictly the next race still ahead of us; null once the season is complete. */
    val upcomingRace: Race? by lazy { races.firstOrNull { it.status != RaceStatus.COMPLETED } }

    /** Most recent race with a known result, if the calendar is far enough along. */
    val latestCompletedRace: Race? by lazy { completedRaces.lastOrNull() }

    /** Season label sourced from the payload so nothing is hardcoded. */
    val seasonLabel: String? by lazy {
        races.firstOrNull()?.season?.takeIf { it.isNotBlank() }
            ?: races.firstOrNull()?.date?.take(4)?.takeIf { it.length == 4 }
    }

    val hasData: Boolean get() = drivers.isNotEmpty() || races.isNotEmpty() || constructors.isNotEmpty()

    fun driver(code: String): Driver? = drivers.find { it.code.equals(code, ignoreCase = true) }

    fun favourite(): Driver? = favouriteDriverCode?.let(::driver)

    fun race(round: Int): Race? = races.find { it.round == round }

    fun driversFor(constructorId: String?): List<Driver> =
        if (constructorId.isNullOrBlank()) emptyList()
        else drivers.filter { it.constructorId == constructorId }

    val podium: List<Driver> by lazy { drivers.sortedBy { it.position }.take(3) }
}
