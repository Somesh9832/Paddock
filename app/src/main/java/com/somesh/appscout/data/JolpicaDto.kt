package com.somesh.appscout.data

/**
 * Ergast-compatible (Jolpica) response shapes.
 *
 * Every field is nullable on purpose: the live API omits whole branches depending on the
 * endpoint (for example `/current.json` has no `StandingsTable` at all), and Gson happily
 * leaves missing fields null. Optional scalars are nullable so a schema change cannot
 * crash deserialisation.
 */

data class JolpicaResponse(val MRData: MRData?)

data class MRData(
    val xmlns: String? = null,
    val series: String? = null,
    val url: String? = null,
    val limit: String? = null,
    val offset: String? = null,
    val total: String? = null,
    val RaceTable: RaceTable? = null,
    val DriverTable: DriverTable? = null,
    val ConstructorTable: ConstructorTable? = null,
    val StandingsTable: StandingsTable? = null
)

data class RaceTable(
    val season: String? = null,
    val round: String? = null,
    val Races: List<RaceDto> = emptyList()
)

data class RaceDto(
    val season: String? = null,
    val round: String? = null,
    val raceName: String? = null,
    val Circuit: CircuitDto? = null,
    val date: String? = null,
    val time: String? = null,
    val url: String? = null,
    val FirstPractice: SessionDto? = null,
    val SecondPractice: SessionDto? = null,
    val ThirdPractice: SessionDto? = null,
    val Qualifying: SessionDto? = null,
    val Sprint: SessionDto? = null,
    val SprintQualifying: SessionDto? = null,
    val Results: List<ResultDto> = emptyList()
)

data class SessionDto(val date: String? = null, val time: String? = null)

data class CircuitDto(
    val circuitId: String? = null,
    val circuitName: String? = null,
    val url: String? = null,
    val Location: LocationDto? = null
)

data class LocationDto(
    val lat: String? = null,
    val lng: String? = null,
    val locality: String? = null,
    val country: String? = null
)

data class DriverTable(
    val season: String? = null,
    val Drivers: List<DriverDto> = emptyList()
)

data class DriverDto(
    val driverId: String? = null,
    val permanentNumber: String? = null,
    val code: String? = null,
    val url: String? = null,
    val givenName: String? = null,
    val familyName: String? = null,
    val dateOfBirth: String? = null,
    val nationality: String? = null,
    val name: String? = null
) {
    val fullName: String
        get() = name ?: listOfNotNull(givenName, familyName).joinToString(" ").trim()
}

data class ConstructorTable(
    val season: String? = null,
    val Constructors: List<ConstructorDto> = emptyList()
)

data class ConstructorDto(
    val constructorId: String? = null,
    val url: String? = null,
    val name: String? = null,
    val nationality: String? = null
)

/**
 * `/current/driverStandings.json` nests the payload one level deeper than a naive model
 * expects: `MRData.StandingsTable.StandingsLists[0].DriverStandings`.
 */
data class StandingsTable(
    val season: String? = null,
    val round: String? = null,
    val StandingsLists: List<StandingsList> = emptyList()
)

data class StandingsList(
    val season: String? = null,
    val round: String? = null,
    val DriverStandings: List<DriverStanding> = emptyList(),
    val ConstructorStandings: List<ConstructorStanding> = emptyList()
)

data class DriverStanding(
    val position: String? = null,
    val positionText: String? = null,
    val points: String? = null,
    val wins: String? = null,
    val Driver: DriverDto? = null,
    val Constructors: List<ConstructorDto> = emptyList()
)

data class ConstructorStanding(
    val position: String? = null,
    val positionText: String? = null,
    val points: String? = null,
    val wins: String? = null,
    val Constructor: ConstructorDto? = null
)

data class ResultTimeDto(val millis: String? = null, val time: String? = null)

data class FastestLapDto(val rank: String? = null, val lap: String? = null, val Time: ResultTimeDto? = null)

/** Single classification row returned by `/current/{round}/results.json`. */
data class ResultDto(
    val number: String? = null,
    val position: String? = null,
    val positionText: String? = null,
    val points: String? = null,
    val Driver: DriverDto? = null,
    val Constructor: ConstructorDto? = null,
    val grid: String? = null,
    val laps: String? = null,
    val status: String? = null,
    val Time: ResultTimeDto? = null,
    val FastestLap: FastestLapDto? = null
)
