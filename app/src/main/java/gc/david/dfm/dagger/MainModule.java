/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

package gc.david.dfm.dagger;

import android.content.Context;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gc.david.dfm.DefaultPreferencesProvider;
import gc.david.dfm.PreferencesProvider;
import gc.david.dfm.address.data.AddressRemoteDataSource;
import gc.david.dfm.address.data.AddressRepository;
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper;
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameInteractor;
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesInteractor;
import gc.david.dfm.address.domain.GetAddressUseCase;
import gc.david.dfm.distance.data.DistanceLocalDataSource;
import gc.david.dfm.distance.data.DistanceRepository;
import gc.david.dfm.distance.domain.GetPositionListInteractor;
import gc.david.dfm.distance.domain.GetPositionListUseCase;
import gc.david.dfm.distance.domain.LoadDistancesInteractor;
import gc.david.dfm.distance.domain.LoadDistancesUseCase;
import gc.david.dfm.elevation.data.ElevationRemoteDataSource;
import gc.david.dfm.elevation.data.ElevationRepository;
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper;
import gc.david.dfm.elevation.domain.ElevationInteractor;
import gc.david.dfm.elevation.domain.ElevationUseCase;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.model.DaoSession;

/**
 * Created by david on 27.12.16.
 */
@Module
public class MainModule {

    @Provides
    @Singleton
    ElevationRepository provideElevationRepository() {
        return new ElevationRemoteDataSource();
    }

    @Provides
    @Singleton
    ElevationUseCase provideElevationUseCase(Executor executor,
                                             MainThread mainThread,
                                             ElevationEntityDataMapper elevationEntityDataMapper,
                                             ElevationRepository elevationRepository) {
        return new ElevationInteractor(executor, mainThread, elevationEntityDataMapper, elevationRepository);
    }

    @Provides
    @Singleton
    PreferencesProvider providePreferencesProvider(Context context) {
        return new DefaultPreferencesProvider(context);
    }

    @Provides
    @Singleton
    AddressRepository provideAddressRepository(Context context) {
        return new AddressRemoteDataSource(context);
    }

    @Provides
    @Singleton
    @Named("CoordinatesByName")
    GetAddressUseCase provideGetAddressCoordinatesByNameUseCase(Executor executor,
                                                                MainThread mainThread,
                                                                AddressCollectionEntityDataMapper addressCollectionEntityDataMapper,
                                                                AddressRepository addressRepository) {
        return new GetAddressCoordinatesByNameInteractor(executor,
                                                         mainThread,
                                                         addressCollectionEntityDataMapper,
                                                         addressRepository);
    }

    @Provides
    @Singleton
    @Named("NameByCoordinates")
    GetAddressUseCase provideGetAddressNameByCoordinatesUseCase(Executor executor,
                                                                MainThread mainThread,
                                                                AddressCollectionEntityDataMapper addressCollectionEntityDataMapper,
                                                                AddressRepository addressRepository) {
        return new GetAddressNameByCoordinatesInteractor(executor,
                                                         mainThread,
                                                         addressCollectionEntityDataMapper,
                                                         addressRepository);
    }

    @Provides
    @Singleton
    DistanceRepository provideDistanceRepository(DaoSession daoSession) {
        return new DistanceLocalDataSource(daoSession);
    }

    @Provides
    @Singleton
    LoadDistancesUseCase provideLoadDistancesUseCase(Executor executor,
                                                     MainThread mainThread,
                                                     DistanceRepository elevationRepository) {
        return new LoadDistancesInteractor(executor, mainThread, elevationRepository);
    }

    @Provides
    @Singleton
    GetPositionListUseCase provideGetPositionListUseCase(Executor executor,
                                                       MainThread mainThread,
                                                       DistanceRepository elevationRepository) {
        return new GetPositionListInteractor(executor, mainThread, elevationRepository);
    }
}
