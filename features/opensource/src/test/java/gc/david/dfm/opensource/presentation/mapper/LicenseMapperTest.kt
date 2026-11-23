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

package gc.david.dfm.opensource.presentation.mapper

import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.opensource.R
import gc.david.dfm.opensource.domain.model.License
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LicenseMapperTest {

    private val resourceProvider = mock<ResourceProvider>()
    private val mapper = LicenseMapper(resourceProvider)

    @Test
    fun `maps Apache V2 license correctly`() {
        val year = "2023"
        val author = "Test Author"
        whenever(resourceProvider.get(R.string.license_apache2, year, author))
            .thenReturn("Apache 2.0 License - Copyright $year $author")

        val result = mapper(License.APACHE_V2, year, author)

        assertEquals("Apache 2.0 License - Copyright $year $author", result)
    }

    @Test
    fun `maps MIT license correctly`() {
        val year = "2022"
        val author = "MIT Author"
        whenever(resourceProvider.get(R.string.license_mit, year, author))
            .thenReturn("MIT License - Copyright $year $author")

        val result = mapper(License.MIT, year, author)

        assertEquals("MIT License - Copyright $year $author", result)
    }

    @Test
    fun `maps EPL 1_0 license correctly`() {
        val year = "2021"
        val author = "EPL Author"
        whenever(resourceProvider.get(R.string.license_epl1))
            .thenReturn("Eclipse Public License 1.0")

        val result = mapper(License.EPL_1_0, year, author)

        assertEquals("Eclipse Public License 1.0", result)
    }

    @Test
    fun `maps Copyright license correctly`() {
        val year = "2020"
        val author = "Copyright Author"
        whenever(resourceProvider.get(R.string.license_copyright, year, author))
            .thenReturn("Copyright $year $author")

        val result = mapper(License.COPYRIGHT, year, author)

        assertEquals("Copyright $year $author", result)
    }

    @Test
    fun `handles empty year and author`() {
        val year = ""
        val author = ""
        whenever(resourceProvider.get(R.string.license_apache2, year, author))
            .thenReturn("Apache 2.0 License - Copyright  ")

        val result = mapper(License.APACHE_V2, year, author)

        assertEquals("Apache 2.0 License - Copyright  ", result)
    }
}

