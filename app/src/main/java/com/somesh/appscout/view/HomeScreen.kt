package com.somesh.appscout.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somesh.appscout.data.Driver
import com.somesh.appscout.data.Race
import com.somesh.appscout.data.RaceResult
import com.somesh.appscout.ui.theme.liveryColor
import com.somesh.appscout.state.PaddockUiState
import com.somesh.appscout.ui.theme.Asphalt
import com.somesh.appscout.ui.theme.Chalk
import com.somesh.appscout.ui.theme.Graphite
import com.somesh.appscout.ui.theme.NumericStyle
import com.somesh.appscout.ui.theme.RacingRed
import com.somesh.appscout.ui.theme.Slate
import com.somesh.appscout.ui.theme.Smoke
import com.somesh.appscout.ui.theme.TrackLime

@Composable
fun HomeScreen(
    state: PaddockUiState,
    pad: PaddingValues,
    onRace: (Int) -> Unit,
    onOpenStandings: () -> Unit,
    onDriver: (String) -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Asphalt)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = pad.calculateBottomPadding() + 28.dp)
    ) {
        item { HomeTopBar(state) }

        if (state.errorMessage != null) {
            item {
                ErrorPanel(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = PaddockDimens.gutter, vertical = 8.dp)
                )
            }
        }

        if (state.isLoading && !state.hasData) {
            item { LoadingBlock() }
            return@LazyColumn
        }

        if (!state.hasData) {
            item {
                EmptyPanel(
                    title = "Season data unavailable",
                    body = "Paddock could not read the championship feed. Check your connection and retry.",
                    modifier = Modifier.padding(PaddockDimens.gutter)
                )
            }
            return@LazyColumn
        }

        // Only promote the weekend hero when a race is genuinely still ahead of us; once the
        // season is over `nextRace` falls back to the finale and the banner would be misleading.
        val upcoming = state.upcomingRace
        if (upcoming != null) {
            item { RaceWeekendHero(race = upcoming, onClick = { onRace(upcoming.round) }) }
            item { WeekendSessions(race = upcoming) }
        } else {
            state.latestCompletedRace?.let { finale ->
                item { SeasonCompleteBanner(race = finale, onClick = { onRace(finale.round) }) }
            }
        }

        state.leader?.let { leader ->
            item { ChampionshipLeaderCard(leader = leader, onOpen = { onDriver(leader.code) }) }
        }

        val latest = state.latestCompletedRace
        if (latest != null) {
            item {
                RecentResultBlock(
                    race = latest,
                    results = state.homeResults,
                    onOpen = { onRace(latest.round) }
                )
            }
        }

        if (state.podium.isNotEmpty()) {
            item {
                StandingsPreview(
                    drivers = state.podium,
                    seasonLabel = state.seasonLabel,
                    onOpenStandings = onOpenStandings,
                    onDriver = onDriver
                )
            }
        }

        if (state.constructors.isNotEmpty()) {
            item { TeamPulse(constructors = state.constructors.take(4)) }
        }

        item {
            FooterNote(lastUpdated = state.lastUpdatedEpochMillis)
        }
    }
}

@Composable
private fun HomeTopBar(state: PaddockUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddockDimens.gutter, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(26.dp)
                .background(RacingRed)
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PADDOCK",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, letterSpacing = 3.sp),
                color = Chalk
            )
            Text(
                text = "FORMULA 1 COMPANION",
                style = MaterialTheme.typography.labelSmall,
                color = Smoke
            )
        }
        state.seasonLabel?.let { season ->
            OutlinedLabel(text = season, color = RacingRed)
        }
    }
}

@Composable
private fun LoadingBlock() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = RacingRed, strokeWidth = 2.dp)
        Spacer(Modifier.height(14.dp))
        Kicker(text = "Loading championship")
    }
}

/* ------------------------------------------------------------------ race weekend hero */

@Composable
private fun RaceWeekendHero(race: Race, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PaddockDimens.heroHeight)
            .clickable { onClick() }
    ) {
        CircuitPlate(modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Asphalt.copy(alpha = 0.55f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(PaddockDimens.gutter)
        ) {
            StatusPill(text = "Next round")
            Spacer(Modifier.height(12.dp))
            Text(
                text = "ROUND ${race.round}",
                style = MaterialTheme.typography.labelLarge,
                color = Chalk
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(PaddockDimens.gutter)
        ) {
            Text(
                text = race.shortName.uppercase(),
                style = MaterialTheme.typography.displayMedium,
                color = Chalk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "GRAND PRIX",
                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 6.sp),
                color = RacingRed
            )
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(2.dp)
                        .background(RacingRed)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = race.longDateLabel.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Chalk
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = listOfNotNull(
                    race.venue.takeIf { it.isNotBlank() },
                    race.country.takeIf { it.isNotBlank() }
                ).joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SeasonCompleteBanner(race: Race, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable { onClick() }
    ) {
        CircuitPlate(modifier = Modifier.fillMaxSize(), accent = com.somesh.appscout.ui.theme.PodiumGold)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(PaddockDimens.gutter)
        ) {
            OutlinedLabel(text = "Season complete", color = com.somesh.appscout.ui.theme.PodiumGold)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "FINAL ROUND",
                style = MaterialTheme.typography.labelLarge,
                color = RacingRed
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = race.shortName.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = Chalk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${race.longDateLabel.uppercase()}  ·  ${race.venue}",
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeekendSessions(race: Race) {
    val sessions = race.sessionDates.entries.toList()
    if (sessions.isEmpty()) {
        Column(modifier = Modifier.padding(PaddockDimens.gutter)) {
            EmptyPanel(
                title = "Session schedule pending",
                body = "The timing feed has not published session times for round ${race.round} yet."
            )
        }
        return
    }

    Column(modifier = Modifier.padding(top = 24.dp)) {
        SectionHeading(
            kicker = "Race weekend",
            title = "Session schedule",
            modifier = Modifier.padding(horizontal = PaddockDimens.gutter)
        )
        Spacer(Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = PaddockDimens.gutter),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sessions) { entry ->
                SessionTile(name = entry.key, stamp = entry.value)
            }
        }
    }
}

@Composable
private fun SessionTile(name: String, stamp: String?) {
    val date = stamp?.substringBefore("T")
    val time = stamp?.substringAfter("T", "")
    val isRace = name == "Race"
    Column(
        modifier = Modifier
            .width(132.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (isRace) RacingRed.copy(alpha = 0.14f) else Graphite)
            .padding(14.dp)
    ) {
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isRace) RacingRed else Smoke,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = date?.takeLast(5)?.replace("-", "/") ?: "TBC",
            style = MaterialTheme.typography.titleMedium,
            color = Chalk
        )
        Text(
            text = if (time.isNullOrBlank()) "Time TBC" else "${time.take(5)} UTC",
            style = MaterialTheme.typography.bodySmall,
            color = Smoke
        )
    }
}

/* ------------------------------------------------------------------ championship leader */

@Composable
private fun ChampionshipLeaderCard(leader: Driver, onOpen: () -> Unit) {
    Column(modifier = Modifier.padding(top = PaddockDimens.sectionGap)) {
        SectionHeading(
            kicker = "Championship",
            title = "Title leader",
            modifier = Modifier.padding(horizontal = PaddockDimens.gutter)
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddockDimens.gutter)
                .height(200.dp)
                .clickable { onOpen() }
        ) {
            Box(
                modifier = Modifier
                    .weight(0.42f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(3.dp))
            ) {
                DriverPortrait(
                    variant = driverVariant(leader.code, leader.name),
                    tint = liveryColor(leader.constructorId, leader.team),
                    tintStrength = 0.28f,
                    driverId = leader.driverId,
                    modifier = Modifier.fillMaxSize()
                )
                HeroScrim(topAlpha = 0f, bottomAlpha = 0.6f)
                StatusPill(
                    text = "P1",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    color = com.somesh.appscout.ui.theme.PodiumGold
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier
                    .weight(0.58f)
                    .fillMaxSize()
            ) {
                TeamAccentBar(
                    constructorId = leader.constructorId,
                    teamName = leader.team,
                    height = 3.dp,
                    width = 46.dp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = leader.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Chalk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = leader.team.ifBlank { "Constructor unavailable" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Smoke,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = leader.points.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = RacingRed
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "PTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Smoke,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${leader.wins} wins  ·  No. ${leader.permanentNumber ?: leader.number.toString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Smoke
                )
            }
        }
    }
}

/* ------------------------------------------------------------------ recent result */

@Composable
private fun RecentResultBlock(race: Race, results: List<RaceResult>, onOpen: () -> Unit) {
    Column(modifier = Modifier.padding(top = PaddockDimens.sectionGap)) {
        SectionHeading(
            kicker = "Last time out",
            title = race.shortName + " GP",
            modifier = Modifier.padding(horizontal = PaddockDimens.gutter),
            trailing = {
                Text(
                    text = "DETAILS",
                    style = MaterialTheme.typography.labelSmall,
                    color = RacingRed,
                    modifier = Modifier
                        .clickable { onOpen() }
                        .padding(4.dp)
                )
            }
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = race.longDateLabel.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Smoke,
            modifier = Modifier.padding(horizontal = PaddockDimens.gutter)
        )
        Spacer(Modifier.height(14.dp))

        if (results.isEmpty()) {
            Box(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                EmptyPanel(
                    title = "Classification not published",
                    body = "Timing data for round ${race.round} is not available from the feed yet.",
                    accent = Slate
                )
            }
            return
        }

        Column(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
            results.take(5).forEachIndexed { index, result ->
                ResultRow(result = result)
                if (index != results.take(5).lastIndex) TrackDivider(modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun ResultRow(result: RaceResult) {
    val accent = liveryColor(result.constructorId, result.constructorName)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (result.isPodium) accent.copy(alpha = 0.06f) else Color.Transparent)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = result.positionText.ifBlank { result.position.toString() },
            style = NumericStyle.copy(fontSize = 18.sp),
            color = podiumColor(result.position),
            modifier = Modifier.width(34.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = Chalk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = result.constructorName.ifBlank { result.status.ifBlank { "—" } },
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (result.points > 0) "+${formatPoints(result.points)}" else "—",
                style = NumericStyle.copy(fontSize = 14.sp),
                color = if (result.points > 0) TrackLime else Smoke
            )
            Text(
                text = result.timeMillis?.let { formatGap(it) } ?: result.status.take(14),
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatGap(millis: Long): String {
    val totalSeconds = millis / 1000.0
    val minutes = (totalSeconds / 60).toInt()
    val seconds = totalSeconds - minutes * 60
    return if (minutes > 0) "%d:%04.1f".format(java.util.Locale.ENGLISH, minutes, seconds)
    else "%.3fs".format(java.util.Locale.ENGLISH, seconds)
}

/* ------------------------------------------------------------------ standings preview */

@Composable
private fun StandingsPreview(
    drivers: List<Driver>,
    seasonLabel: String?,
    onOpenStandings: () -> Unit,
    onDriver: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = PaddockDimens.sectionGap)) {
        SectionHeading(
            kicker = seasonLabel?.let { "$it season" } ?: "Season",
            title = "Championship top three",
            modifier = Modifier.padding(horizontal = PaddockDimens.gutter),
            trailing = {
                Text(
                    text = "ALL",
                    style = MaterialTheme.typography.labelSmall,
                    color = RacingRed,
                    modifier = Modifier
                        .clickable { onOpenStandings() }
                        .padding(4.dp)
                )
            }
        )
        Spacer(Modifier.height(14.dp))
        Column(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
            drivers.forEachIndexed { index, driver ->
                PreviewDriverRow(driver = driver, onOpen = { onDriver(driver.code) })
                if (index != drivers.lastIndex) {
                    TrackDivider(modifier = Modifier.padding(start = 46.dp))
                }
            }
        }
    }
}

@Composable
private fun PreviewDriverRow(driver: Driver, onOpen: () -> Unit) {
    val accent = liveryColor(driver.constructorId, driver.team)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PositionPlate(position = driver.position)
        Spacer(Modifier.width(12.dp))
        TeamAccentBar(constructorId = driver.constructorId, teamName = driver.team)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = driver.name,
                style = MaterialTheme.typography.titleMedium,
                color = Chalk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = driver.team.ifBlank { "—" },
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = driver.points.toString(),
            style = NumericStyle.copy(fontSize = 17.sp),
            color = accent
        )
        Spacer(Modifier.width(4.dp))
        Text(text = "PTS", style = MaterialTheme.typography.labelSmall, color = Smoke)
    }
}

/* ------------------------------------------------------------------ team pulse */

@Composable
private fun TeamPulse(constructors: List<com.somesh.appscout.data.Constructor>) {
    val maxPoints = constructors.maxOfOrNull { it.points }?.coerceAtLeast(1) ?: 1
    Column(modifier = Modifier.padding(top = PaddockDimens.sectionGap)) {
        SectionHeading(
            kicker = "Constructors",
            title = "Team pulse",
            modifier = Modifier.padding(horizontal = PaddockDimens.gutter)
        )
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier.padding(horizontal = PaddockDimens.gutter),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            constructors.forEach { constructor ->
                val accent = liveryColor(constructor.constructorId, constructor.name)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = constructor.position.toString().padStart(2, '0'),
                            style = NumericStyle.copy(fontSize = 13.sp),
                            color = Smoke,
                            modifier = Modifier.width(26.dp)
                        )
                        Text(
                            text = constructor.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = Chalk,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = constructor.points.toString(),
                            style = NumericStyle.copy(fontSize = 14.sp),
                            color = Chalk
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Slate)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(constructor.points.toFloat() / maxPoints)
                                .height(4.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accent, accent.copy(alpha = 0.5f))
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FooterNote(lastUpdated: Long?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PaddockDimens.sectionGap)
            .padding(horizontal = PaddockDimens.gutter)
    ) {
        TrackDivider()
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(TrackLime)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "LIVE FEED · JOLPICA F1 API",
                style = MaterialTheme.typography.labelSmall,
                color = Smoke
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = lastUpdated?.let { "Standings and calendar as published by the season feed." }
                ?: "Awaiting the season feed.",
            style = MaterialTheme.typography.bodySmall,
            color = Smoke
        )
    }
}
