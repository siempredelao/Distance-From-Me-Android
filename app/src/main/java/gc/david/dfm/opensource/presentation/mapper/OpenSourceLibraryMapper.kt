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

import gc.david.dfm.opensource.domain.OpenSourceLibrary
import gc.david.dfm.opensource.presentation.LicenseMapper
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryUiModel

/**
 * Created by david on 25.01.17.
 *
 * Mapper class used to transform [OpenSourceLibrary] in the Domain layer
 * to [OpenSourceLibraryUiModel] in the Presentation layer.
 */
class OpenSourceLibraryMapper(
    private val licenseMapper: LicenseMapper
) {

    operator fun invoke(libraries: List<OpenSourceLibrary>): List<OpenSourceLibraryUiModel> {
        return libraries.map(this::invoke)
    }

    operator fun invoke(library: OpenSourceLibrary): OpenSourceLibraryUiModel {
        return with(library) {
            OpenSourceLibraryUiModel(
                name,
                description,
                author,
                "v${library.version}",
                link,
                "${library.license.code} license",
                licenseMapper(library.license, library.year, library.author),
                year
            )
        }
    }
}
