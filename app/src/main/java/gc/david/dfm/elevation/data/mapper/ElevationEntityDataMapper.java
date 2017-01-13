package gc.david.dfm.elevation.data.mapper;

import java.util.ArrayList;
import java.util.List;

import gc.david.dfm.elevation.data.model.ElevationEntity;
import gc.david.dfm.elevation.data.model.Result;
import gc.david.dfm.elevation.domain.model.Elevation;

/**
 * Created by david on 13.01.17.
 * <p>
 * Mapper class used to transform {@link ElevationEntity} in the Data layer
 * to {@link Elevation } in the Domain layer.
 */
public class ElevationEntityDataMapper {

    public static Elevation transform(final ElevationEntity elevationEntity) {
        final List<Double> elevationList = new ArrayList<>();
        for (Result result : elevationEntity.getResults()) {
            elevationList.add(result.getElevation());
        }
        return new Elevation(elevationList);
    }
}
