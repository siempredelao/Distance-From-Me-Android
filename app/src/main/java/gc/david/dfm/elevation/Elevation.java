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

        void showChart(List<Double> elevationList);

        void animateHideChart();

        void animateShowChart();
    }

    interface Presenter {
        void buildChart(List<LatLng> coordinates);

        void onCloseChart();

        void onReset();
    }

}
