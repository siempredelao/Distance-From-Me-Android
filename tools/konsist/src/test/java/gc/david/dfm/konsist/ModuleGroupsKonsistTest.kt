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
 * Modules live in groups that may only depend inwards: `features` on `core` and `support`,
 * `core` on `core` and `support`, and `support` on `support` alone. `:app` sits outside the
 * groups and may use all of them.
 */
class ModuleGroupsKonsistTest {

    private val productionFiles = Konsist.scopeFromProduction().files

    @Test
    fun `core code does not import feature code`() {
        assertEquals(emptyList<String>(), importViolations(from = CORE, forbidden = listOf(FEATURES)))
    }

    @Test
    fun `support code does not import core or feature code`() {
        assertEquals(emptyList<String>(), importViolations(from = SUPPORT, forbidden = listOf(CORE, FEATURES)))
    }

    @Test
    fun `core build files do not depend on feature modules`() {
        assertEquals(emptyList<String>(), buildFileViolations(from = CORE, forbidden = listOf(FEATURES)))
    }

    @Test
    fun `support build files do not depend on core or feature modules`() {
        assertEquals(emptyList<String>(), buildFileViolations(from = SUPPORT, forbidden = listOf(CORE, FEATURES)))
    }

    private fun importViolations(from: String, forbidden: List<String>): List<String> {
        val forbiddenPackages = forbidden.flatMap { group -> packagesOf(group) }.toSet()
        return productionFiles
            .filter { it.moduleName.startsWith("$from/") }
            .flatMap { file ->
                file.imports
                    .filter { import -> enclosingNames(import.name).any { it in forbiddenPackages } }
                    .map { "${file.moduleName}/${file.name}: ${it.name}" }
            }
    }

    private fun packagesOf(group: String): Set<String> {
        val packages = productionFiles
            .filter { it.moduleName.startsWith("$group/") }
            .mapNotNull { it.packagee?.name }
            .toSet()
        check(packages.isNotEmpty()) { "No production packages found under $group/" }
        return packages
    }

    /** `a.b.C.D` → `a.b.C`, `a.b`, `a`: an import names a class, a nested class or a top-level function. */
    private fun enclosingNames(importName: String): List<String> {
        val segments = importName.split('.')
        return (segments.size - 1 downTo 1).map { segments.take(it).joinToString(".") }
    }

    private fun buildFileViolations(from: String, forbidden: List<String>): List<String> {
        val groupDir = File(Konsist.projectRootPath, from)
        val buildFiles = groupDir.walkTopDown()
            .onEnter { it.name != "build" && !it.name.startsWith(".") }
            .filter { it.name == "build.gradle.kts" }
            .toList()
        check(buildFiles.isNotEmpty()) { "No build files found under ${groupDir.path}" }
        val dependency = Regex("""project\(":(${forbidden.joinToString("|")}):[^"]+"\)""")
        return buildFiles.flatMap { file ->
            dependency.findAll(file.readText()).map { "$from/${file.parentFile.relativeTo(groupDir)}: ${it.value}" }
        }
    }

    private companion object {
        const val CORE = "core"
        const val FEATURES = "features"
        const val SUPPORT = "support"
    }
}
