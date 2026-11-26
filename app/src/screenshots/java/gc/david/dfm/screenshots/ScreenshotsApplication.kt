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

package gc.david.dfm.screenshots

import gc.david.dfm.BuildConfigProvider
import gc.david.dfm.DFMApplication
import gc.david.dfm.core.address.domain.AddressRepository
import gc.david.dfm.elevation.domain.ElevationRepository
import gc.david.dfm.main.domain.model.StoredDistances
import gc.david.dfm.main.presentation.mapper.LoadDistancesMapper
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

/** The release app with what the Play Store pictures need. */
class ScreenshotsApplication : DFMApplication() {

    override fun onCreate() {
        super.onCreate()
        loadKoinModules(screenshotsModule)
    }
}

private val screenshotsModule = module {
    single<AddressRepository> { CannedAddresses() }
    single<ElevationRepository> { CannedElevation() }
    // A rate prompt has no place in a store picture
    single<LoadDistancesMapper> {
        object : LoadDistancesMapper(get()) {
            override fun map(storedDistances: StoredDistances) =
                super.map(storedDistances.copy(rateRequest = StoredDistances.RateRequest.NotAvailable))
        }
    }
    // Looks like a release, without the debug section
    single<BuildConfigProvider> {
        object : BuildConfigProvider {
            override fun isReleaseBuild() = true
        }
    }
}
