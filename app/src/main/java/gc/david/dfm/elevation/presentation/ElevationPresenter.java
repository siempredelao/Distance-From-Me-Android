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

package gc.david.dfm.elevation.presentation;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import gc.david.dfm.ConnectionManager;
import gc.david.dfm.PreferencesProvider;
import gc.david.dfm.elevation.domain.ElevationUseCase;

/**
 * Created by david on 06.01.17.
 */
public class ElevationPresenter implements Elevation.Presenter {

    private static final int ELEVATION_SAMPLES = 100;

    private final Elevation.View      elevationView;
    private final ElevationUseCase    elevationUseCase;
    private final ConnectionManager   connectionManager;
    private final PreferencesProvider preferencesProvider;

    private boolean stopPendingUseCase = false;

    public ElevationPresenter(final Elevation.View elevationView,
                              final ElevationUseCase elevationUseCase,
                              final ConnectionManager connectionManager,
                              final PreferencesProvider preferencesProvider) {
        this.elevationView = elevationView;
        this.elevationUseCase = elevationUseCase;
        this.connectionManager = connectionManager;
        this.preferencesProvider = preferencesProvider;
        this.elevationView.setPresenter(this);
    }

    @Override
    public void buildChart(final List<LatLng> coordinates) {
        stopPendingUseCase = false;

        if (preferencesProvider.shouldShowElevationChart() && connectionManager.isOnline()) {
            elevationUseCase.execute(coordinates, ELEVATION_SAMPLES, new ElevationUseCase.Callback() {
                @Override
                public void onElevationLoaded(final gc.david.dfm.elevation.domain.model.Elevation elevationList) {
                    if (!stopPendingUseCase) {
                        elevationView.buildChart(elevationList.getResults());
                    }
                }

                @Override
                public void onError(final String errorMessage) {
                    elevationView.logError(errorMessage);
                }
            });
        } else {
            elevationView.hideChart();
        }
    }

    @Override
    public void onChartBuilt() {
        if (!elevationView.isMinimiseButtonShown()) {
            elevationView.showChart();
        }
    }

    @Override
    public void onOpenChart() {
        elevationView.animateShowChart();
    }

    @Override
    public void onCloseChart() {
        elevationView.animateHideChart();
    }

    @Override
    public void onReset() {
        elevationView.hideChart();
        stopPendingUseCase = true; // TODO: 06.01.17 improve this workaround, stop thread
    }
}
