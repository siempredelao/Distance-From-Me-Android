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

package gc.david.dfm.opensource.domain;

import java.util.List;

import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.Interactor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.opensource.data.OpenSourceRepository;
import gc.david.dfm.opensource.data.model.OpenSourceLibraryEntity;

/**
 * Created by david on 25.01.17.
 */
public class OpenSourceInteractor implements Interactor, OpenSourceUseCase {

    private final Executor             executor;
    private final MainThread           mainThread;
    private final OpenSourceRepository repository;

    private Callback callback;

    public OpenSourceInteractor(final Executor executor,
                                final MainThread mainThread,
                                final OpenSourceRepository repository) {
        this.executor = executor;
        this.mainThread = mainThread;
        this.repository = repository;
    }

    @Override
    public void execute(final Callback callback) {
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        repository.getOpenSourceLibraries(new OpenSourceRepository.Callback() {
            @Override
            public void onSuccess(final List<OpenSourceLibraryEntity> openSourceLibraryEntities) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onOpenSourceLibrariesLoaded(openSourceLibraryEntities);
                    }
                });
            }

            @Override
            public void onError(final String message) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(message);
                    }
                });
            }
        });
    }
}
