package gc.david.dfm.elevation;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by david on 05.01.17.
 */
public interface ElevationUseCase {

    interface Callback {

        void onElevationLoaded(final List<Double> elevationList);

        void onError();

    }

    void execute(List<LatLng> coordinateList, Callback callback);
}
