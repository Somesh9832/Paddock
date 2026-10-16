package com.somesh.appscout.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.somesh.appscout.R
import com.somesh.appscout.ui.theme.liveryColor
import com.somesh.appscout.ui.theme.Asphalt
import com.somesh.appscout.ui.theme.Chalk
import com.somesh.appscout.ui.theme.Graphite
import com.somesh.appscout.ui.theme.NumericStyle
import com.somesh.appscout.ui.theme.RacingRed
import com.somesh.appscout.ui.theme.Slate
import com.somesh.appscout.ui.theme.Smoke
import java.util.Locale

/* Layout primitives */

object PaddockDimens {
    val gutter = 20.dp
    val sectionGap = 34.dp
    val heroHeight = 320.dp
}

/** Wide-tracked uppercase eyebrow used above every section title. */
@Composable
fun Kicker(text: String, color: Color = RacingRed, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier
    )
}

/** Section heading with a red rule that anchors the editorial rhythm. */
@Composable
fun SectionHeading(
    kicker: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(3.dp)
                    .background(RacingRed)
            )
            Spacer(Modifier.width(8.dp))
            Kicker(kicker)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Chalk,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                trailing()
            }
        }
    }
}

/** Thin track-style divider. */
@Composable
fun TrackDivider(modifier: Modifier = Modifier, color: Color = Slate) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

/**
 * Diagonal "speed" band used sparingly behind hero content so panels read as engineered
 * shapes rather than plain rectangles.
 */
@Composable
fun DiagonalAccent(
    modifier: Modifier = Modifier,
    color: Color = RacingRed,
    width: Dp = 120.dp,
    alpha: Float = 0.18f
) {
    Box(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .graphicsLayer {
                rotationZ = -18f
                transformOrigin = TransformOrigin.Center
            }
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = alpha), color.copy(alpha = 0f))
                )
            )
    )
}

/* Team identity */

@Composable
fun TeamAccentBar(
    constructorId: String?,
    teamName: String? = null,
    modifier: Modifier = Modifier,
    width: Dp = 3.dp,
    height: Dp = 34.dp
) {
    val color = liveryColor(constructorId, teamName)
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(
                Brush.verticalGradient(listOf(color, color.copy(alpha = 0.35f)))
            )
    )
}

@Composable
fun TeamChip(
    constructorId: String?,
    teamName: String,
    modifier: Modifier = Modifier
) {
    if (teamName.isBlank()) return
    val color = liveryColor(constructorId, teamName)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(2.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = teamName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Chalk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/* Imagery */

/**
 * Anchor used when cropping a real portrait.
 *
 * Photographs sit slightly above centre so the face stays inside the short, wide tiles used by
 * the driver grid. Tall heroes are effectively unaffected (they show almost the whole frame),
 * so this only changes how a photograph is framed, never the box it is drawn into.
 */
private val PortraitAlignment: Alignment = BiasAlignment(horizontalBias = 0.5f, verticalBias = 0.32f)

/**
 * Shared editorial portrait.
 *
 * When a bundled photograph exists for [driverId] it is drawn instead of the fallback plate.
 * The fallback keeps its original treatment: `variant` flips and nudges the plate so a grid of
 * drivers does not read as an obvious duplicate, and `tint` ties each instance to its team
 * livery. Sizing, overlays and the livery tint are identical on both paths, so swapping in a
 * photograph cannot shift the layout.
 */
@Composable
fun DriverPortrait(
    variant: Int,
    modifier: Modifier = Modifier,
    tint: Color = RacingRed,
    tintStrength: Float = 0.34f,
    driverId: String? = null,
    contentDescription: String? = null
) {
    val resources = LocalResources.current
    val packageName = LocalContext.current.packageName
    val portraitRes = remember(resources, packageName, driverId) {
        driverPortraitResId(resources, packageName, driverId)
    }
    var measured by remember { mutableStateOf<IntSize?>(null) }

    Box(modifier = modifier.onSizeChanged { measured = it }) {
        if (portraitRes != 0) {
            val bitmap = rememberPaddockBitmap(portraitRes, measured)
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    alignment = PortraitAlignment,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                )
            }
        } else {
            val flip = variant % 2 != 0
            val scale = 1f + ((variant % 4) * 0.03f)
            Image(
                painter = painterResource(R.drawable.paddock_driver_editorial),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = if (flip) -scale else scale
                        scaleY = scale
                    }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(tint.copy(alpha = tintStrength))
        )
    }
}

/** Bottom-up scrim so headline text stays legible over photography. */
@Composable
fun HeroScrim(
    modifier: Modifier = Modifier,
    topAlpha: Float = 0.10f,
    bottomAlpha: Float = 0.98f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                Brush.verticalGradient(
                    0f to Asphalt.copy(alpha = topAlpha),
                    0.45f to Asphalt.copy(alpha = 0.55f),
                    1f to Asphalt.copy(alpha = bottomAlpha)
                )
            )
    )
}

/** Generic circuit hero plate used by calendar and race detail screens. */
@Composable
fun CircuitPlate(
    modifier: Modifier = Modifier,
    accent: Color = RacingRed,
    content: @Composable () -> Unit = {}
) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.paddock_hero_circuit),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(accent.copy(alpha = 0.22f))
        )
        HeroScrim(topAlpha = 0.35f, bottomAlpha = 0.95f)
        content()
    }
}

/* Statistics */

@Composable
fun StatBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = Chalk,
    caption: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Smoke,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 26.sp),
            color = accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (caption != null) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = Smoke,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatStrip(
    stats: List<Triple<String, String, Color>>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Graphite)
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        stats.forEachIndexed { index, stat ->
            StatBlock(
                label = stat.first,
                value = stat.second,
                accent = stat.third,
                modifier = Modifier.weight(1f)
            )
            if (index != stats.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(38.dp)
                        .background(Slate)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

fun podiumColor(position: Int): Color = when (position) {
    1 -> com.somesh.appscout.ui.theme.PodiumGold
    2 -> com.somesh.appscout.ui.theme.Silver
    3 -> com.somesh.appscout.ui.theme.Bronze
    else -> RacingRed
}

/** Position badge rendered as a squared plate, gold/silver/bronze for the podium. */
@Composable
fun PositionPlate(position: Int, modifier: Modifier = Modifier, compact: Boolean = false) {
    val accent = podiumColor(position)
    val onPodium = position in 1..3
    Box(
        modifier = modifier
            .width(if (compact) 28.dp else 38.dp)
            .height(if (compact) 28.dp else 38.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(if (onPodium) accent.copy(alpha = 0.16f) else Graphite)
            .border(
                1.dp,
                if (onPodium) accent.copy(alpha = 0.55f) else Slate,
                RoundedCornerShape(3.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = position.toString(),
            style = NumericStyle.copy(fontSize = if (compact) 12.sp else 15.sp),
            color = if (onPodium) accent else Smoke
        )
    }
}

/* States */

@Composable
fun ErrorPanel(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Graphite)
            .border(1.dp, RacingRed.copy(alpha = 0.4f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(36.dp)
                .background(RacingRed)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = Chalk,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "RETRY",
            style = MaterialTheme.typography.labelMedium,
            color = RacingRed,
            modifier = Modifier
                .clickable { onRetry() }
                .padding(horizontal = 6.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun EmptyPanel(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    accent: Color = RacingRed,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Graphite)
            .padding(20.dp)
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(3.dp)
                .background(accent)
        )
        Spacer(Modifier.height(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = Chalk)
        Spacer(Modifier.height(6.dp))
        Text(text = body, style = MaterialTheme.typography.bodySmall, color = Smoke)
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

/** Small red-on-black pill used for live/next markers. */
@Composable
fun StatusPill(text: String, modifier: Modifier = Modifier, color: Color = RacingRed) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
fun OutlinedLabel(text: String, modifier: Modifier = Modifier, color: Color = Smoke) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
            .border(1.dp, Slate, RoundedCornerShape(2.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

/* Formatting helpers */

fun formatPoints(points: Double): String =
    if (points % 1.0 == 0.0) points.toInt().toString()
    else String.format(Locale.ENGLISH, "%.1f", points)

fun driverVariant(code: String?, name: String?): Int {
    val seed = code?.takeIf { it.isNotBlank() } ?: name.orEmpty()
    return (seed.hashCode() and 0x7FFFFFFF) % 7
}

/** Country / nationality -> ISO-2 style code for the small flag badge. */
fun countryCode(country: String?): String {
    val c = country?.trim().orEmpty()
    if (c.isEmpty()) return ""
    return when (c.lowercase()) {
        "united kingdom", "uk", "great britain", "british" -> "GB"
        "united states", "usa", "united states of america", "american" -> "US"
        "netherlands", "dutch" -> "NL"
        "germany", "german" -> "DE"
        "spain", "spanish" -> "ES"
        "france", "french" -> "FR"
        "italy", "italian" -> "IT"
        "monaco", "monegasque", "monégasque" -> "MC"
        "mexico", "mexican" -> "MX"
        "brazil", "brazilian" -> "BR"
        "canada", "canadian" -> "CA"
        "australia", "australian" -> "AU"
        "japan", "japanese" -> "JP"
        "china", "chinese" -> "CN"
        "austria", "austrian" -> "AT"
        "belgium", "belgian" -> "BE"
        "switzerland", "swiss" -> "CH"
        "sweden", "swedish" -> "SE"
        "finland", "finnish" -> "FI"
        "denmark", "danish" -> "DK"
        "thailand", "thai" -> "TH"
        "new zealand", "new zealander" -> "NZ"
        "argentina", "argentinian", "argentine" -> "AR"
        "singapore", "singaporean" -> "SG"
        "azerbaijan", "azerbaijani" -> "AZ"
        "saudi arabia", "saudi" -> "SA"
        "united arab emirates" -> "AE"
        "south africa", "south african" -> "ZA"
        "poland", "polish" -> "PL"
        "indonesia", "indonesian" -> "ID"
        else -> c.take(2).uppercase(Locale.ENGLISH)
    }
}

@Composable
fun CountryBadge(country: String?, modifier: Modifier = Modifier) {
    val code = countryCode(country)
    if (code.isBlank()) return
    Text(
        text = code,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
        color = Smoke,
        modifier = modifier
            .border(1.dp, Slate, RoundedCornerShape(2.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
    )
}

@Composable
fun RotatedWatermark(text: String, modifier: Modifier = Modifier, color: Color = Slate) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier.rotate(-90f)
    )
}
