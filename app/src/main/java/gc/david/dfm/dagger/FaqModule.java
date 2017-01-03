package gc.david.dfm.dagger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.faq.GetFaqsDiskDataSource;
import gc.david.dfm.faq.GetFaqsInteractor;
import gc.david.dfm.faq.GetFaqsRepository;
import gc.david.dfm.faq.GetFaqsUseCase;

/**
 * Created by david on 27.12.16.
 */
@Module
public class FaqModule {

    @Provides
    @Singleton
    GetFaqsRepository provideGetFaqsRepository() {
        return new GetFaqsDiskDataSource();
    }

    @Provides
    @Singleton
    GetFaqsUseCase provideGetFaqsUseCase(Executor executor,
                                         MainThread mainThread,
                                         GetFaqsRepository getFaqsRepository) {
        return new GetFaqsInteractor(executor, mainThread, getFaqsRepository);
    }
}
