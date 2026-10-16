package com.somesh.appscout.data

import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate

/**
 * Live data source backed by the Jolpica (Ergast-compatible) API.
 *
 * Mapping notes:
 *  - standings live at `MRData.StandingsTable.StandingsLists[0]`
 *  - `/current.json` returns the whole season; the next round is derived from today's date
 *    rather than from array position
 *  - every field is defensively coerced, so a partial payload degrades instead of crashing
 */
class RemotePaddockRepository(
    private val baseUrl: String = "https://api.jolpi.ca/ergast/f1/"
) : PaddockRepository {

    companion object {
        private const val TAG = "RemotePaddockRepo"
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val service: JolpicaService by lazy { retrofit.create(JolpicaService::class.java) }

    override suspend fun drivers(): List<Driver> = withContext(Dispatchers.IO) {
        // The standings and the entry list are independent endpoints. They used to be requested
        // one after the other, which put two sequential round-trips on the startup path; running
        // them together halves the wait on first paint.
        coroutineScope {
            val standingsJob = async { service.getDriverStandings() }

            // Best-effort enrichment: date of birth / nationality live on the entry list, not the
            // standings payload. A failure here must not take down the standings themselves.
            val entryListJob = async {
                runCatching {
                    service.getSeasonDrivers().MRData?.DriverTable?.Drivers.orEmpty()
                        .associateBy { it.driverId.orEmpty() }
                }.getOrDefault(emptyMap())
            }

            val response = standingsJob.await()
            val entryList = entryListJob.await()

            val standings = response.MRData?.StandingsTable?.StandingsLists?.firstOrNull()?.DriverStandings
                ?: emptyList()

            Log.d(TAG, "driver standings rows: ${standings.size}, entry list: ${entryList.size}")

            standings.map { row ->
                val dto = row.Driver
                val driverId = dto?.driverId.orEmpty()
                val entry = entryList[driverId]
                val constructor = row.Constructors.firstOrNull()
                Driver(
                    position = row.position?.toIntOrNull() ?: 0,
                    name = (dto?.fullName ?: "").ifBlank { "Unknown driver" },
                    code = dto?.code?.uppercase().orEmpty(),
                    team = constructor?.name.orEmpty(),
                    points = row.points?.toDoubleOrNull()?.toInt() ?: 0,
                    number = dto?.permanentNumber?.toIntOrNull() ?: 0,
                    country = dto?.nationality ?: entry?.nationality.orEmpty(),
                    bio = "",
                    driverId = driverId.ifBlank { null },
                    constructorId = constructor?.constructorId,
                    wins = row.wins?.toIntOrNull() ?: 0,
                    dateOfBirth = dto?.dateOfBirth ?: entry?.dateOfBirth,
                    permanentNumber = dto?.permanentNumber
                )
            }
        }
    }

    override suspend fun constructors(): List<Constructor> = withContext(Dispatchers.IO) {
        val response = service.getConstructorStandings()
        val standings = response.MRData?.StandingsTable?.StandingsLists?.firstOrNull()?.ConstructorStandings
            ?: emptyList()
        Log.d(TAG, "constructor standings rows: ${standings.size}")

        standings.map { row ->
            Constructor(
                position = row.position?.toIntOrNull() ?: 0,
                name = row.Constructor?.name ?: "Unknown team",
                points = row.points?.toDoubleOrNull()?.toInt() ?: 0,
                drivers = "",
                constructorId = row.Constructor?.constructorId,
                nationality = row.Constructor?.nationality,
                wins = row.wins?.toIntOrNull() ?: 0
            )
        }
    }

    override suspend fun races(): List<Race> = withContext(Dispatchers.IO) {
        val response = service.getCurrentSeasonRaces()
        val dtos = response.MRData?.RaceTable?.Races ?: emptyList()
        Log.d(TAG, "race calendar rows: ${dtos.size}")
        val today = LocalDate.now()

        dtos.map { dto ->
            val date = dto.date?.toLocalDateOrNull()
            val status = when {
                date == null -> RaceStatus.UPCOMING
                date.isBefore(today) -> RaceStatus.COMPLETED
                else -> RaceStatus.UPCOMING
            }
            Race(
                round = dto.round?.toIntOrNull() ?: 0,
                name = dto.raceName ?: "Grand Prix",
                venue = dto.Circuit?.circuitName.orEmpty(),
                country = dto.Circuit?.Location?.country.orEmpty(),
                date = dto.date.orEmpty(),
                status = status,
                locality = dto.Circuit?.Location?.locality,
                circuitId = dto.Circuit?.circuitId,
                season = dto.season ?: response.MRData?.RaceTable?.season,
                time = dto.time,
                sessionDates = buildMap {
                    dto.FirstPractice?.let { put("Practice 1", sessionStamp(it)) }
                    dto.SecondPractice?.let { put("Practice 2", sessionStamp(it)) }
                    dto.ThirdPractice?.let { put("Practice 3", sessionStamp(it)) }
                    dto.SprintQualifying?.let { put("Sprint Quali", sessionStamp(it)) }
                    dto.Sprint?.let { put("Sprint", sessionStamp(it)) }
                    dto.Qualifying?.let { put("Qualifying", sessionStamp(it)) }
                    dto.time?.let { put("Race", sessionStamp(SessionDto(dto.date, it))) }
                }
            )
        }
    }

    override suspend fun results(round: Int): List<RaceResult> = withContext(Dispatchers.IO) {
        if (round <= 0) return@withContext emptyList()
        val response = runCatching { service.getRaceResults(round) }.getOrNull()
        val dtos = response?.MRData?.RaceTable?.Races?.firstOrNull()?.Results ?: emptyList()
        Log.d(TAG, "results for round $round: ${dtos.size}")

        dtos.map { dto ->
            RaceResult(
                position = dto.position?.toIntOrNull() ?: 0,
                positionText = dto.positionText.orEmpty(),
                driverName = dto.Driver?.fullName.orEmpty(),
                driverCode = dto.Driver?.code?.uppercase().orEmpty(),
                driverId = dto.Driver?.driverId,
                constructorName = dto.Constructor?.name.orEmpty(),
                constructorId = dto.Constructor?.constructorId,
                points = dto.points?.toDoubleOrNull() ?: 0.0,
                grid = dto.grid?.toIntOrNull(),
                laps = dto.laps?.toIntOrNull(),
                status = dto.status.orEmpty(),
                timeMillis = dto.Time?.millis?.toLongOrNull()
            )
        }
    }

    private fun sessionStamp(session: SessionDto): String? {
        val date = session.date ?: return null
        val time = session.time?.removeSuffix("Z")
        return if (time.isNullOrBlank()) date else "${date}T$time"
    }
}
