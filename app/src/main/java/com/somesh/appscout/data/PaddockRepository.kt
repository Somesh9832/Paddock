package com.somesh.appscout.data

/**
 * Single read surface for Paddock data. Implementations are responsible for mapping the
 * transport schema into the domain models in [PaddockModels].
 */
interface PaddockRepository {
    suspend fun drivers(): List<Driver>
    suspend fun constructors(): List<Constructor>
    suspend fun races(): List<Race>

    /** Classification for one round. Empty when the session has not run yet. */
    suspend fun results(round: Int): List<RaceResult>
}
