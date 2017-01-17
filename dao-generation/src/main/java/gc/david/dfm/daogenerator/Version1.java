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
import org.greenrobot.greendao.generator.Schema;

/**
 * Created by David on 29/10/2014.
 */
public class Version1 extends SchemaVersion {

    private static final String ENTRY_NAME = "Entry";

    /**
     * Constructor
     *
     * @param current
     */
    public Version1(boolean current) {
        super(current);

        final Schema schema = getSchema();

        addEntryEntity(schema);
    }

    private void addEntryEntity(Schema schema) {
        final Entity entry = schema.addEntity(ENTRY_NAME);

        entry.addIdProperty().autoincrement();
        entry.addStringProperty("nombre").notNull();
        entry.addDoubleProperty("lat_origen").notNull();
        entry.addDoubleProperty("lon_origen").notNull();
        entry.addDoubleProperty("lat_destino").notNull();
        entry.addDoubleProperty("lon_destino").notNull();
        entry.addStringProperty("distancia").notNull();
        entry.addStringProperty("fecha").notNull();
    }

    @Override
    public int getVersionNumber() {
        return 1;
    }
}
