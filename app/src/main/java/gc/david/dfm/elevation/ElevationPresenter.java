package gc.david.dfm.elevation;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import gc.david.dfm.ConnectionManager;
import gc.david.dfm.PreferencesProvider;

/**
 * Created by david on 06.01.17.
 */
public class ElevationPresenter implements Elevation.Presenter {

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
            elevationUseCase.execute(coordinates, new ElevationUseCase.Callback() {
                @Override
                public void onElevationLoaded(List<Double> elevationList) {
                    if (!stopPendingUseCase) {
                        elevationView.buildChart(elevationList);
                    }
                }

                @Override
                public void onError() {
                    // TODO: 06.01.17 handle error
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
