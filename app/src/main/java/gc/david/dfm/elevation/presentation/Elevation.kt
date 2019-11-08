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

package gc.david.dfm.elevation.presentation

import com.google.android.gms.maps.model.LatLng

/**
 * Created by david on 06.01.17.
 */
interface Elevation {

    interface View {

        val isMinimiseButtonShown: Boolean
        fun setPresenter(presenter: Presenter)

        fun hideChart()

        fun showChart()

        fun buildChart(elevationList: List<Double>)

        fun animateHideChart()

        fun animateShowChart()

        fun logError(errorMessage: String)
    }

    interface Presenter {
        fun buildChart(coordinates: List<LatLng>)

        fun onChartBuilt()

        fun onOpenChart()

        fun onCloseChart()

        fun onReset()
    }

}
