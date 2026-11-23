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

class BuildConventionsKonsistTest {

    @Test
    fun `module build files leave the shared setup to the build-logic convention plugins`() {
        val root = File(Konsist.projectRootPath)
        val moduleBuildFiles = root.walkTopDown()
            .onEnter { it.name != "build" && it.name != "build-logic" && !it.name.startsWith(".") }
            .filter { it.name == "build.gradle.kts" && it.parentFile != root }
            .toList()
        check(moduleBuildFiles.isNotEmpty()) { "No module build files found under ${root.path}" }

        val violations = moduleBuildFiles.flatMap { file ->
            val text = file.readText()
            CONVENTION_OWNED.filter { it in text }.map { "${file.relativeTo(root)}: $it" }
        }

        assertEquals(emptyList<String>(), violations)
    }

    private companion object {

        // Each of these is set by a dfm.* plugin. A module that sets it again drifts from the others.
        val CONVENTION_OWNED = listOf(
            "compileSdk",
            "minSdk",
            "targetSdk",
            "sourceCompatibility",
            "targetCompatibility",
            "jvmTarget",
            "useJUnitPlatform",
            "compose = true",
            "libs.plugins.android.application",
            "libs.plugins.android.library",
            "libs.plugins.jetbrains.kotlin.android",
            "libs.plugins.jetbrains.kotlin.jvm",
            "libs.plugins.kotlin.compose.compiler",
            "libs.compose.bom",
            "libs.junit.jupiter.params",
            "project(\":test-support\")",
        )
    }
}
