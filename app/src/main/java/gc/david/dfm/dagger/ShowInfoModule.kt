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

package gc.david.dfm.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import gc.david.dfm.address.data.AddressRemoteDataSource
import gc.david.dfm.address.data.AddressRepository
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesInteractor
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.database.DFMDatabase
import gc.david.dfm.distance.data.DistanceLocalDataSource
import gc.david.dfm.distance.data.DistanceRepository
import gc.david.dfm.distance.domain.InsertDistanceInteractor
import gc.david.dfm.distance.domain.InsertDistanceUseCase
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.MainThread
import javax.inject.Singleton

/**
 * Created by david on 16.01.17.
 */
@Module
class ShowInfoModule {

    @Provides
    @Singleton
    fun provideDistanceRepository(database: DFMDatabase): DistanceRepository {
        return DistanceLocalDataSource(database)
    }

    @Provides
    @Singleton
    fun provideAddressRepository(context: Context): AddressRepository {
        return AddressRemoteDataSource(context)
    }

    @Provides
    @Singleton
    fun provideInsertDistanceUseCase(executor: Executor,
                                     mainThread: MainThread,
                                     elevationRepository: DistanceRepository
    ): InsertDistanceUseCase {
        return InsertDistanceInteractor(executor, mainThread, elevationRepository)
    }

    @Provides
    fun provideGetAddressNameByCoordinatesUseCase(executor: Executor,
                                                  mainThread: MainThread,
                                                  addressCollectionEntityDataMapper: AddressCollectionEntityDataMapper,
                                                  addressRepository: AddressRepository
    ): GetAddressNameByCoordinatesUseCase {
        return GetAddressNameByCoordinatesInteractor(executor,
                mainThread,
                addressCollectionEntityDataMapper,
                addressRepository)
    }
}
