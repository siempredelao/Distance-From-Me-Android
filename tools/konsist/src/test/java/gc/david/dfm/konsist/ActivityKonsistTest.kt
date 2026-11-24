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

package gc.david.dfm.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withParentNamed
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class ActivityKonsistTest {

    private val activities = Konsist.scopeFromProduction()
        .classes()
        .withParentNamed("ComponentActivity", "AppCompatActivity", "FragmentActivity")

    @Test
    fun `activities are named with the Activity suffix`() {
        activities.assertTrue { it.name.endsWith("Activity") }
    }

    @Test
    fun `activities reside in a presentation package`() {
        activities.assertTrue { it.resideInPackage("..presentation..") }
    }

    @Test
    fun `activities are launched through a companion open function`() {
        activities
            // The launcher activity and the map it hands over to aren't opened from another screen.
            .withoutName("LauncherActivity", "MainActivity")
            .assertTrue { activity ->
                activity.objects(includeNested = false)
                    .any { it.hasCompanionModifier && it.hasFunctionWithName("open") }
            }
    }

    @Test
    fun `activities let the system recreate them on configuration changes`() {
        val activitiesHandlingChanges = manifestActivities()
            .filter { it.handlesConfigChanges }
            .map { it.name }
            // MainActivity still handles them itself; removing that is a separate, riskier step.
            .filterNot { it.endsWith(".MainActivity") }

        // Handling changes in the Activity hides lost state: dark mode, locale, font scale
        // and process death still recreate it, so state has to survive recreation anyway.
        assertEquals(emptyList<String>(), activitiesHandlingChanges)
    }

    @Test
    fun `the app is launched under its released component name`() {
        val launchers = manifestElements("activity", "activity-alias")
            .filter { it.hasLauncherCategory }
            .map { it.getAttributeNS(ANDROID_NAMESPACE, "name") }

        // Home screen icons and shortcuts store this name, so a launcher may lose them if it changes
        assertEquals(listOf(".onboarding.presentation.OnboardActivity"), launchers)
    }

    private fun manifestActivities(): List<ManifestActivity> =
        manifestElements("activity").map {
            ManifestActivity(
                name = it.getAttributeNS(ANDROID_NAMESPACE, "name"),
                handlesConfigChanges = it.hasAttributeNS(ANDROID_NAMESPACE, "configChanges"),
            )
        }

    private val Element.hasLauncherCategory: Boolean
        get() = getElementsByTagName("category").elements()
            .any { it.getAttributeNS(ANDROID_NAMESPACE, "name") == "android.intent.category.LAUNCHER" }

    private fun manifestElements(vararg tags: String): List<Element> {
        val manifests = File(Konsist.projectRootPath).walkTopDown()
            .onEnter { it.name != "build" && !it.name.startsWith(".") }
            .filter { it.name == "AndroidManifest.xml" && it.parentFile.parentFile.name == "src" }
            .toList()
        check(manifests.isNotEmpty()) { "No manifests found under ${Konsist.projectRootPath}" }
        return manifests.flatMap { manifest ->
            val document = DocumentBuilderFactory.newInstance()
                .apply { isNamespaceAware = true }
                .newDocumentBuilder()
                .parse(manifest)
            tags.flatMap { document.getElementsByTagName(it).elements() }
        }
    }

    private fun NodeList.elements(): List<Element> = (0 until length).map { item(it) as Element }

    private data class ManifestActivity(val name: String, val handlesConfigChanges: Boolean)

    private companion object {

        const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
