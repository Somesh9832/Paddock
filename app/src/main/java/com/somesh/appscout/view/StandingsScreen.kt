package com.somesh.appscout.view

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somesh.appscout.data.Constructor
import com.somesh.appscout.data.Driver
import com.somesh.appscout.ui.theme.liveryColor
import com.somesh.appscout.state.Championship
import com.somesh.appscout.state.PaddockUiState
import com.somesh.appscout.ui.theme.Asphalt
import com.somesh.appscout.ui.theme.Chalk
import com.somesh.appscout.ui.theme.Graphite
import com.somesh.appscout.ui.theme.NumericStyle
import com.somesh.appscout.ui.theme.RacingRed
import com.somesh.appscout.ui.theme.Slate
import com.somesh.appscout.ui.theme.Smoke

@Composable
fun StandingsScreen(
    state: PaddockUiState,
    pad: PaddingValues,
    onChampionship: (Championship) -> Unit,
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
        item { StandingsHeader(state = state, onChampionship = onChampionship) }

        if (state.errorMessage != null) {
            item {
                ErrorPanel(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = PaddockDimens.gutter, vertical = 8.dp)
                )
            }
        }

        when (state.championship) {
            Championship.DRIVERS -> {
                if (state.drivers.isEmpty()) {
                    item {
                        EmptyPanel(
                            title = "Driver standings unavailable",
                            body = "The championship feed returned no driver classification.",
                            modifier = Modifier.padding(PaddockDimens.gutter)
                        )
                    }
                } else {
                    item { PodiumStrip(drivers = state.podium, onDriver = onDriver) }
                    item {
                        Column(modifier = Modifier.padding(top = PaddockDimens.sectionGap)) {
                            SectionHeading(
                                kicker = state.seasonLabel?.let { "$it championship" } ?: "Championship",
                                title = "Full classification",
                                modifier = Modifier.padding(horizontal = PaddockDimens.gutter)
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    items(items = state.drivers, key = { it.code + it.position }) { driver ->
                        DriverStandingRow(driver = driver, onClick = { onDriver(driver.code) })
                    }
                }
            }

            Championship.CONSTRUCTORS -> {
                if (state.constructors.isEmpty()) {
                    item {
                        EmptyPanel(
                            title = "Constructor standings unavailable",
                            body = "The championship feed returned no constructor classification.",
                            modifier = Modifier.padding(PaddockDimens.gutter)
                        )
                    }
                } else {
                    item { ConstructorLeaderCard(constructor = state.constructorLeader) }
                    item {
                        Column(modifier = Modifier.padding(top = PaddockDimens.sectionGap)) {
                            SectionHeading(
                                kicker = state.seasonLabel?.let { "$it championship" } ?: "Championship",
                                title = "Team classification",
                                modifier = Modifier.padding(horizontal = PaddockDimens.gutter)
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    items(
                        items = state.constructors,
                        key = { (it.constructorId ?: it.name) + it.position }
                    ) { constructor ->
                        ConstructorStandingRow(
                            constructor = constructor,
                            drivers = state.driversFor(constructor.constructorId)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingsHeader(
    state: PaddockUiState,
    onChampionship: (Championship) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddockDimens.gutter)
            .padding(top = 22.dp)
    ) {
        Kicker(text = state.seasonLabel?.let { "Formula 1 · $it" } ?: "Formula 1")
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Standings",
            style = MaterialTheme.typography.displaySmall,
            color = Chalk
        )
        Spacer(Modifier.height(16.dp))
        ChampionshipToggle(selected = state.championship, onSelect = onChampionship)
    }
}

@Composable
private fun ChampionshipToggle(
    selected: Championship,
    onSelect: (Championship) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Graphite)
            .padding(3.dp)
    ) {
        ToggleSegment(
            label = "Drivers",
            selected = selected == Championship.DRIVERS,
            onClick = { onSelect(Championship.DRIVERS) },
            modifier = Modifier.weight(1f)
        )
        ToggleSegment(
            label = "Constructors",
            selected = selected == Championship.CONSTRUCTORS,
            onClick = { onSelect(Championship.CONSTRUCTORS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ToggleSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        targetValue = if (selected) RacingRed else Color.Transparent,
        label = "segmentBackground"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else Smoke,
        label = "segmentText"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(background)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

/* ------------------------------------------------------------------ podium */

@Composable
private fun PodiumStrip(drivers: List<Driver>, onDriver: (String) -> Unit) {
    if (drivers.isEmpty()) return
    Column(modifier = Modifier.padding(top = 22.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddockDimens.gutter),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            drivers.forEach { driver ->
                PodiumTile(
                    driver = driver,
                    onClick = { onDriver(driver.code) },
                    modifier = Modifier.weight(1f)
                )
            }
            repeat(3 - drivers.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PodiumTile(driver: Driver, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = podiumColor(driver.position)
    val teamColor = liveryColor(driver.constructorId, driver.team)
    val height = when (driver.position) {
        1 -> 208.dp
        2 -> 184.dp
        else -> 172.dp
    }
    Column(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(3.dp))
            .background(Graphite)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            DriverPortrait(
                variant = driverVariant(driver.code, driver.name),
                tint = teamColor,
                tintStrength = if (driver.position == 1) 0.26f else 0.44f,
                driverId = driver.driverId,
                modifier = Modifier.fillMaxSize()
            )
            HeroScrim(topAlpha = 0f, bottomAlpha = 0.85f)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .width(24.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driver.position.toString(),
                    style = NumericStyle.copy(fontSize = 12.sp),
                    color = Asphalt
                )
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = driver.familyName.uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = Chalk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = driver.team.ifBlank { "—" },
                style = MaterialTheme.typography.labelSmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = driver.points.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = accent
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "PTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Smoke,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(2.dp)
                    .background(teamColor)
            )
        }
    }
}

/* ------------------------------------------------------------------ rows */

@Composable
private fun DriverStandingRow(driver: Driver, onClick: () -> Unit) {
    val accent = liveryColor(driver.constructorId, driver.team)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (driver.position == 1) accent.copy(alpha = 0.05f) else Color.Transparent)
            .padding(horizontal = PaddockDimens.gutter, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = driver.position.toString().padStart(2, '0'),
            style = NumericStyle.copy(fontSize = 17.sp),
            color = if (driver.position <= 3) podiumColor(driver.position) else Smoke,
            modifier = Modifier.width(34.dp)
        )
        TeamAccentBar(constructorId = driver.constructorId, teamName = driver.team, height = 30.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = driver.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Chalk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (driver.code.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = driver.code,
                        style = MaterialTheme.typography.labelSmall,
                        color = Smoke
                    )
                }
            }
            Text(
                text = buildString {
                    append(driver.team.ifBlank { "—" })
                    if (driver.wins > 0) append("  ·  ${driver.wins} W")
                },
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = driver.points.toString(),
                style = NumericStyle.copy(fontSize = 16.sp),
                color = Chalk
            )
            Text(text = "PTS", style = MaterialTheme.typography.labelSmall, color = Smoke)
        }
    }
    TrackDivider(modifier = Modifier.padding(start = PaddockDimens.gutter + 46.dp))
}

@Composable
private fun ConstructorLeaderCard(constructor: Constructor?) {
    if (constructor == null) return
    val accent = liveryColor(constructor.constructorId, constructor.name)
    Column(modifier = Modifier.padding(top = 22.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PaddockDimens.gutter)
                .height(190.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.linearGradient(
                        listOf(accent.copy(alpha = 0.35f), Graphite)
                    )
                )
        ) {
            DiagonalAccent(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 30.dp),
                color = accent,
                width = 150.dp,
                alpha = 0.22f
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                OutlinedLabel(text = "Constructors' leader", color = accent)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = constructor.name.uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    color = Chalk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = constructor.points.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = accent
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "POINTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Smoke,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (constructor.wins > 0) {
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = "${constructor.wins} WINS",
                            style = MaterialTheme.typography.labelMedium,
                            color = Smoke,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConstructorStandingRow(constructor: Constructor, drivers: List<Driver>) {
    val accent = liveryColor(constructor.constructorId, constructor.name)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddockDimens.gutter, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = constructor.position.toString().padStart(2, '0'),
            style = NumericStyle.copy(fontSize = 17.sp),
            color = if (constructor.position <= 3) podiumColor(constructor.position) else Smoke,
            modifier = Modifier.width(34.dp)
        )
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(34.dp)
                .background(accent)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = constructor.name,
                style = MaterialTheme.typography.titleMedium,
                color = Chalk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val driverLine = if (drivers.isNotEmpty()) {
                drivers.joinToString(" · ") { it.familyName }
            } else {
                constructor.nationality.orEmpty()
            }
            if (driverLine.isNotBlank()) {
                Text(
                    text = driverLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = Smoke,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = constructor.points.toString(),
                style = NumericStyle.copy(fontSize = 16.sp),
                color = Chalk
            )
            Text(
                text = if (constructor.wins > 0) "${constructor.wins} W" else "PTS",
                style = MaterialTheme.typography.labelSmall,
                color = Smoke,
                textAlign = TextAlign.End
            )
        }
    }
    TrackDivider(modifier = Modifier.padding(start = PaddockDimens.gutter + 46.dp))
}
