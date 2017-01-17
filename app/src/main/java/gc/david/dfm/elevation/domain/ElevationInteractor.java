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

package gc.david.dfm.elevation.domain;

import android.support.annotation.VisibleForTesting;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import gc.david.dfm.elevation.data.ElevationRepository;
import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper;
import gc.david.dfm.elevation.data.model.ElevationEntity;
import gc.david.dfm.elevation.domain.model.Elevation;
import gc.david.dfm.executor.Executor;
import gc.david.dfm.executor.Interactor;
import gc.david.dfm.executor.MainThread;

import static android.support.annotation.VisibleForTesting.PRIVATE;

/**
 * Created by david on 05.01.17.
 */
public class ElevationInteractor implements Interactor, ElevationUseCase {

    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_OK               = "OK";
    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_INVALID_REQUEST  = "INVALID_REQUEST";
    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_REQUEST_DENIED   = "REQUEST_DENIED";
    @VisibleForTesting(otherwise = PRIVATE)
    static final String STATUS_UNKNOWN_ERROR    = "UNKNOWN_ERROR";

    private final Executor                  executor;
    private final MainThread                mainThread;
    private final ElevationEntityDataMapper elevationEntityDataMapper;
    private final ElevationRepository       repository;

    private Callback     callback;
    private List<LatLng> coordinateList;
    private int          maxSamples;

    public ElevationInteractor(final Executor executor,
                               final MainThread mainThread,
                               final ElevationEntityDataMapper elevationEntityDataMapper,
                               final ElevationRepository repository) {
        this.executor = executor;
        this.mainThread = mainThread;
        this.elevationEntityDataMapper = elevationEntityDataMapper;
        this.repository = repository;
    }

    @Override
    public void execute(final List<LatLng> coordinateList, final int maxSamples, final Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback can't be null, the client of this interactor needs to get the response in the callback");
        }
        this.coordinateList = coordinateList;
        this.maxSamples = maxSamples;
        this.callback = callback;
        this.executor.run(this);
    }

    @Override
    public void run() {
        if (coordinateList.isEmpty()) {
            notifyError("Empty coordinates list");
        } else {
            final String coordinatesPath = getCoordinatesPath(coordinateList);

            repository.getElevation(coordinatesPath, maxSamples, new ElevationRepository.Callback() {
                @Override
                public void onSuccess(final ElevationEntity elevationEntity) {
                    if (STATUS_OK.equals(elevationEntity.getStatus())) {
                        final Elevation elevation = elevationEntityDataMapper.transform(elevationEntity);

                        mainThread.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onElevationLoaded(elevation);
                            }
                        });
                    } else {
                        notifyError(elevationEntity.getStatus());
                    }
                }

                @Override
                public void onError(final String errorMessage) {
                    notifyError(errorMessage);
                }
            });
        }
    }

    private void notifyError(final String errorMessage) {
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(errorMessage);
            }
        });
    }

    private String getCoordinatesPath(final List<LatLng> coordinateList) {
        String positionListUrlParameter = "";
        for (int i = 0; i < coordinateList.size(); i++) {
            final LatLng coordinate = coordinateList.get(i);
            positionListUrlParameter += String.valueOf(coordinate.latitude) +
                                        "," +
                                        String.valueOf(coordinate.longitude);
            if (i != coordinateList.size() - 1) {
                positionListUrlParameter += "|";
            }
        }
        return positionListUrlParameter;
    }
}
