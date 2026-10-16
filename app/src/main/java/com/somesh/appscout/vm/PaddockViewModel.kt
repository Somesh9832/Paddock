package com.somesh.appscout.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.somesh.appscout.di.PaddockContainer
import com.somesh.appscout.state.Championship
import com.somesh.appscout.state.PaddockUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.IOException

class PaddockViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PaddockContainer.repository
    private val preferences =
        application.getSharedPreferences("paddock_preferences", Application.MODE_PRIVATE)

    private val _state = MutableStateFlow(PaddockUiState())
    val state: StateFlow<PaddockUiState> = _state.asStateFlow()

    companion object {
        private const val TAG = "PaddockViewModel"
        private const val FAVOURITE_KEY = "favourite_driver"
    }

    init {
        _state.update { it.copy(favouriteDriverCode = preferences.getString(FAVOURITE_KEY, null)) }
        viewModelScope.launch { loadData() }
    }

    private suspend fun loadData() {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            // Standings and calendar are independent endpoints: fetch them concurrently and
            // let whichever ones succeed populate the screen.
            val (drivers, constructors, races) = coroutineScope {
                val driversJob = async { runCatching { repository.drivers() } }
                val constructorsJob = async { runCatching { repository.constructors() } }
                val racesJob = async { runCatching { repository.races() } }
                Triple(driversJob.await(), constructorsJob.await(), racesJob.await())
            }

            val failures = listOf(drivers, constructors, races).count { it.isFailure }
            val loadedDrivers = drivers.getOrDefault(emptyList())
            val loadedConstructors = constructors.getOrDefault(emptyList())
            val loadedRaces = races.getOrDefault(emptyList())

            Log.d(
                TAG,
                "loaded drivers=${loadedDrivers.size} constructors=${loadedConstructors.size} races=${loadedRaces.size} failures=$failures"
            )

            _state.update {
                it.copy(
                    drivers = loadedDrivers,
                    constructors = loadedConstructors,
                    races = loadedRaces,
                    isLoading = false,
                    errorMessage = if (failures > 0) friendlyError(listOf(drivers, constructors, races)) else null,
                    lastUpdatedEpochMillis = if (failures < 3) System.currentTimeMillis() else it.lastUpdatedEpochMillis
                )
            }

            // Home shows the most recent classification; fetch it once so the screen is
            // complete on first paint instead of after a second tap.
            _state.value.latestCompletedRace?.round?.let { round ->
                val homeResults = runCatching { repository.results(round) }.getOrDefault(emptyList())
                _state.update { it.copy(homeResults = homeResults) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "loadData failed", e)
            _state.update {
                it.copy(
                    isLoading = false,
                    errorMessage = friendlyError(listOf(Result.failure<Unit>(e)))
                )
            }
        }
    }

    private fun friendlyError(results: List<Result<*>>): String {
        val cause = results.firstOrNull { it.isFailure }?.exceptionOrNull()
        return when (cause) {
            is IOException -> "No connection to the F1 data service. Check your network and retry."
            null -> "Some championship data could not be loaded."
            else -> "Could not reach the F1 data service. Pull to retry."
        }
    }

    fun selectChampionship(championship: Championship) =
        _state.update { it.copy(championship = championship) }

    fun setFavourite(code: String) {
        val next = if (_state.value.favouriteDriverCode == code) null else code
        preferences.edit().apply {
            if (next == null) remove(FAVOURITE_KEY) else putString(FAVOURITE_KEY, next)
        }.apply()
        _state.update { it.copy(favouriteDriverCode = next) }
    }

    fun clearFavourite() {
        preferences.edit().remove(FAVOURITE_KEY).apply()
        _state.update { it.copy(favouriteDriverCode = null) }
    }

    /** Loads classification for a round. Results are empty (not an error) before a race runs. */
    fun openRace(round: Int) {
        if (_state.value.selectedRound == round && !_state.value.resultsLoading) return
        _state.update {
            it.copy(
                selectedRound = round,
                raceResults = emptyList(),
                resultsLoading = true,
                resultsError = null
            )
        }
        viewModelScope.launch {
            val result = runCatching { repository.results(round) }
            _state.update {
                it.copy(
                    raceResults = result.getOrDefault(emptyList()),
                    resultsLoading = false,
                    resultsError = result.exceptionOrNull()?.let { _ ->
                        "Results for this round could not be loaded."
                    }
                )
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            repository.invalidate()
            loadData()
        }
    }

    fun retryResults() {
        val round = _state.value.selectedRound ?: return
        viewModelScope.launch {
            repository.forgetResults(round)
            _state.update { it.copy(selectedRound = null) }
            openRace(round)
        }
    }
}
