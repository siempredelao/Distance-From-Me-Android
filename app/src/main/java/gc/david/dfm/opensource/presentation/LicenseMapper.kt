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

package gc.david.dfm.opensource.presentation

import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.opensource.domain.License

/**
 * Created by david on 05.02.17.
 */
class LicenseMapper(private val resourceProvider: ResourceProvider) {

    operator fun invoke(license: License, year: String, author: String) =
        when (license) {
            License.APACHE_V2 ->
                resourceProvider.get(R.string.license_apache2, year, author)
            License.MIT ->
                resourceProvider.get(R.string.license_mit, year, author)
            License.EPL_1_0 ->
                resourceProvider.get(R.string.license_epl1)
            License.COPYRIGHT ->
                resourceProvider.get(R.string.license_copyright, year, author)
        }
}
