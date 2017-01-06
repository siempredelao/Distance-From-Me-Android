package gc.david.dfm.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gc.david.dfm.elevation.ElevationInteractor;
import gc.david.dfm.elevation.ElevationRemoteDataSource;
import gc.david.dfm.elevation.ElevationRepository;
import gc.david.dfm.elevation.ElevationUseCase;
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
}
