package gc.david.dfm.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gc.david.dfm.ConnectionManager;
import gc.david.dfm.DefaultConnectionManager;
import gc.david.dfm.DefaultPreferencesProvider;
import gc.david.dfm.PreferencesProvider;
import gc.david.dfm.elevation.domain.ElevationInteractor;
import gc.david.dfm.elevation.data.ElevationRemoteDataSource;
import gc.david.dfm.elevation.data.ElevationRepository;
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
                                             ElevationRepository elevationRepository) {
        return new ElevationInteractor(executor, mainThread, elevationRepository);
    }

    @Provides
    @Singleton
    ConnectionManager provideConnectionManager(Context context) {
        return new DefaultConnectionManager(context);
    }

    @Provides
    @Singleton
    PreferencesProvider providePreferencesProvider(Context context) {
        return new DefaultPreferencesProvider(context);
    }


}
