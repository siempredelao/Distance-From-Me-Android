/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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

import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by david on 26.01.17.
 */
class OpenSourceLibraryMapperTest {

    @Test
    fun `transforms open source library entity to open source library model`() {
        val libraryName = "fake name"
        val libraryAuthor = "fake author"
        val libraryVersion = "fake version"
        val libraryLink = "fake link"
        val libraryLicense = "fake license"
        val libraryYear = "fake year"
        val libraryDescription = "fake description"
        val openSourceLibraryEntity = OpenSourceLibraryEntity(libraryName,
                libraryAuthor,
                libraryVersion,
                libraryLink,
                libraryLicense,
                libraryYear,
                libraryDescription)

        val openSourceLibraryEntityList = mutableListOf(openSourceLibraryEntity)

        val openSourceLibraryModelList =
                OpenSourceLibraryMapper().transform(openSourceLibraryEntityList)

        assertEquals(1, openSourceLibraryModelList.size)
        val openSourceLibraryModel = openSourceLibraryModelList[0]
        assertEquals(libraryName, openSourceLibraryModel.name)
        assertEquals(libraryAuthor, openSourceLibraryModel.author)
        assertEquals(libraryVersion, openSourceLibraryModel.version)
        assertEquals(libraryLink, openSourceLibraryModel.link)
        assertEquals(libraryLicense, openSourceLibraryModel.license)
        assertEquals(libraryYear, openSourceLibraryModel.year)
        assertEquals(libraryDescription, openSourceLibraryModel.description)
    }
}