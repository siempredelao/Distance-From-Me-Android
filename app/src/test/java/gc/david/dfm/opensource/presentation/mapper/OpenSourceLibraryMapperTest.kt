/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

import gc.david.dfm.opensource.domain.License
import gc.david.dfm.opensource.domain.OpenSourceLibrary
import gc.david.dfm.opensource.presentation.LicenseMapper
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Created by david on 26.01.17.
 */
class OpenSourceLibraryMapperTest {

    private val licenseMapper: LicenseMapper = mock()

    private val mapper = OpenSourceLibraryMapper(licenseMapper)

    @Test
    fun `transforms open source library entity to open source library model`() {
        val libraryName = "fake name"
        val libraryAuthor = "fake author"
        val libraryVersion = "fake version"
        val libraryLink = "fake link"
        val libraryLicense = License.MIT
        val libraryYear = "fake year"
        val libraryDescription = "fake description"
        val openSourceLibrary = OpenSourceLibrary(
            libraryName,
            libraryDescription,
            libraryAuthor,
            libraryVersion,
            libraryLink,
            libraryLicense,
            libraryYear
        )
        val mappedLicenseDescription = "mapped license description"
        whenever(licenseMapper.invoke(libraryLicense, libraryYear, libraryAuthor))
            .thenReturn(mappedLicenseDescription)

        val actualUiModel = mapper.invoke(openSourceLibrary)

        val expectedUiModel = OpenSourceLibraryUiModel(
            name = libraryName,
            description = libraryDescription,
            author = libraryAuthor,
            version = "vfake version",
            link = libraryLink,
            licenseTitle = "${License.MIT.code} license",
            licenseDescription = mappedLicenseDescription,
            year = libraryYear
        )
        assertEquals(expectedUiModel, actualUiModel)
    }
}