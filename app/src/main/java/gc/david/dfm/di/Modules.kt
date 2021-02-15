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

package gc.david.dfm.di

import androidx.room.Room
import gc.david.dfm.ConnectionManager
import gc.david.dfm.DefaultConnectionManager
import gc.david.dfm.DefaultPreferencesProvider
import gc.david.dfm.PreferencesProvider
import gc.david.dfm.address.data.AddressRemoteDataSource
import gc.david.dfm.address.data.AddressRepository
import gc.david.dfm.address.data.NewAddressRemoteDataSource
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameInteractor
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesInteractor
import gc.david.dfm.database.DFMDatabase
import gc.david.dfm.distance.data.DistanceLocalDataSource
import gc.david.dfm.distance.data.DistanceRepository
import gc.david.dfm.distance.data.NewDistanceLocalDataSource
import gc.david.dfm.distance.domain.ClearDistancesInteractor
import gc.david.dfm.distance.domain.GetPositionListInteractor
import gc.david.dfm.distance.domain.InsertDistanceInteractor
import gc.david.dfm.distance.domain.LoadDistancesInteractor
import gc.david.dfm.elevation.data.ElevationRemoteDataSource
import gc.david.dfm.elevation.data.ElevationRepository
import gc.david.dfm.elevation.data.NewElevationRemoteDataSource
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper
import gc.david.dfm.elevation.domain.ElevationInteractor
import gc.david.dfm.executor.NewMainThread
import gc.david.dfm.executor.NewThreadExecutor
import gc.david.dfm.faq.GetFaqsDiskDataSource
import gc.david.dfm.faq.GetFaqsInteractor
import gc.david.dfm.faq.GetFaqsRepository
import gc.david.dfm.faq.NewGetFaqsDiskDataSource
import gc.david.dfm.initializers.DefaultUnitInitializer
import gc.david.dfm.initializers.FirebaseInitializer
import gc.david.dfm.initializers.Initializers
import gc.david.dfm.initializers.LoggingInitializer
import gc.david.dfm.opensource.data.NewOpenSourceDiskDataSource
import gc.david.dfm.opensource.data.OpenSourceDiskDataSource
import gc.david.dfm.opensource.data.OpenSourceRepository
import gc.david.dfm.opensource.domain.OpenSourceInteractor
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryMapper
import org.koin.dsl.module

val appModule = module {

    single<ConnectionManager> { DefaultConnectionManager(get()) }
    single { arrayOf(DefaultUnitInitializer(), FirebaseInitializer(), LoggingInitializer(get())) }
    single { Initializers(get()) }
    single<PreferencesProvider> { DefaultPreferencesProvider(get()) }
    single { NewMainThread() }
    single { NewThreadExecutor() }
}

val viewModelModule = module {
    // TODO
}

val useCaseModule = module {
    // Use cases
    factory { ClearDistancesInteractor(get(), get(), get()) }
    factory { ElevationInteractor(get(), get(), get(), get()) }
    factory { GetAddressCoordinatesByNameInteractor(get(), get(), get(), get()) }
    factory { GetAddressNameByCoordinatesInteractor(get(), get(), get(), get()) }
    factory { GetFaqsInteractor(get(), get(), get()) }
    factory { GetPositionListInteractor(get(), get(), get()) }
    factory { InsertDistanceInteractor(get(), get(), get()) }
    factory { LoadDistancesInteractor(get(), get(), get()) }
    factory { OpenSourceInteractor(get(), get(), get()) }

    // Mappers
    factory { AddressCollectionEntityDataMapper() }
    factory { ElevationEntityDataMapper() }
    factory { OpenSourceLibraryMapper() }
}

val repositoryModule = module {

    single<DistanceRepository> { DistanceLocalDataSource(get()) }
    single<AddressRepository> { AddressRemoteDataSource(get()) }
    single<OpenSourceRepository> { OpenSourceDiskDataSource() }
    single<ElevationRepository> { ElevationRemoteDataSource(get()) }
    single<GetFaqsRepository> { GetFaqsDiskDataSource() }

    // Temporal
    single { NewAddressRemoteDataSource(get()) }
    single { NewDistanceLocalDataSource(get()) }
    single { NewElevationRemoteDataSource(get()) }
    single { NewGetFaqsDiskDataSource() }
    single { NewOpenSourceDiskDataSource() }
}

val storageModule = module {

    single { Room.databaseBuilder(get(), DFMDatabase::class.java, "DistanciasDB.db").build() }
}