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

package gc.david.dfm.distance.domain;

import java.util.List;

import gc.david.dfm.distance.data.DistanceRepository;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.Interactor;
import gc.david.dfm.executor.MainThread;
import gc.david.dfm.model.Distance;

/**
 * Created by david on 16.01.17.
 */
public class LoadDistancesInteractor implements Interactor, LoadDistancesUseCase {

    private final Executor           executor;
    private final MainThread         mainThread;
    private final DistanceRepository repository;

    private Callback callback;

    public LoadDistancesInteractor(Executor executor, MainThread mainThread, DistanceRepository repository) {
        this.executor = executor;
        this.mainThread = mainThread;
        this.repository = repository;
    }

    @Override
    public void execute(final Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback can't be null, the client of this interactor needs to get the response in the callback");
        }
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        repository.loadDistances(new DistanceRepository.LoadDistancesCallback() {
            @Override
            public void onSuccess(final List<Distance> distanceList) {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDistanceListLoaded(distanceList);
                    }
                });
            }

            @Override
            public void onFailure() {
                mainThread.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError();
                    }
                });
            }
        });
    }
}
