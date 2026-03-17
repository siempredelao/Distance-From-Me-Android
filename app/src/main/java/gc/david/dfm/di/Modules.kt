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

package gc.david.dfm.di

import gc.david.dfm.distance.data.BaseCoordinatesRepository
import gc.david.dfm.distance.data.CoordinatesMemoryDataSource
import gc.david.dfm.distance.data.CurrentLocationProvider
import gc.david.dfm.distance.data.DistanceModeProvider
import gc.david.dfm.distance.domain.CoordinatesRepository
import gc.david.dfm.initializers.DefaultUnitInitializer
import gc.david.dfm.initializers.FirebaseInitializer
import gc.david.dfm.initializers.Initializers
import gc.david.dfm.initializers.LoggingInitializer
import gc.david.dfm.location.GeofencingLocationManager
import gc.david.dfm.main.presentation.MainViewModel
import gc.david.dfm.ui.activity.MapDrawer
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { arrayOf(DefaultUnitInitializer(get()), FirebaseInitializer(), LoggingInitializer(get())) }
    single { Initializers(get()) }
    single { MapDrawer(get()) }
    single { DistanceModeProvider() }
    single { CurrentLocationProvider() }
    single { CoordinatesMemoryDataSource() }
    single<CoordinatesRepository> { BaseCoordinatesRepository(get()) }
    factory { GeofencingLocationManager(get(), get()) }
}

val viewModelModule = module {

    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}

