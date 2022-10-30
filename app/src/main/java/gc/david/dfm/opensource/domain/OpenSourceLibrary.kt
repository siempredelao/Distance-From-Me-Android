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

data class OpenSourceLibrary(
    val name: String,
    val description: String,
    val author: String,
    val version: String,
    val link: String,
    val license: License,
    val year: String
)

enum class License(val code: String) {
    APACHE_V2(OpenSourceLibraryEntity.APACHE_2_0_LICENSE_CODE),
    MIT(OpenSourceLibraryEntity.MIT_LICENSE_CODE),
    EPL_1_0(OpenSourceLibraryEntity.EPL_1_0_LICENSE_CODE),
    COPYRIGHT(OpenSourceLibraryEntity.COPYRIGHT_LICENSE_CODE);

    companion object {

        fun fromCode(code: String) =
            values().find { it.code == code } ?: error("Invalid license with code $code")
    }
}
