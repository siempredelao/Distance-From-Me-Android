/*
 * Copyright (c) 2018 David Aguiar Gonzalez
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

package gc.david.dfm.distance.data;

import java.util.List;

import gc.david.dfm.model.DaoSession;
import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

/**
 * Created by david on 16.01.17.
 */
public class DistanceLocalDataSource implements DistanceRepository {

    private final DaoSession daoSession;

    public DistanceLocalDataSource(final DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    @Override
    public void insert(final Distance distance, final List<Position> positionList, final Callback callback) {
        final long rowID = daoSession.insert(distance);
        if (rowID == -1) {
            callback.onFailure();
        } else {
            for (final Position position : positionList) {
                position.setDistanceId(rowID);
                daoSession.insert(position);
            }
            callback.onSuccess();
        }
    }

    @Override
    public void loadDistances(final LoadDistancesCallback callback) {
        callback.onSuccess(daoSession.loadAll(Distance.class));
    }

    @Override
    public void clear(final Callback callback) {
        daoSession.deleteAll(Distance.class);
        daoSession.deleteAll(Position.class);
        callback.onSuccess();
    }

    @Override
    public void getPositionListById(final long distanceId, final LoadPositionsByIdCallback callback) {
        callback.onSuccess(daoSession.getPositionDao()._queryDistance_PositionList(distanceId));
    }
}
