package com.somesh.appscout.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.somesh.appscout.data.RaceResult
import com.somesh.appscout.data.RaceStatus
import com.somesh.appscout.ui.theme.liveryColor
import com.somesh.appscout.ui.theme.Asphalt
import com.somesh.appscout.ui.theme.Chalk
import com.somesh.appscout.ui.theme.Graphite
import com.somesh.appscout.ui.theme.NumericStyle
import com.somesh.appscout.ui.theme.RacingRed
import com.somesh.appscout.ui.theme.Slate
import com.somesh.appscout.ui.theme.Smoke
import com.somesh.appscout.ui.theme.TrackLime

@Composable
fun RaceDetailScreen(
    race: Race?,
    results: List<RaceResult>,
    resultsLoading: Boolean,
    resultsError: String?,
    pad: PaddingValues,
    onBack: () -> Unit,
    onRetryResults: () -> Unit
) {
    if (race == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Asphalt)
                .padding(pad)
                .padding(PaddockDimens.gutter)
                .statusBarsPadding()
        ) {
            DetailBackRow(onBack = onBack, title = "Race")
            Spacer(Modifier.height(24.dp))
            EmptyPanel(
                title = "Race unavailable",
                body = "This round is not part of the current season calendar."
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Asphalt),
        contentPadding = PaddingValues(bottom = pad.calculateBottomPadding() + 32.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                CircuitPlate(modifier = Modifier.fillMaxSize(), accent = RacingRed)

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(PaddockDimens.gutter),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Asphalt.copy(alpha = 0.65f))
                            .border(1.dp, Slate, RoundedCornerShape(2.dp))
                            .clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Chalk,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    StatusPill(
                        text = when (race.status) {
                            RaceStatus.COMPLETED -> "Completed"
                            RaceStatus.NEXT -> "Up next"
                            RaceStatus.UPCOMING -> "Scheduled"
                        },
                        color = if (race.status == RaceStatus.COMPLETED) TrackLime else RacingRed
                    )
                }

                Text(
                    text = race.round.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 132.sp, letterSpacing = (-8).sp),
                    color = Chalk.copy(alpha = 0.12f),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 14.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(PaddockDimens.gutter)
                ) {
                    Text(
                        text = "ROUND ${race.round}",
                        style = MaterialTheme.typography.labelMedium,
                        color = RacingRed
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = race.name.uppercase(),
                        style = MaterialTheme.typography.displaySmall,
                        color = Chalk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = race.longDateLabel.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = Chalk
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = listOfNotNull(
                            race.venue.takeIf { it.isNotBlank() },
                            race.place
                        ).joinToString("  ·  "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Smoke,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            StatStrip(
                stats = listOf(
                    Triple("Round", race.round.toString(), Chalk),
                    Triple("Date", race.dateLabel, Chalk),
                    Triple("Season", race.season ?: "—", Chalk),
                    Triple("Country", countryCode(race.country).ifBlank { "—" }, RacingRed)
                ),
                modifier = Modifier.padding(horizontal = PaddockDimens.gutter)
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                Spacer(Modifier.height(PaddockDimens.sectionGap))
                SectionHeading(kicker = "Circuit", title = "Venue")
                Spacer(Modifier.height(14.dp))
                InfoLine("Circuit", race.venue.ifBlank { "—" })
                InfoLine("Location", race.place.ifBlank { "—" })
                InfoLine("Country", race.country.ifBlank { "—" })
                if (!race.time.isNullOrBlank()) {
                    InfoLine("Race start", "${race.time.removeSuffix("Z")} UTC")
                }
            }
        }

        if (race.sessionDates.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                    Spacer(Modifier.height(PaddockDimens.sectionGap))
                    SectionHeading(kicker = "Weekend", title = "Sessions")
                    Spacer(Modifier.height(12.dp))
                    race.sessionDates.forEach { (name, stamp) ->
                        SessionRow(name = name, stamp = stamp, isRace = name == "Race")
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                Spacer(Modifier.height(PaddockDimens.sectionGap))
                SectionHeading(
                    kicker = "Classification",
                    title = if (results.isEmpty()) "Race result" else "Final order"
                )
                Spacer(Modifier.height(14.dp))
            }
        }

        when {
            resultsLoading -> item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RacingRed, strokeWidth = 2.dp)
                }
            }

            resultsError != null -> item {
                Box(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                    ErrorPanel(message = resultsError, onRetry = onRetryResults)
                }
            }

            results.isEmpty() -> item {
                Box(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                    EmptyPanel(
                        title = "No classification yet",
                        body = "The timing feed has not published a result for this round.",
                        accent = Slate
                    )
                }
            }

            else -> item {
                Column(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                    results.forEach { result ->
                        ClassificationRow(result = result)
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                Spacer(Modifier.height(PaddockDimens.sectionGap))
                TrackDivider()
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Circuit, session and classification data are published by the championship feed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Smoke
                )
            }
        }
    }
}

@Composable
private fun DetailBackRow(onBack: () -> Unit, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Graphite)
                .border(1.dp, Slate, RoundedCornerShape(2.dp))
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Chalk,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = Chalk)
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Smoke,
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Chalk,
            modifier = Modifier.weight(1f)
        )
    }
    TrackDivider()
}

@Composable
private fun SessionRow(name: String, stamp: String?, isRace: Boolean) {
    val date = stamp?.substringBefore("T") ?: "TBC"
    val time = stamp?.substringAfter("T", "")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(30.dp)
                .background(if (isRace) RacingRed else Slate)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = name.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (isRace) RacingRed else Chalk,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodySmall,
            color = Smoke
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = if (time.isNullOrBlank()) "—" else time.take(5),
            style = NumericStyle.copy(fontSize = 13.sp),
            color = Chalk
        )
    }
    TrackDivider()
}

@Composable
private fun ClassificationRow(result: RaceResult) {
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
            style = NumericStyle.copy(fontSize = 17.sp),
            color = podiumColor(result.position),
            modifier = Modifier.width(36.dp)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(28.dp)
                .background(accent)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = Chalk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(result.constructorName.ifBlank { "—" })
                    result.grid?.let { append("  ·  from P$it") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = result.timeMillis?.let { formatLapGap(it) } ?: result.status.take(16),
                style = NumericStyle.copy(fontSize = 13.sp),
                color = Chalk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (result.points > 0) "+${formatPoints(result.points)} pts" else "—",
                style = MaterialTheme.typography.labelSmall,
                color = if (result.points > 0) TrackLime else Smoke
            )
        }
    }
    TrackDivider()
}

private fun formatLapGap(millis: Long): String {
    val totalSeconds = millis / 1000.0
    val minutes = (totalSeconds / 60).toInt()
    val seconds = totalSeconds - minutes * 60
    return if (minutes > 0) "%d:%04.1f".format(java.util.Locale.ENGLISH, minutes, seconds)
    else "%.3fs".format(java.util.Locale.ENGLISH, seconds)
}
