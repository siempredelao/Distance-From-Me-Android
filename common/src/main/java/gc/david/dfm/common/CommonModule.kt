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

package gc.david.dfm.common

import gc.david.dfm.ConnectionManager
import gc.david.dfm.DefaultConnectionManager
import gc.david.dfm.DefaultGeocodeApiKeyProvider
import gc.david.dfm.DefaultPermissionChecker
import gc.david.dfm.GeocodeApiKeyProvider
import gc.david.dfm.PermissionChecker
import gc.david.dfm.common.domain.DistanceCalculator
import gc.david.dfm.common.domain.UnitConverter
import gc.david.dfm.common.presentation.DistanceFormatter
import org.koin.dsl.module

val commonModule = module {

    single { ResourceProvider(get()) }
    single<ConnectionManager> { DefaultConnectionManager(get()) }
    single<GeocodeApiKeyProvider> { DefaultGeocodeApiKeyProvider(get()) }
    single<PermissionChecker> { DefaultPermissionChecker(get()) }
    
    // Distance calculation and unit conversion
    single { DistanceCalculator() }
    single { UnitConverter() }
    single { DistanceFormatter(get()) }
}

