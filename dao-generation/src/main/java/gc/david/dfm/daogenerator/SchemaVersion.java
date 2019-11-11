/*
 * Copyright (c) 2019 David Aguiar Gonzalez
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
 * A Version of the Schema.
 * <p/>
 * Created by David on 29/10/2014.
 */
public abstract class SchemaVersion {

    public static final String CURRENT_SCHEMA_PACKAGE = "gc.david.dfm.model";
    private final Schema  schema;
    private final boolean current;

    /**
     * Constructor
     *
     * @param current indicating if this is the current schema.
     */
    public SchemaVersion(final boolean current) {
        final int version = getVersionNumber();
        String packageName = CURRENT_SCHEMA_PACKAGE;
        if (!current) {
            packageName += ".v" + version;
        }
        this.schema = new Schema(version, packageName);
        this.schema.enableKeepSectionsByDefault();
        this.current = current;
    }

    /**
     * @return the GreenDAO schema.
     */
    protected Schema getSchema() {
        return schema;
    }

    protected Entity getEntity(final String entityName, final Schema schema) {

        for (final Entity entity : schema.getEntities()) {
            if (entity.getClassName().equals(entityName)) {
                return entity;
            }
        }
        return null;
    }

    protected void removeEntity(final String entityName, final Schema schema) {
        for (final Entity entity : schema.getEntities()) {
            if (entity.getClassName().equals(entityName)) {
                schema.getEntities().remove(entity);
                return;
            }
        }
    }

    protected void removeProperty(final String propertyName, final Entity entity) {
        for (final Property property : entity.getProperties()) {
            if (property.getPropertyName().equals(propertyName)) {
                entity.getProperties().remove(property);
                break;
            }
        }
    }

    /**
     * @return boolean indicating if this is the highest or current schema version.
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * @return unique integer schema version identifier.
     */
    public abstract int getVersionNumber();
}
