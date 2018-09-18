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

package gc.david.dfm.daogenerator;

import org.greenrobot.greendao.generator.DaoGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DFMDaoGenerator {

    private static final String SCHEMA_OUTPUT_DIR = "app/src/main/java";

    /**
     * Generator main application which builds all of the schema versions
     * (including older versions used for migration test purposes) and ensures
     * business rules are met; these include ensuring we only have a single
     * current schema instance and the version numbering is correct.
     *
     * @param args
     * @throws Exception
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {

        final List<SchemaVersion> versions = new ArrayList<>();
        versions.add(new Version1(false));
        versions.add(new Version2(false));
        // Workaround to have each version with its version number and final version outside
        versions.add(new WorkingVersion(true));

        validateSchemas(versions);

        for (final SchemaVersion version : versions) {
            // NB: Test output creates stubs, we have an established testing
            // standard which should be followed in preference to generating
            // these stubs.
            new DaoGenerator().generateAll(version.getSchema(), SCHEMA_OUTPUT_DIR);
        }
    }

    /**
     * Validate the schema.
     *
     * @param versions versions
     * @throws IllegalArgumentException if data is invalid
     */
    public static void validateSchemas(final List<SchemaVersion> versions) throws IllegalArgumentException {
        int numCurrent = 0;
        final Set<Integer> versionNumbers = new HashSet<>();

        for (final SchemaVersion version : versions) {
            if (version.isCurrent()) {
                numCurrent++;
            }

            final int versionNumber = version.getVersionNumber();
            versionNumbers.add(versionNumber);
        }

        if (numCurrent != 1) {
            throw new IllegalArgumentException("Unable to generate schema, exactly one schema marked as current is required.");
        }
    }
}
