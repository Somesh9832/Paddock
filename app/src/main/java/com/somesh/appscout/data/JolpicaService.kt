package com.somesh.appscout.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Ergast-compatible endpoints exposed by the Jolpica mirror. */
interface JolpicaService {

    /** Full race calendar for the season currently in progress. */
    @GET("current.json")
    suspend fun getCurrentSeasonRaces(
        @Query("limit") limit: Int = 100
    ): JolpicaResponse

    @GET("current/driverStandings.json")
    suspend fun getDriverStandings(
        @Query("limit") limit: Int = 100
    ): JolpicaResponse

    @GET("current/constructorStandings.json")
    suspend fun getConstructorStandings(
        @Query("limit") limit: Int = 100
    ): JolpicaResponse

    /** Classification for one round; returns an empty result set before the race runs. */
    @GET("current/{round}/results.json")
    suspend fun getRaceResults(
        @Path("round") round: Int,
        @Query("limit") limit: Int = 100
    ): JolpicaResponse

    /** Season entry list, used to enrich standings with nationality / date of birth. */
    @GET("current/drivers.json")
    suspend fun getSeasonDrivers(
        @Query("limit") limit: Int = 100
    ): JolpicaResponse
}
