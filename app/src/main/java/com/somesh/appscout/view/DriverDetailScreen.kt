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
import androidx.compose.material3.Icon
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
import com.somesh.appscout.ui.theme.liveryColor
import com.somesh.appscout.state.PaddockUiState
import com.somesh.appscout.ui.theme.Asphalt
import com.somesh.appscout.ui.theme.Chalk
import com.somesh.appscout.ui.theme.Graphite
import com.somesh.appscout.ui.theme.NumericStyle
import com.somesh.appscout.ui.theme.RacingRed
import com.somesh.appscout.ui.theme.Slate
import com.somesh.appscout.ui.theme.Smoke

@Composable
fun DriverDetailScreen(
    driver: Driver?,
    state: PaddockUiState,
    isFavourite: Boolean,
    pad: PaddingValues,
    onFavourite: (String) -> Unit,
    onBack: () -> Unit
) {
    if (driver == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Asphalt)
                .padding(pad)
                .padding(PaddockDimens.gutter)
                .statusBarsPadding()
        ) {
            BackRow(onBack = onBack, title = "Driver")
            Spacer(Modifier.height(24.dp))
            EmptyPanel(
                title = "Driver unavailable",
                body = "This driver is not part of the current championship classification."
            )
        }
        return
    }

    val teamColor = liveryColor(driver.constructorId, driver.team)
    val teammates = state.driversFor(driver.constructorId).filter { it.code != driver.code }
    val gapToLeader = state.leader?.let { it.points - driver.points }?.takeIf { it > 0 }

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
                    .height(430.dp)
            ) {
                DriverPortrait(
                    variant = driverVariant(driver.code, driver.name),
                    tint = teamColor,
                    tintStrength = 0.30f,
                    driverId = driver.driverId,
                    contentDescription = "${driver.name} portrait",
                    modifier = Modifier.fillMaxSize()
                )
                HeroScrim(topAlpha = 0.5f, bottomAlpha = 0.98f)
                DiagonalAccent(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 18.dp),
                    color = teamColor,
                    width = 130.dp,
                    alpha = 0.22f
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(PaddockDimens.gutter)
                        .fillMaxWidth(),
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
                    FavouriteButton(isFavourite = isFavourite) { onFavourite(driver.code) }
                }

                // Oversized racing number anchored to the plate
                Text(
                    text = (driver.permanentNumber ?: driver.number.toString().takeIf { it != "0" } ?: "—"),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 128.sp, letterSpacing = (-6).sp),
                    color = Chalk.copy(alpha = 0.10f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(PaddockDimens.gutter)
                ) {
                    OutlinedLabel(
                        text = "P${driver.position}  ·  ${driver.points} PTS",
                        color = teamColor
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = driver.name.split(" ").dropLast(1).joinToString(" ").uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Smoke,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = driver.familyName.uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Chalk,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(3.dp)
                                .background(teamColor)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = driver.team.ifBlank { "Constructor unavailable" }.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Chalk
                        )
                    }
                }
            }
        }

        item {
            StatStrip(
                stats = listOf(
                    Triple("Position", "P${driver.position}", podiumColor(driver.position)),
                    Triple("Points", driver.points.toString(), Chalk),
                    Triple("Wins", driver.wins.toString(), Chalk),
                    Triple("No.", (driver.permanentNumber ?: driver.number.toString().takeIf { it != "0" } ?: "—"), teamColor)
                ),
                modifier = Modifier.padding(horizontal = PaddockDimens.gutter)
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                Spacer(Modifier.height(PaddockDimens.sectionGap))
                SectionHeading(kicker = "Profile", title = "Driver identity")
                Spacer(Modifier.height(16.dp))
                InfoRow("Nationality", driver.country.ifBlank { "—" })
                InfoRow("Code", driver.code.ifBlank { "—" })
                InfoRow("Date of birth", driver.dateOfBirth?.let { formatDob(it) } ?: "—")
                InfoRow(
                    "Gap to leader",
                    gapToLeader?.let { "$it pts" } ?: if (driver.position == 1) "Championship leader" else "—"
                )
                if (driver.bio.isNotBlank()) {
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = driver.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Smoke
                    )
                }
            }
        }

        if (teammates.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = PaddockDimens.gutter)) {
                    Spacer(Modifier.height(PaddockDimens.sectionGap))
                    SectionHeading(kicker = "Constructor", title = "Team-mates")
                    Spacer(Modifier.height(12.dp))
                    teammates.forEach { mate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Graphite)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TeamAccentBar(
                                constructorId = mate.constructorId,
                                teamName = mate.team,
                                height = 30.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = mate.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Chalk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "P${mate.position}  ·  ${mate.wins} wins",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Smoke
                                )
                            }
                            Text(
                                text = mate.points.toString(),
                                style = NumericStyle.copy(fontSize = 16.sp),
                                color = Chalk
                            )
                        }
                        Spacer(Modifier.height(8.dp))
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
                    text = "Statistics are the championship figures published by the season feed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Smoke
                )
            }
        }
    }
}

@Composable
private fun BackRow(onBack: () -> Unit, title: String) {
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
private fun FavouriteButton(isFavourite: Boolean, onClick: () -> Unit) {
    val accent = if (isFavourite) RacingRed else Smoke
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Asphalt.copy(alpha = 0.65f))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isFavourite) RacingRed else Color.Transparent)
                .border(1.dp, accent, RoundedCornerShape(4.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (isFavourite) "FAVOURITE" else "SET FAVOURITE",
            style = MaterialTheme.typography.labelSmall,
            color = if (isFavourite) Chalk else Smoke
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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
            modifier = Modifier.width(120.dp)
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

private fun formatDob(raw: String): String = runCatching {
    java.time.LocalDate.parse(raw)
        .format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ENGLISH))
}.getOrDefault(raw)
