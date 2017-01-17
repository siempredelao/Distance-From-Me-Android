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

package gc.david.dfm.elevation.presentation;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by david on 06.01.17.
 */
public interface Elevation {

    interface View {
        void setPresenter(Presenter presenter);

        void hideChart();

        void showChart();

        void buildChart(List<Double> elevationList);

        void animateHideChart();

        void animateShowChart();

        boolean isMinimiseButtonShown();

        void logError(String errorMessage);
    }

    interface Presenter {
        void buildChart(List<LatLng> coordinates);

        void onChartBuilt();

        void onOpenChart();

        void onCloseChart();

        void onReset();
    }

}
