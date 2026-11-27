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
 * The Play Store texts have to exist for every language and fit Play's limits: fastlane sends an
 * empty text for a locale without release notes, and Play rejects the ones that are too long.
 */
class StoreListingKonsistTest {

    private val root = File(Konsist.projectRootPath)
    private val metadata = root.resolve("fastlane/metadata/android")

    @Test
    fun `every app language has a Play locale`() {
        val buildFile = root.resolve("app/build.gradle.kts").readText()
        val localeFilters = checkNotNull(LOCALE_FILTERS.find(buildFile)) { "No localeFilters in app/build.gradle.kts" }
        val appLanguages = QUOTED.findAll(localeFilters.groupValues[1]).map { it.groupValues[1] }.toSet()

        assertEquals(PLAY_LOCALES.keys, appLanguages)
    }

    @Test
    fun `only the known locales have a folder`() {
        val folders = checkNotNull(metadata.listFiles()) { "No folder ${metadata.path}" }
            .filter { it.isDirectory }
            .map { it.name }
            .toSet()

        assertEquals(NOTES_LOCALES, folders)
    }

    @Test
    fun `every listing has its texts, within the limits`() {
        val violations = PLAY_LOCALES.values.flatMap { locale ->
            LISTING_LIMITS.mapNotNull { (name, limit) -> violation(metadata.resolve("$locale/$name"), limit) }
        }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun `release notes exist in every locale, within the limit`() {
        val versionCodes = notesFiles().map { it.name }.toSet()
        check(versionCodes.isNotEmpty()) { "No release notes found under ${metadata.path}" }

        val violations = versionCodes.flatMap { name ->
            NOTES_LOCALES.mapNotNull { locale -> violation(metadata.resolve("$locale/changelogs/$name"), NOTES_LIMIT) }
        }

        assertEquals(emptyList<String>(), violations)
    }

    @Test
    fun `release notes are lists`() {
        val violations = notesFiles()
            .filter { it.nameWithoutExtension.toInt() >= FIRST_LIST_VERSION_CODE }
            .filter { file -> file.readText().trimEnd().lines().any { !it.startsWith(LIST_MARKER) } }
            .map { it.relativeTo(root).path }

        assertEquals(emptyList<String>(), violations)
    }

    private fun notesFiles() = metadata.walkTopDown().filter { it.parentFile.name == "changelogs" && it.extension == "txt" }.toList()

    private fun violation(file: File, limit: Int): String? {
        val path = file.relativeTo(root).path
        if (!file.isFile) return "$path: missing"
        val text = file.readText()
        val length = text.codePointCount(0, text.length)
        return when {
            text.isBlank() -> "$path: empty"
            length > limit -> "$path: $length characters, the limit is $limit"
            else -> null
        }
    }

    private companion object {

        val LOCALE_FILTERS = Regex("""localeFilters\.addAll\(listOf\(([^)]*)\)""")
        val QUOTED = Regex(""""([^"]+)"""")

        val PLAY_LOCALES = mapOf(
            "en" to "en-GB",
            "ca" to "ca",
            "de" to "de-DE",
            "es" to "es-ES",
            "fr" to "fr-FR",
            "it" to "it-IT",
            "pt" to "pt-PT",
        )

        // These get release notes but have no listing of their own.
        val NOTES_LOCALES = PLAY_LOCALES.values.toSet() + setOf("en-US", "es-419", "es-US", "pt-BR")

        val LISTING_LIMITS = mapOf(
            "title.txt" to 30,
            "short_description.txt" to 80,
            "full_description.txt" to 4000,
        )
        const val NOTES_LIMIT = 500
        const val FIRST_LIST_VERSION_CODE = 36
        const val LIST_MARKER = "- "
    }
}
