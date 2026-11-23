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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Screen reader labels have to come from string resources, so TalkBack reads them in the user's language.
 */
class AccessibilityKonsistTest {

    @Test
    fun `compose content descriptions are not hard-coded`() {
        val hardCoded = Konsist.scopeFromProduction()
            .files
            .filter { HARD_CODED_COMPOSE.containsMatchIn(it.text) }
            .map { it.name }

        assertEquals(emptyList<String>(), hardCoded)
    }

    @Test
    fun `layout content descriptions are not hard-coded`() {
        val layouts = File(Konsist.projectRootPath).walkTopDown()
            .onEnter { it.name != "build" && !it.name.startsWith(".") }
            .filter { it.extension == "xml" && it.parentFile.name.startsWith("layout") }
            .toList()
        check(layouts.isNotEmpty()) { "No layouts found under ${Konsist.projectRootPath}" }

        val hardCoded = layouts
            .filter { HARD_CODED_LAYOUT.containsMatchIn(it.readText()) }
            .map { it.name }

        assertEquals(emptyList<String>(), hardCoded)
    }

    private companion object {

        val HARD_CODED_COMPOSE = Regex("""contentDescription\s*=\s*"""")
        val HARD_CODED_LAYOUT = Regex("""android:contentDescription\s*=\s*"[^@]""")
    }
}
