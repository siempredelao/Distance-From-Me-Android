/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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

package gc.david.dfm.elevation.presentation

import com.google.android.gms.maps.model.LatLng

import gc.david.dfm.ConnectionManager
import gc.david.dfm.PreferencesProvider
import gc.david.dfm.elevation.domain.ElevationUseCase

/**
 * Created by david on 06.01.17.
 */
class ElevationPresenter(
        private val elevationView: Elevation.View,
        private val elevationUseCase: ElevationUseCase,
        private val connectionManager: ConnectionManager,
        private val preferencesProvider: PreferencesProvider
) : Elevation.Presenter {

    private var stopPendingUseCase = false

    init {
        this.elevationView.setPresenter(this)
    }

    override fun buildChart(coordinates: List<LatLng>) {
        stopPendingUseCase = false

        if (preferencesProvider.shouldShowElevationChart() && connectionManager.isOnline()) {
            elevationUseCase.execute(coordinates, ELEVATION_SAMPLES, object : ElevationUseCase.Callback {
                override fun onElevationLoaded(elevation: gc.david.dfm.elevation.domain.model.Elevation) {
                    if (!stopPendingUseCase) {
                        elevationView.buildChart(elevation.results)
                    }
                }

                override fun onError(errorMessage: String) {
                    elevationView.logError(errorMessage)
                }
            })
        } else {
            elevationView.hideChart()
        }
    }

    override fun onChartBuilt() {
        if (!elevationView.isMinimiseButtonShown()) {
            elevationView.showChart()
        }
    }

    override fun onOpenChart() {
        elevationView.animateShowChart()
    }

    override fun onCloseChart() {
        elevationView.animateHideChart()
    }

    override fun onReset() {
        elevationView.hideChart()
        stopPendingUseCase = true // TODO: 06.01.17 improve this workaround, stop thread
    }

    companion object {

        private const val ELEVATION_SAMPLES = 100
    }
}
