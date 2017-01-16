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
import gc.david.dfm.elevation.data.ElevationRemoteDataSource;
import gc.david.dfm.elevation.data.ElevationRepository;
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper;
import gc.david.dfm.elevation.domain.ElevationInteractor;
import gc.david.dfm.elevation.domain.ElevationUseCase;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;

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
                                                                AddressRepository elevationRepository) {
        return new GetAddressCoordinatesByNameInteractor(executor,
                                                         mainThread,
                                                         addressCollectionEntityDataMapper,
                                                         elevationRepository);
    }

    @Provides
    @Singleton
    @Named("NameByCoordinates")
    GetAddressUseCase provideGetAddressNameByCoordinatesUseCase(Executor executor,
                                                                MainThread mainThread,
                                                                AddressCollectionEntityDataMapper addressCollectionEntityDataMapper,
                                                                AddressRepository elevationRepository) {
        return new GetAddressNameByCoordinatesInteractor(executor,
                                                         mainThread,
                                                         addressCollectionEntityDataMapper,
                                                         elevationRepository);
    }
}
