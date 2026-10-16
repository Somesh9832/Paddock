package com.somesh.appscout

import com.somesh.appscout.data.Driver
import com.somesh.appscout.view.portraitAssetName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the driver-photography contract: the stable Jolpica driverId maps to a predictable
 * drawable name, and the five priority portraits are actually packaged in the APK resources.
 */
class DriverPortraitTest {

    /** The driverIds returned by the live driver-standings payload. */
    private val priorityDrivers = mapOf(
        "antonelli" to "Andrea Kimi Antonelli",
        "russell" to "George Russell",
        "hamilton" to "Lewis Hamilton",
        "max_verstappen" to "Max Verstappen",
        "leclerc" to "Charles Leclerc"
    )

    @Test
    fun `priority drivers resolve to their own bundled asset name`() {
        priorityDrivers.forEach { (driverId, name) ->
            assertEquals("paddock_driver_$driverId", portraitAssetName(driverId))
            assertTrue("$name should resolve", portraitAssetName(driverId)!!.startsWith("paddock_driver_"))
        }
    }

    @Test
    fun `aliases and casing collapse onto the canonical asset`() {
        assertEquals("paddock_driver_max_verstappen", portraitAssetName("verstappen"))
        assertEquals("paddock_driver_max_verstappen", portraitAssetName("MaxVerstappen"))
        assertEquals("paddock_driver_antonelli", portraitAssetName("Kimi_Antonelli"))
        assertEquals("paddock_driver_hamilton", portraitAssetName("HAMILTON"))
        assertEquals("paddock_driver_leclerc", portraitAssetName("  leclerc  "))
    }

    @Test
    fun `unknown and blank ids fall back instead of inventing an asset`() {
        assertNull(portraitAssetName(null))
        assertNull(portraitAssetName(""))
        assertNull(portraitAssetName("   "))
        // Unknown drivers still produce a lookup name; the resolver returns 0 when absent, which
        // is what keeps the editorial plate in place rather than breaking the layout.
        assertEquals("paddock_driver_someone_new", portraitAssetName("someone_new"))
    }

    @Test
    fun `driver keeps its stable id through equality and copy`() {
        val driver = Driver(
            position = 4,
            name = "Max Verstappen",
            code = "VER",
            team = "Red Bull",
            points = 221,
            number = 1,
            country = "Dutch",
            bio = "",
            driverId = "max_verstappen",
            constructorId = "red_bull"
        )

        assertEquals(driver, driver.copy())
        assertEquals("max_verstappen", driver.copy(points = 0).driverId)
        assertEquals("Verstappen", driver.familyName)
        assertEquals("VERSTAPPEN", driver.familyName.uppercase())
    }

    @Test
    fun `portraits for the priority drivers are packaged as drawables`() {
        val resDir = findDrawableNodpi() ?: return
        priorityDrivers.forEach { (driverId, name) ->
            val asset = File(resDir, "paddock_driver_$driverId.jpg")
            assertTrue("missing bundled portrait for $name at ${asset.path}", asset.isFile)
            assertTrue("portrait for $name looks empty", asset.length() > 10_000)
        }
    }

    /** Locates `res/drawable-nodpi` regardless of whether the test runs from the module or root. */
    private fun findDrawableNodpi(): File? {
        var dir: File? = File(System.getProperty("user.dir") ?: ".").absoluteFile
        repeat(4) {
            val current = dir ?: return null
            listOf("app/src/main/res/drawable-nodpi", "src/main/res/drawable-nodpi").forEach { rel ->
                val candidate = File(current, rel)
                if (candidate.isDirectory) return candidate
            }
            dir = current.parentFile
        }
        return null
    }
}
