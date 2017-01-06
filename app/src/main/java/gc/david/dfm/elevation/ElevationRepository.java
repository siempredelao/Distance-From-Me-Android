package gc.david.dfm.elevation;

import gc.david.dfm.elevation.model.ElevationModel;

/**
 * Created by david on 05.01.17.
 */
public interface ElevationRepository {

    ElevationModel getElevation(String coordinatesPath);

}
