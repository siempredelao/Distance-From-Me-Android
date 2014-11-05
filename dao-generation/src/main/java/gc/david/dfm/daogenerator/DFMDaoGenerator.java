package gc.david.dfm.daogenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.greenrobot.daogenerator.DaoGenerator;

public class DFMDaoGenerator {

    private static final String SCHEMA_OUTPUT_DIR      = "../app/src/main/java";
    private static final String SCHEMA_TEST_OUTPUT_DIR = "../app/src/androidTest/java";

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
    public static void main(String[] args) throws IOException, Exception {

        final List<SchemaVersion> versions = new ArrayList<SchemaVersion>();
        versions.add(new Version1(false));
        versions.add(new Version2(false));
        // Workaround to have each version with its version number and final version outside
        versions.add(new WorkingVersion(true));

        validateSchemas(versions);

        for (final SchemaVersion version : versions) {
            // NB: Test output creates stubs, we have an established testing
            // standard which should be followed in preference to generating
            // these stubs.
            new DaoGenerator().generateAll(version.getSchema(), SCHEMA_OUTPUT_DIR, SCHEMA_TEST_OUTPUT_DIR);
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
        final Set<Integer> versionNumbers = new HashSet<Integer>();

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
