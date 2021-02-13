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

package gc.david.dfm.opensource.presentation

import android.content.Context

import gc.david.dfm.R
import gc.david.dfm.opensource.presentation.model.OpenSourceLibraryModel

/**
 * Created by david on 05.02.17.
 */
object LicensePrinter {

    private const val APACHE_2_0_LICENSE_CODE = "Apache-2.0"
    private const val MIT_LICENSE_CODE = "MIT"
    private const val EPL_1_0_LICENSE_CODE = "EPL-1.0"
    private const val COPYRIGHT_LICENSE_CODE = "Copyright"

    fun print(openSourceLibraryModel: OpenSourceLibraryModel, context: Context): String? {
        return when (openSourceLibraryModel.license) {
            APACHE_2_0_LICENSE_CODE -> context.getString(R.string.license_apache2,
                    openSourceLibraryModel.year,
                    openSourceLibraryModel.author)
            MIT_LICENSE_CODE -> context.getString(R.string.license_mit,
                    openSourceLibraryModel.year,
                    openSourceLibraryModel.author)
            EPL_1_0_LICENSE_CODE -> context.getString(R.string.license_epl1)
            COPYRIGHT_LICENSE_CODE -> context.getString(R.string.license_copyright,
                    openSourceLibraryModel.year,
                    openSourceLibraryModel.author)
            else -> null
        }
    }
}
