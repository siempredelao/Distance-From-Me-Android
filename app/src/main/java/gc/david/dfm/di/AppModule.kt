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

import android.app.Activity
import com.google.android.gms.location.LocationServices
import gc.david.dfm.AppNavigator
import gc.david.dfm.BuildConfigProvider
import gc.david.dfm.DefaultBuildConfigProvider
import gc.david.dfm.distance.data.BaseDistanceModeRepository
import gc.david.dfm.distance.domain.DistanceModeRepository
import gc.david.dfm.initializers.DefaultUnitInitializer
import gc.david.dfm.initializers.FirebaseInitializer
import gc.david.dfm.initializers.Initializers
import gc.david.dfm.initializers.LoggingInitializer
import gc.david.dfm.initializers.MapInitializer
import gc.david.dfm.location.data.BaseLocationRepository
import gc.david.dfm.location.data.DefaultPermissionChecker
import gc.david.dfm.location.domain.LocationRepository
import gc.david.dfm.location.domain.PermissionChecker
import gc.david.dfm.main.domain.GetStoredDistancesUseCase
import gc.david.dfm.main.presentation.MainViewModel
import gc.david.dfm.main.presentation.mapper.LoadDistancesMapper
import gc.david.dfm.main.presentation.mapper.MapStateMapper
import gc.david.dfm.onboarding.domain.MapHintProcessor
import gc.david.dfm.support.navigation.Navigator
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { arrayOf(DefaultUnitInitializer(get()), FirebaseInitializer(), LoggingInitializer(get()), MapInitializer()) }
    single { Initializers(get()) }
    single<DistanceModeRepository> { BaseDistanceModeRepository() }
    single { LocationServices.getFusedLocationProviderClient(androidContext()) }
    single<LocationRepository> { BaseLocationRepository(get()) }
    single<PermissionChecker> { DefaultPermissionChecker(get()) }
    single<BuildConfigProvider> { DefaultBuildConfigProvider() }
    // One per Activity: the navigator holds the Activity it launches from
    factory<Navigator> { (activity: Activity) -> AppNavigator(activity) }
    single { MapStateMapper(get()) }
    single { LoadDistancesMapper(get()) }
    single { MapHintProcessor() }
    single { GetStoredDistancesUseCase(get()) }
    viewModel { MainViewModel(get(),get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
