package com.somesh.appscout.view

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Bundled driver photography.
 *
 * The Ergast/Jolpica payload carries no image URL, so portraits ship with the app in
 * `res/drawable-nodpi/` and are resolved by the driver's *stable* identifier
 * (`driverId`, for example `max_verstappen`) rather than by display name or standings
 * position. A driver with no bundled asset simply resolves to 0 and the caller keeps the
 * existing editorial plate, so a missing photograph can never break a layout.
 */

private const val PORTRAIT_PREFIX = "paddock_driver_"

/**
 * Accepted spellings for the same asset, so a file dropped in under a friendlier name is still
 * picked up. The canonical key is always the Jolpica driverId.
 */
private val PORTRAIT_ALIASES = mapOf(
    "verstappen" to "max_verstappen",
    "maxverstappen" to "max_verstappen",
    "kimi_antonelli" to "antonelli",
    "antonelli_kimi" to "antonelli",
    "c_leclerc" to "leclerc",
    "l_hamilton" to "hamilton",
    "g_russell" to "russell",
    "k_antonelli" to "antonelli"
)

private val portraitIdCache = ConcurrentHashMap<String, Int>()

/**
 * Drawable name for a driver, or null when the id is unusable. Pure and side-effect free so the
 * mapping can be unit tested without an Android runtime.
 */
internal fun portraitAssetName(driverId: String?): String? {
    val key = driverId?.trim()?.lowercase(Locale.ENGLISH).orEmpty()
    if (key.isEmpty()) return null
    return PORTRAIT_PREFIX + (PORTRAIT_ALIASES[key] ?: key)
}

/**
 * Resource id of the bundled portrait for [driverId], or 0 when none is packaged.
 *
 * Uses a name lookup because the assets are optional: dropping
 * `paddock_driver_<driverId>.jpg` into `res/drawable-nodpi/` is the entire integration step.
 * Results are memoised because `getIdentifier` is a comparatively expensive string lookup.
 */
@Suppress("DiscouragedApi") // Deliberate: assets are resolved by stable driverId at runtime.
fun driverPortraitResId(resources: Resources, packageName: String, driverId: String?): Int {
    val name = portraitAssetName(driverId) ?: return 0
    portraitIdCache[name]?.let { return it }
    val resId = resources.getIdentifier(name, "drawable", packageName)
    portraitIdCache[name] = resId
    return resId
}

/**
 * Decoded portraits, bounded by bytes.
 *
 * Entries are keyed by resource id *and* subsample factor, so every call site that needs the
 * same level of detail shares one bitmap instead of decoding its own copy.
 */
private object PaddockImageCache {

    private val maxBytes: Int =
        (Runtime.getRuntime().maxMemory() / 8).coerceAtMost(24L * 1024 * 1024).toInt()

    private val bitmaps = object : LruCache<String, ImageBitmap>(maxBytes) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = value.width * value.height * 4
    }

    /** Source pixel dimensions per resource id, read once from the bitmap header. */
    private val sourceSizes = ConcurrentHashMap<Int, IntSize>()

    /** Cached bitmap for this exact request, without touching disk. */
    fun peek(resources: Resources, resId: Int, target: IntSize): ImageBitmap? {
        val source = sourceSizes[resId] ?: return null
        return bitmaps.get(cacheKey(resId, sampleSize(source, target)))
    }

    /** Decodes off the main thread, downsampling only as far as quality allows. */
    suspend fun load(resources: Resources, resId: Int, target: IntSize): ImageBitmap? =
        withContext(Dispatchers.IO) {
            val source = sourceSizes[resId]
                ?: readSourceSize(resources, resId).also { sourceSizes[resId] = it }
            if (source.width <= 0 || source.height <= 0) return@withContext null

            val key = cacheKey(resId, sampleSize(source, target))
            bitmaps.get(key)?.let { return@withContext it }

            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(source, target)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = BitmapFactory.decodeResource(resources, resId, options)
                ?: return@withContext null
            decoded.asImageBitmap().also { bitmaps.put(key, it) }
        }

    private fun readSourceSize(resources: Resources, resId: Int): IntSize {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(resources, resId, options)
        return IntSize(options.outWidth, options.outHeight)
    }

    /**
     * Largest power-of-two subsample that still leaves at least the requested pixel size.
     * The decoded bitmap is therefore never smaller than the slot it fills, so the optimisation
     * removes memory and GPU work without softening the portrait.
     */
    private fun sampleSize(source: IntSize, target: IntSize): Int {
        if (target.width <= 0 || target.height <= 0) return 1
        var sample = 1
        while (source.width / (sample * 2) >= target.width &&
            source.height / (sample * 2) >= target.height
        ) {
            sample *= 2
        }
        return sample
    }

    private fun cacheKey(resId: Int, sample: Int) = "$resId@$sample"
}

/**
 * Loads a bundled portrait asynchronously for the measured [target] size.
 *
 * Returns the cached bitmap immediately when one exists, otherwise null until the background
 * decode completes. Never blocks the main thread and never repeats a download or decode.
 */
@Composable
fun rememberPaddockBitmap(resId: Int, target: IntSize?): ImageBitmap? {
    val resources = LocalResources.current
    return produceState<ImageBitmap?>(
        initialValue = target?.let { PaddockImageCache.peek(resources, resId, it) },
        resId,
        target
    ) {
        if (resId == 0 || target == null) return@produceState
        PaddockImageCache.load(resources, resId, target)?.let { value = it }
    }.value
}
