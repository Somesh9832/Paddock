package com.somesh.appscout.view

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.somesh.appscout.navigation.BottomTab
import com.somesh.appscout.navigation.PaddockDestination
import com.somesh.appscout.navigation.destination
import com.somesh.appscout.navigation.isDetail
import com.somesh.appscout.navigation.parentTab
import com.somesh.appscout.ui.theme.Asphalt
import com.somesh.appscout.ui.theme.Carbon
import com.somesh.appscout.ui.theme.Chalk
import com.somesh.appscout.ui.theme.RacingRed
import com.somesh.appscout.ui.theme.Slate
import com.somesh.appscout.ui.theme.Smoke
import com.somesh.appscout.vm.PaddockViewModel

@Composable
fun PaddockApp(viewModel: PaddockViewModel = viewModel()) {
    // Lifecycle-aware collection: the app stops observing the ViewModel state while it is in the
    // background, so nothing is recomposed or re-emitted off-screen.
    val state by viewModel.state.collectAsStateWithLifecycle()
    var destination by remember { mutableStateOf<PaddockDestination>(PaddockDestination.Home) }

    val selectedTab = destination.parentTab()

    BackHandler(enabled = destination.isDetail) {
        destination = when (destination) {
            is PaddockDestination.DriverDetail -> PaddockDestination.Standings
            is PaddockDestination.RaceDetail -> PaddockDestination.Calendar
            else -> destination
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Asphalt,
        bottomBar = {
            if (!destination.isDetail) {
                PaddockBottomBar(
                    selected = selectedTab,
                    onSelect = { tab -> destination = tab.destination() }
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = destination,
            transitionSpec = {
                val forward = targetState.isDetail
                val enter = slideInHorizontally(tween(280)) { width -> if (forward) width / 4 else -width / 5 } +
                    fadeIn(tween(220))
                val exit = slideOutHorizontally(tween(240)) { width -> if (forward) -width / 6 else width / 6 } +
                    fadeOut(tween(180))
                enter togetherWith exit
            },
            label = "paddockNavigation"
        ) { screen ->
            when (screen) {
                PaddockDestination.Home -> HomeScreen(
                    state = state,
                    pad = padding,
                    onRace = { round ->
                        viewModel.openRace(round)
                        destination = PaddockDestination.RaceDetail(round)
                    },
                    onOpenStandings = { destination = PaddockDestination.Standings },
                    onDriver = { code -> destination = PaddockDestination.DriverDetail(code) },
                    onRetry = viewModel::retry
                )

                PaddockDestination.Calendar -> CalendarScreen(
                    state = state,
                    pad = padding,
                    onRace = { round ->
                        viewModel.openRace(round)
                        destination = PaddockDestination.RaceDetail(round)
                    },
                    onRetry = viewModel::retry
                )

                PaddockDestination.Standings -> StandingsScreen(
                    state = state,
                    pad = padding,
                    onChampionship = viewModel::selectChampionship,
                    onDriver = { code -> destination = PaddockDestination.DriverDetail(code) },
                    onRetry = viewModel::retry
                )

                PaddockDestination.Profile -> ProfileScreen(
                    state = state,
                    pad = padding,
                    onChooseDriver = { destination = PaddockDestination.Standings },
                    onDriver = { code -> destination = PaddockDestination.DriverDetail(code) },
                    onClearFavourite = viewModel::clearFavourite,
                    onRetry = viewModel::retry
                )

                is PaddockDestination.DriverDetail -> DriverDetailScreen(
                    driver = state.driver(screen.driverCode),
                    state = state,
                    isFavourite = state.favouriteDriverCode.equals(screen.driverCode, ignoreCase = true),
                    pad = padding,
                    onFavourite = viewModel::setFavourite,
                    onBack = { destination = PaddockDestination.Standings }
                )

                is PaddockDestination.RaceDetail -> RaceDetailScreen(
                    race = state.race(screen.round),
                    results = if (state.selectedRound == screen.round) state.raceResults else emptyList(),
                    resultsLoading = state.resultsLoading && state.selectedRound == screen.round,
                    resultsError = if (state.selectedRound == screen.round) state.resultsError else null,
                    pad = padding,
                    onBack = { destination = PaddockDestination.Calendar },
                    onRetryResults = viewModel::retryResults
                )
            }
        }
    }
}

/**
 * Custom bottom navigation. Deliberately not a Material NavigationBar: squared selection
 * plate, team-red indicator and a monospaced caption keep the shell on-brand.
 */
@Composable
private fun PaddockBottomBar(selected: BottomTab, onSelect: (BottomTab) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Carbon)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Slate)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab.entries.forEach { tab ->
                BottomTabItem(
                    tab = tab,
                    selected = tab == selected,
                    onClick = { onSelect(tab) }
                )
            }
        }
    }
}

@Composable
private fun BottomTabItem(tab: BottomTab, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(84.dp)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(if (selected) 26.dp else 8.dp)
                .height(3.dp)
                .background(if (selected) RacingRed else Color.Transparent)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = tab.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (selected) Chalk else Smoke
        )
        Text(
            text = tab.caption,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = if (selected) RacingRed else Smoke.copy(alpha = 0.7f)
        )
    }
}
