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

import androidx.room.Room
import gc.david.dfm.*
import gc.david.dfm.database.DFMDatabase
import gc.david.dfm.distance.data.BaseDistanceRepository
import gc.david.dfm.distance.data.CurrentLocationProvider
import gc.david.dfm.distance.data.DistanceLocalDataSource
import gc.david.dfm.distance.data.DistanceModeProvider
import gc.david.dfm.distance.domain.*
import gc.david.dfm.faq.data.BaseFaqRepository
import gc.david.dfm.faq.data.FaqDiskDataSource
import gc.david.dfm.faq.domain.FaqRepository
import gc.david.dfm.faq.domain.GetFaqsUseCase
import gc.david.dfm.initializers.DefaultUnitInitializer
import gc.david.dfm.initializers.FirebaseInitializer
import gc.david.dfm.initializers.Initializers
import gc.david.dfm.initializers.LoggingInitializer
import gc.david.dfm.main.presentation.MainViewModel
import gc.david.dfm.settings.presentation.SettingsViewModel
import gc.david.dfm.showinfo.presentation.AddressFormatter
import gc.david.dfm.showinfo.presentation.SaveDistanceViewModel
import gc.david.dfm.showinfo.presentation.ShowInfoViewModel
import gc.david.dfm.ui.activity.MapDrawer
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
val appModule = module {

    single { arrayOf(DefaultUnitInitializer(), FirebaseInitializer(), LoggingInitializer(get())) }
    single { Initializers(get()) }
    single<PreferencesProvider> { DefaultPreferencesProvider(get()) }
    single { MapDrawer(get()) }
    single { DistanceModeProvider() }
    single { CurrentLocationProvider() }
}

val viewModelModule = module {

    viewModel { SettingsViewModel(get(), get()) }
    viewModel { ShowInfoViewModel(get(), get(), get(), get()) }
    viewModel { SaveDistanceViewModel(get(), get()) }
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get()) }
}

val useCaseModule = module {
    // Use cases
    factory { GetFaqsUseCase(get()) }
    factory { ClearDistancesUseCase(get()) }
    factory { SaveDistanceUseCase(get()) }
    factory { GetPositionListUseCase(get()) }
    factory { GetDistancesUseCase(get()) }

    // Mappers
    factory { AddressFormatter() }
}

val repositoryModule = module {

    single<DistanceRepository> { BaseDistanceRepository(get()) }
    single<FaqRepository> { BaseFaqRepository(get()) }

    single { DistanceLocalDataSource(get()) }
    single { FaqDiskDataSource() }
}

val storageModule = module {

    single { Room.databaseBuilder(get(), DFMDatabase::class.java, "DistanciasDB.db").build() }
}