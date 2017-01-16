package gc.david.dfm.elevation.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import gc.david.dfm.elevation.domain.model.Elevation;

/**
 * Created by david on 05.01.17.
 */
public interface ElevationUseCase {

    interface Callback {

        void onElevationLoaded(Elevation elevation);

        void onError(String errorMessage);

    }

    void execute(List<LatLng> coordinateList, int maxSamples, Callback callback);
}
