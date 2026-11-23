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

package gc.david.dfm.opensource.data.mapper

import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import gc.david.dfm.opensource.domain.model.License
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OpenSourceLibraryMapperTest {

    private val mapper = OpenSourceLibraryMapper()

    @Test
    fun `maps single entity correctly`() {
        val entity = OpenSourceLibraryEntity(
            name = "Kotlin",
            description = "Programming language",
            author = "JetBrains",
            version = "1.9.0",
            link = "https://kotlinlang.org",
            licenseCode = "Apache-2.0",
            licenseYear = "2023"
        )

        val result = mapper(entity)

        assertEquals("Kotlin", result.name)
        assertEquals("Programming language", result.description)
        assertEquals("JetBrains", result.author)
        assertEquals("1.9.0", result.version)
        assertEquals("https://kotlinlang.org", result.link)
        assertEquals(License.APACHE_V2, result.license)
        assertEquals("2023", result.year)
    }

    @Test
    fun `maps entity list correctly`() {
        val entities = listOf(
            OpenSourceLibraryEntity(
                name = "Lib1", description = "Desc1", author = "Author1",
                version = "1.0", link = "link1", licenseCode = "MIT", licenseYear = "2020"
            ),
            OpenSourceLibraryEntity(
                name = "Lib2", description = "Desc2", author = "Author2",
                version = "2.0", link = "link2", licenseCode = "EPL-1.0", licenseYear = "2021"
            )
        )

        val result = mapper(entities)

        assertEquals(2, result.size)
        assertEquals("Lib1", result[0].name)
        assertEquals(License.MIT, result[0].license)
        assertEquals("Lib2", result[1].name)
        assertEquals(License.EPL_1_0, result[1].license)
    }

    @Test
    fun `maps empty list correctly`() {
        val result = mapper(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `maps Apache license code correctly`() {
        val entity = OpenSourceLibraryEntity("Test", "", "", "", "", "Apache-2.0", "")

        val result = mapper(entity)

        assertEquals(License.APACHE_V2, result.license)
    }

    @Test
    fun `maps MIT license code correctly`() {
        val entity = OpenSourceLibraryEntity("Test", "", "", "", "", "MIT", "")

        val result = mapper(entity)

        assertEquals(License.MIT, result.license)
    }

    @Test
    fun `maps EPL license code correctly`() {
        val entity = OpenSourceLibraryEntity("Test", "", "", "", "", "EPL-1.0", "")

        val result = mapper(entity)

        assertEquals(License.EPL_1_0, result.license)
    }

    @Test
    fun `maps copyright license code correctly`() {
        val entity = OpenSourceLibraryEntity("Test", "", "", "", "", "Copyright", "")

        val result = mapper(entity)

        assertEquals(License.COPYRIGHT, result.license)
    }
}





