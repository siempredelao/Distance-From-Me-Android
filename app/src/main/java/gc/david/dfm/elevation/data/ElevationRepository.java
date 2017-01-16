package gc.david.dfm.elevation.data;

import gc.david.dfm.elevation.data.model.ElevationEntity;

/**
 * Created by david on 05.01.17.
 */
public interface ElevationRepository {

    interface Callback {

        void onSuccess(ElevationEntity elevationEntity);

        void onError(String message);

    }

    void getElevation(String coordinatesPath, int maxSamples, Callback callback);
}
