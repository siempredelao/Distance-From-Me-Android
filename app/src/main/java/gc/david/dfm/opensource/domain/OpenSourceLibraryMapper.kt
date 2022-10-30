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

package gc.david.dfm.opensource.domain

import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity

class OpenSourceLibraryMapper {

    operator fun invoke(entityList: List<OpenSourceLibraryEntity>): List<OpenSourceLibrary> {
        return entityList.map(this::invoke)
    }

    operator fun invoke(entity: OpenSourceLibraryEntity): OpenSourceLibrary {
        return OpenSourceLibrary(
            entity.name,
            entity.description,
            entity.author,
            entity.version,
            entity.link,
            getLicense(entity.licenseCode),
            entity.licenseYear
        )
    }

    private fun getLicense(licenseCode: String) = License.fromCode(licenseCode)
}