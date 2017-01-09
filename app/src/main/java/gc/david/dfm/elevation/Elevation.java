package gc.david.dfm.elevation;

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
    }

    interface Presenter {
        void buildChart(List<LatLng> coordinates);

        void onChartBuilt();

        void onOpenChart();

        void onCloseChart();

        void onReset();
    }

}
