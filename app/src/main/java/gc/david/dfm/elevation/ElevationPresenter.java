package gc.david.dfm.elevation;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by david on 06.01.17.
 */
public class ElevationPresenter implements Elevation.Presenter {

    private final Elevation.View   elevationView;
    private final ElevationUseCase elevationUseCase;

    private boolean stopPendingUseCase = false;

    public ElevationPresenter(final Elevation.View elevationView, final ElevationUseCase elevationUseCase) {
        this.elevationView = elevationView;
        this.elevationUseCase = elevationUseCase;
        this.elevationView.setPresenter(this);
    }

    @Override
    public void buildChart(final List<LatLng> coordinates) {
        stopPendingUseCase = false;

        elevationView.hideChart();

        elevationUseCase.execute(coordinates, new ElevationUseCase.Callback() {
            @Override
            public void onElevationLoaded(List<Double> elevationList) {
                if (!stopPendingUseCase) {
                    elevationView.showChart(elevationList);
                }
            }

            @Override
            public void onError() {
                // TODO: 06.01.17 handle error
            }
        });
    }

    @Override
    public void onCloseChart() {
        elevationView.hideChart();
    }

    @Override
    public void onReset() {
        elevationView.hideChart();
        stopPendingUseCase = true; // TODO: 06.01.17 improve this workaround, stop thread
    }
}
