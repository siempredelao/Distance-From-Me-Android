/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by david on 25.01.17.
 *
 *
 * Mapper class used to transform [OpenSourceLibraryEntity] in the Domain layer
 * to [OpenSourceLibraryModel] in the Presentation layer.
 */
@Singleton
class OpenSourceLibraryMapper @Inject constructor() {

    fun transform(openSourceLibraryEntityList: List<OpenSourceLibraryEntity>): List<OpenSourceLibraryModel> {
        return openSourceLibraryEntityList.map {
            OpenSourceLibraryModel(it.name, it.author, it.version, it.link, it.licenseCode, it.licenseYear, it.description)
        }
    }
}
