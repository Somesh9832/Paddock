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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
fun ProfileScreen(
    state: PaddockUiState,
    pad: PaddingValues,
    onChooseDriver: () -> Unit,
    onDriver: (String) -> Unit,
    onClearFavourite: () -> Unit,
    onRetry: () -> Unit
) {
    val favourite = state.favourite()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Asphalt)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = pad.calculateBottomPadding() + 28.dp)
    ) {
        item { ProfileHeader(state) }

        if (state.errorMessage != null) {
            item {
                ErrorPanel(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.padding(horizontal = PaddockDimens.gutter, vertical = 8.dp)
                )
            }
        }

        if (favourite != null) {
            item {
                FavouriteHero(
                    driver = favourite,
                    onOpen = { onDriver(favourite.code) },
                    onClear = onClearFavourite
                )
            }
        } else {
            item {
                EmptyPanel(
                    title = "Choose your driver",
                    body = if (state.drivers.isEmpty()) {
                        "Driver data is not available yet. Once the championship feed responds you can pick a favourite here."
                    } else {
                        "Pick a driver to pin their championship position, points and team to this screen."
                    },
                    modifier = Modifier
                        .padding(horizontal = PaddockDimens.gutter)
                        .padding(top = 22.dp)
                )
            }
        }

        if (state.drivers.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = PaddockDimens.sectionGap)) {
                    SectionHeading(
                        kicker = if (favourite != null) "Change selection" else "Select a driver",
                        title = "The grid",
                        modifier = Modifier.padding(horizontal = PaddockDimens.gutter)
                    )
                    Spacer(Modifier.height(14.dp))
                }
            }
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(gridHeight(state.drivers.size)),
                    contentPadding = PaddingValues(horizontal = PaddockDimens.gutter),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false
                ) {
                    items(items = state.drivers, key = { it.code + it.position }) { driver ->
                        DriverGridTile(
                            driver = driver,
                            selected = driver.code.equals(state.favouriteDriverCode, ignoreCase = true),
                            onClick = { onDriver(driver.code) }
                        )
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
                    text = "Your favourite is stored on this device only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Smoke
                )
            }
        }
    }
}

/** Grid height for a two-column layout; avoids nesting a scrollable inside a scrollable. */
private fun gridHeight(count: Int): androidx.compose.ui.unit.Dp {
    val rows = (count + 1) / 2
    return (rows * 152 + (rows - 1) * 10).dp
}

@Composable
private fun ProfileHeader(state: PaddockUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddockDimens.gutter)
            .padding(top = 22.dp)
    ) {
        Kicker(text = state.seasonLabel?.let { "My paddock · $it" } ?: "My paddock")
        Spacer(Modifier.height(8.dp))
        Text(text = "Profile", style = MaterialTheme.typography.displaySmall, color = Chalk)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Track a driver through the season and jump straight to their championship data.",
            style = MaterialTheme.typography.bodySmall,
            color = Smoke
        )
    }
}

@Composable
private fun FavouriteHero(driver: Driver, onOpen: () -> Unit, onClear: () -> Unit) {
    val teamColor = liveryColor(driver.constructorId, driver.team)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaddockDimens.gutter)
            .padding(top = 22.dp)
            .height(400.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onOpen() }
    ) {
        DriverPortrait(
            variant = driverVariant(driver.code, driver.name),
            tint = teamColor,
            tintStrength = 0.28f,
            driverId = driver.driverId,
            contentDescription = "${driver.name} portrait",
            modifier = Modifier.fillMaxSize()
        )
        HeroScrim(topAlpha = 0.45f, bottomAlpha = 0.98f)
        DiagonalAccent(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp),
            color = teamColor,
            width = 130.dp,
            alpha = 0.2f
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(PaddockDimens.gutter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusPill(text = "My driver")
            Spacer(Modifier.weight(1f))
            Text(
                text = "CLEAR",
                style = MaterialTheme.typography.labelSmall,
                color = Smoke,
                modifier = Modifier
                    .border(1.dp, Slate, RoundedCornerShape(2.dp))
                    .clickable { onClear() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(PaddockDimens.gutter)
        ) {
            Text(
                text = driver.familyName.uppercase(),
                style = MaterialTheme.typography.displayMedium,
                color = Chalk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(3.dp)
                        .background(teamColor)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = driver.team.ifBlank { "—" }.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Chalk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                HeroStat("Championship", "P${driver.position}", podiumColor(driver.position))
                HeroStat("Points", driver.points.toString(), Chalk)
                HeroStat("Wins", driver.wins.toString(), Chalk)
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String, accent: Color) {
    Column {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Smoke)
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
            color = accent
        )
    }
}

@Composable
private fun DriverGridTile(driver: Driver, selected: Boolean, onClick: () -> Unit) {
    val teamColor = liveryColor(driver.constructorId, driver.team)
    Column(
        modifier = Modifier
            .height(152.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Graphite)
            .border(
                1.dp,
                if (selected) RacingRed else Color.Transparent,
                RoundedCornerShape(3.dp)
            )
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
                tintStrength = 0.45f,
                driverId = driver.driverId,
                modifier = Modifier.fillMaxSize()
            )
            HeroScrim(topAlpha = 0f, bottomAlpha = 0.9f)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) RacingRed else Asphalt.copy(alpha = 0.6f))
                    .border(1.dp, if (selected) RacingRed else Slate, RoundedCornerShape(9.dp))
            )
            Text(
                text = "P${driver.position}",
                style = NumericStyle.copy(fontSize = 11.sp),
                color = Smoke,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            )
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
                text = "${driver.points} pts  ·  ${driver.wins} W",
                style = MaterialTheme.typography.labelSmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
