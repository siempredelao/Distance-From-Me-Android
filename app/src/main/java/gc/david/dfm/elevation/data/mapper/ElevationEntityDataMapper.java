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

package gc.david.dfm.elevation.data.mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import gc.david.dfm.elevation.data.model.ElevationEntity;
import gc.david.dfm.elevation.data.model.Result;
import gc.david.dfm.elevation.domain.model.Elevation;

/**
 * Created by david on 13.01.17.
 * <p>
 * Mapper class used to transform {@link ElevationEntity} in the Data layer
 * to {@link Elevation } in the Domain layer.
 */
@Singleton
public class ElevationEntityDataMapper {

    @Inject
    public ElevationEntityDataMapper() {
    }

    public Elevation transform(final ElevationEntity elevationEntity) {
        final List<Double> elevationList = new ArrayList<>();
        for (Result result : elevationEntity.getResults()) {
            elevationList.add(result.getElevation());
        }
        return new Elevation(elevationList);
    }
}
