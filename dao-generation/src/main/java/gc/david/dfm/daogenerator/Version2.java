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

package gc.david.dfm.daogenerator;

import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Property;
import org.greenrobot.greendao.generator.Schema;

/**
 * Created by David on 29/10/2014.
 * Just giving a proper name to entities.
 */
public class Version2 extends Version1 {

    private static final String DISTANCE_NAME = "Distance";
    private static final String POSITION_NAME = "Position";

    public Version2(boolean current) {
        super(current);

        final Schema schema = getSchema();

        final Entity distanceEntity = addDistanceEntity(schema);
        final Entity positionEntity = addPositionEntity(schema);

        final Property distanceIdProperty = positionEntity.addLongProperty("distanceId").notNull().getProperty();
        distanceEntity.addToMany(positionEntity, distanceIdProperty);

        removeEntryEntity(schema);
    }

    private Entity addDistanceEntity(final Schema schema) {
        final Entity distance = schema.addEntity(DISTANCE_NAME);

        distance.addIdProperty().autoincrement();
        distance.addStringProperty("name").notNull();
        distance.addStringProperty("distance").notNull();
        distance.addDateProperty("date").notNull();

        return distance;
    }

    private Entity addPositionEntity(final Schema schema) {
        final Entity position = schema.addEntity(POSITION_NAME);

        position.addIdProperty().autoincrement();
        position.addDoubleProperty("latitude").notNull();
        position.addDoubleProperty("longitude").notNull();

        return position;
    }

    private void removeEntryEntity(final Schema schema) {
        removeEntity("Entry", schema);
    }

    @Override
    public int getVersionNumber() {
        return 2;
    }
}
