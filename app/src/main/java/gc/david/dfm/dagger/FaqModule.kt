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

import dagger.Module
import dagger.Provides
import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.MainThread
import gc.david.dfm.faq.GetFaqsDiskDataSource
import gc.david.dfm.faq.GetFaqsInteractor
import gc.david.dfm.faq.GetFaqsRepository
import gc.david.dfm.faq.GetFaqsUseCase
import javax.inject.Singleton

/**
 * Created by david on 27.12.16.
 */
@Module
class FaqModule {

    @Provides
    @Singleton
    fun provideGetFaqsRepository(): GetFaqsRepository {
        return GetFaqsDiskDataSource()
    }

    @Provides
    @Singleton
    fun provideGetFaqsUseCase(executor: Executor,
                              mainThread: MainThread,
                              getFaqsRepository: GetFaqsRepository
    ): GetFaqsUseCase {
        return GetFaqsInteractor(executor, mainThread, getFaqsRepository)
    }
}
