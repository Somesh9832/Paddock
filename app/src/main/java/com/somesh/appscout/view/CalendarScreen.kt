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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somesh.appscout.data.Race
import com.somesh.appscout.data.RaceStatus
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
fun CalendarScreen(
    state: PaddockUiState,
    pad: PaddingValues,
    onRace: (Int) -> Unit,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Asphalt)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = pad.calculateBottomPadding() + 28.dp)
    ) {
        item { CalendarHeader(state) }

        if (state.errorMessage != null) {
            item {
                ErrorPanel(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = PaddockDimens.gutter, vertical = 8.dp)
                )
            }
        }

        if (state.races.isEmpty()) {
            item {
                EmptyPanel(
                    title = "Calendar unavailable",
                    body = "The season schedule could not be read from the feed.",
                    modifier = Modifier.padding(PaddockDimens.gutter)
                )
            }
            return@LazyColumn
        }

        state.nextRace?.let { next ->
            item { NextRaceBanner(race = next, onClick = { onRace(next.round) }) }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = PaddockDimens.gutter, end = PaddockDimens.gutter, top = PaddockDimens.sectionGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SEASON CALENDAR",
                    style = MaterialTheme.typography.labelMedium,
                    color = RacingRed
                )
                Spacer(Modifier.width(12.dp))
                TrackDivider(modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${state.races.size} ROUNDS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Smoke
                )
            }
        }

        items(items = state.races, key = { it.round }) { race ->
            CalendarRow(
                race = race,
                isNext = state.nextRace?.round == race.round,
                onClick = { onRace(race.round) }
            )
        }

        item {
            Column(modifier = Modifier.padding(PaddockDimens.gutter)) {
                TrackDivider()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Sessions and classifications are published by the championship feed. Times are UTC unless stated.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Smoke
                )
            }
        }
    }
}

@Composable
private fun CalendarHeader(state: PaddockUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddockDimens.gutter)
            .padding(top = 22.dp)
    ) {
        Kicker(text = state.seasonLabel?.let { "Formula 1 · $it season" } ?: "Formula 1")
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Calendar",
            style = MaterialTheme.typography.displaySmall,
            color = Chalk
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Every round of the season, from the season opener to the final flag.",
            style = MaterialTheme.typography.bodySmall,
            color = Smoke
        )
    }
}

@Composable
private fun NextRaceBanner(race: Race, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddockDimens.gutter)
            .padding(top = 20.dp)
            .height(210.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
    ) {
        CircuitPlate(modifier = Modifier.fillMaxSize(), accent = RacingRed)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp)
        ) {
            StatusPill(text = "Up next")
            Spacer(Modifier.height(10.dp))
            Text(
                text = race.shortName.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = Chalk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "ROUND ${race.round}  ·  ${race.longDateLabel.uppercase()}",
                style = MaterialTheme.typography.labelSmall,
                color = RacingRed
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = listOfNotNull(race.venue.takeIf { it.isNotBlank() }, race.place)
                    .joinToString("  ·  "),
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CalendarRow(race: Race, isNext: Boolean, onClick: () -> Unit) {
    val completed = race.status == RaceStatus.COMPLETED
    val accent = if (isNext) RacingRed else if (completed) TrackLime else Slate

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isNext) RacingRed.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = PaddockDimens.gutter)
    ) {
        // Timeline gutter
        Column(
            modifier = Modifier.width(46.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(14.dp)
                    .background(Slate)
            )
            Box(
                modifier = Modifier
                    .width(if (isNext) 10.dp else 7.dp)
                    .height(if (isNext) 10.dp else 7.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(accent)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(74.dp)
                    .background(Slate)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "R${race.round.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isNext) RacingRed else Smoke
                )
                Spacer(Modifier.width(10.dp))
                if (isNext) {
                    StatusPill(text = "Next")
                } else if (completed) {
                    Text(
                        text = "COMPLETED",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrackLime
                    )
                } else {
                    OutlinedLabel(text = "Scheduled")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = race.shortName,
                style = MaterialTheme.typography.headlineSmall,
                color = Chalk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = race.venue.ifBlank { race.place },
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CountryBadge(country = race.country)
                Text(
                    text = race.place,
                    style = MaterialTheme.typography.labelSmall,
                    color = Smoke,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier
                .width(62.dp)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = race.dayLabel,
                style = MaterialTheme.typography.headlineMedium,
                color = if (completed) Smoke else Chalk
            )
            Text(
                text = race.monthLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (isNext) RacingRed else Smoke
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = race.weekdayLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Smoke
            )
        }
    }
}
