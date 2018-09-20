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
import gc.david.dfm.address.data.AddressRemoteDataSource;
import gc.david.dfm.address.data.AddressRepository;
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper;
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesInteractor;
import gc.david.dfm.address.domain.GetAddressUseCase;
import gc.david.dfm.distance.data.DistanceLocalDataSource;
import gc.david.dfm.distance.data.DistanceRepository;
import gc.david.dfm.distance.domain.InsertDistanceInteractor;
import gc.david.dfm.distance.domain.InsertDistanceUseCase;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.model.DaoSession;

/**
 * Created by david on 16.01.17.
 */
@Module
public class ShowInfoModule {

    @Provides
    @Singleton
    DistanceRepository provideDistanceRepository(DaoSession daoSession) {
        return new DistanceLocalDataSource(daoSession);
    }

    @Provides
    @Singleton
    AddressRepository provideAddressRepository(Context context) {
        return new AddressRemoteDataSource(context);
    }

    @Provides
    @Singleton
    InsertDistanceUseCase provideInsertDistanceUseCase(Executor executor,
                                                       MainThread mainThread,
                                                       DistanceRepository elevationRepository) {
        return new InsertDistanceInteractor(executor, mainThread, elevationRepository);
    }

    @Provides
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
}
