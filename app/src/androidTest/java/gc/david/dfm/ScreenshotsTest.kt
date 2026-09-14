/*
 * Copyright (c) 2026 David Aguiar Gonzalez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dfm

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import gc.david.dfm.main.presentation.MainActivity
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(AndroidJUnit4::class)
class ScreenshotsTest {

    companion object {
        @ClassRule
        @JvmField
        val localeTestRule = LocaleTestRule()

        @BeforeClass
        @JvmStatic
        fun beforeAll() {
            Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())

            // Disable the "Show IME with hard keyboard" setting to avoid the floating shortcut bar
            InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(
                "settings put secure show_ime_with_hard_keyboard 0"
            )
        }
    }

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    //    @Test
    fun takeScreenshots() {
        ActivityScenario.launch(MainActivity::class.java).use {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

            // 1. Wait for map tiles to load
            Thread.sleep(7000)
            Screengrab.screenshot("01_MainScreen")

            // 2. Open Menu
            composeTestRule.onNodeWithTag("toolbar_open_menu").performClick()
            Thread.sleep(1000)
            Screengrab.screenshot("02_Menu")

            // 3. Open About Screen
            composeTestRule.onNodeWithTag("navigation_rail_menu_about").performClick()
            Thread.sleep(2000)
            Screengrab.screenshot("03_About")
            device.pressBack()
            Thread.sleep(1000)

            // 4. Open FAQ Screen
            composeTestRule.onNodeWithTag("toolbar_open_menu").performClick()
            Thread.sleep(1000)
            composeTestRule.onNodeWithTag("navigation_rail_menu_faq").performClick()
            Thread.sleep(2000)
            Screengrab.screenshot("04_FAQ")
            device.pressBack()
            Thread.sleep(1000)
        }
    }

    @Test
    fun takeSearchAndSaveScreenshots() {
        ActivityScenario.launch(MainActivity::class.java).use {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

            // Wait for map tiles to load
            Thread.sleep(10000)

            // 1. Tap the loupe icon to search a location
            composeTestRule.onNodeWithTag("toolbar_search_button").performClick()
            Thread.sleep(1000)

            // 2. Type "Morro Jable"
            composeTestRule.onNodeWithTag("toolbar_search_text_field")
                .performTextInput("Morro Jable")
            Thread.sleep(1000)

            // 3. Take a screenshot
            Screengrab.screenshot("05_Search_FTV")

            // 4. Input ENTER to locate the place
            composeTestRule.onNodeWithTag("toolbar_search_text_field").performImeAction()

            // Handle ambiguous results (dialog)
            Thread.sleep(2000)
            val suggestions = composeTestRule.onAllNodesWithTag("address_suggestion_item")
            if (suggestions.fetchSemanticsNodes().isNotEmpty()) {
                suggestions[0].performClick()
            }
            Thread.sleep(5000)

            // Uncomment when query limit is reset
            // 5. Tap on the show chart FloatingActionButton, verify that the elevation chart is shown and Take a screenshot.
            // Wait for map to settle and elevation to load (using waitUntil for robustness)
//            composeTestRule.waitUntil(timeoutMillis = 20000) {
//                composeTestRule.onAllNodesWithTag("fab_show_chart").fetchSemanticsNodes()
//                    .isNotEmpty()
//            }
//
//            composeTestRule.onNodeWithTag("fab_show_chart").performClick()
//            Thread.sleep(1000)
//            composeTestRule.onNodeWithTag("elevation_chart").assertIsDisplayed()
//            Screengrab.screenshot("06_ElevationChart")

            // 6. Tap on the marker info window to open the details screen
            // Info window is a native view. We search for standard distance units.
            val infoWindow =
                device.findObject(UiSelector().descriptionContains("km")).takeIf { it.exists() }
                    ?: device.findObject(UiSelector().descriptionContains("mi")).takeIf { it.exists() }

            if (infoWindow != null) {
                val rect = infoWindow.bounds
                // To avoid clicking the marker itself (which centers the map), 
                // we click the top part of the detected info window area.
                device.click(rect.centerX(), rect.top - 10)
            } else {
                throw Exception("Marker info window (distance) not found on screen.")
            }
            Thread.sleep(2000)

            // 7. Take a screenshot of the details screen
            Screengrab.screenshot("07_ShowInfo")

            // 8. Tap the menu icon to save the distance
            composeTestRule.onNodeWithTag("save_distance_button").performClick()
            Thread.sleep(1000)

            // 9. Type "De Las Canteras a Morro Jable"
            composeTestRule.onNodeWithTag("save_distance_text_field")
                .performTextInput("De Las Canteras a Morro Jable")
            Thread.sleep(1000)

            // Hide keyboard and floating menu before taking the screenshot
            // On emulators, the first pressBack usually hides the keyboard.
            device.pressBack()
            Thread.sleep(1000)

            // 10 .Take a screenshot
            Screengrab.screenshot("08_SaveDistanceDialog")

            // 11. Save the distance
            composeTestRule.onNodeWithTag("save_distance_confirm_button").performClick()
            Thread.sleep(1000)

            // 12. Exit the screen
            composeTestRule.onNodeWithTag("show_info_back_button").performClick()
            Thread.sleep(1000)

            // 13. Open Menu
            composeTestRule.onNodeWithTag("toolbar_open_menu").performClick()
            Thread.sleep(1000)
            composeTestRule.onNodeWithTag("navigation_rail_menu_load_distances").performClick()
            Thread.sleep(2000)

            // 14. Select the first saved distance
            composeTestRule.onAllNodesWithTag("distance_selection_item")[0].performClick()
            Thread.sleep(2000)

            // 15. Verify distance is shown on map (info window exists)
            val finalInfoWindow = device.findObject(UiSelector().descriptionContains("km")).takeIf { it.exists() }
                ?: device.findObject(UiSelector().descriptionContains("mi")).takeIf { it.exists() }
            
            if (finalInfoWindow == null || !finalInfoWindow.exists()) {
                throw Exception("Distance not shown on map after loading.")
            }
            Screengrab.screenshot("09_LoadedDistance")
        }
    }
}
