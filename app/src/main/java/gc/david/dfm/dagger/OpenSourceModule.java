/*
 * Copyright (c) 2017 David Aguiar Gonzalez
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

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.opensource.data.OpenSourceDiskDataSource;
import gc.david.dfm.opensource.data.OpenSourceRepository;
import gc.david.dfm.opensource.domain.OpenSourceInteractor;
import gc.david.dfm.opensource.domain.OpenSourceUseCase;

/**
 * Created by david on 27.12.16.
 */
@Module
public class OpenSourceModule {

    @Provides
    @Singleton
    OpenSourceRepository provideOpenSourceRepository() {
        return new OpenSourceDiskDataSource();
    }

    @Provides
    @Singleton
    OpenSourceUseCase provideOpenSourceUseCase(Executor executor,
                                               MainThread mainThread,
                                               OpenSourceRepository openSourceRepository) {
        return new OpenSourceInteractor(executor, mainThread, openSourceRepository);
    }
}
