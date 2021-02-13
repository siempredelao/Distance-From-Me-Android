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

package gc.david.dfm.opensource.domain

import gc.david.dfm.executor.Executor
import gc.david.dfm.executor.Interactor
import gc.david.dfm.executor.MainThread
import gc.david.dfm.opensource.data.OpenSourceRepository
import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity

/**
 * Created by david on 25.01.17.
 */
class OpenSourceInteractor(
        private val executor: Executor,
        private val mainThread: MainThread,
        private val repository: OpenSourceRepository
) : Interactor, OpenSourceUseCase {

    private lateinit var callback: OpenSourceUseCase.Callback

    override fun execute(callback: OpenSourceUseCase.Callback) {
        this.callback = callback
        this.executor.run(this)
    }

    override fun run() {
        repository.getOpenSourceLibraries(object : OpenSourceRepository.Callback {
            override fun onSuccess(openSourceLibraryEntities: List<OpenSourceLibraryEntity>) {
                mainThread.post(Runnable { callback.onOpenSourceLibrariesLoaded(openSourceLibraryEntities) })
            }

            override fun onError(message: String) {
                mainThread.post(Runnable { callback.onError(message) })
            }
        })
    }
}
