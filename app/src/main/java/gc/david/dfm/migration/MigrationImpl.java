package gc.david.dfm.migration;

import android.database.sqlite.SQLiteDatabase;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract base class with the basic implementation to promote reuse.
 * <p/>
 * Created by David on 29/10/2014.
 */
public abstract class MigrationImpl implements Migration {

	/**
	 * A helper method which helps the migration prepare by passing the call up the chain.
	 *
	 * @param db
	 * @param currentVersion
	 */
	protected void prepareMigration(final SQLiteDatabase db, final int currentVersion) {
		checkNotNull(db, "Database cannot be null");
		if (currentVersion < 1) {
			throw new IllegalArgumentException(
					"Lowest suported schema version is 1, unable to prepare for migration from version: "
							+ currentVersion);
		}

		if (currentVersion < getSourceVersion()) {
			final Migration previousMigration = getPreviousMigration();

			if (previousMigration == null) {
				// This is the first migration
				if (currentVersion != getSourceVersion()) {
					throw new IllegalStateException(
							"Unable to apply migration as Version: "
									+ currentVersion
									+ " is not suitable for this Migration.");
				}
			}
			if (previousMigration.applyMigration(db, currentVersion) != getSourceVersion()) {
				// For all other migrations ensure that after the earlier
				// migration has been applied the expected version matches
				throw new IllegalStateException(
						"Error, expected migration parent to update database to appropriate version");
			}
		}
	}
}
