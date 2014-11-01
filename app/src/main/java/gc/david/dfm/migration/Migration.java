package gc.david.dfm.migration;

import android.database.sqlite.SQLiteDatabase;

/**
 * Migration abstraction designed to be a chain of responsibility pattern.
 * <p/>
 * Each migration is capable of passing the migration request higher up the
 * chain until the required migration is found. The result of this chain call
 * for example would be that given a currentVersion of 1 and target of 3
 * MigrationV2ToV3 would be called by the {@link UpgradeHelper} it would defer
 * to MigrationV1ToV2 who would apply the required changes before returning to
 * allow MigrationV2ToV3 to apply it's changes completing the migration process.
 * <p/>
 * Created by David on 29/10/2014.
 */
public interface Migration {

	/**
	 * Apply the migration to the given database
	 *
	 * @param db             to be updated
	 * @param currentVersion the current version before migration
	 * @return the version after migration has been applied
	 */
	int applyMigration(SQLiteDatabase db, int currentVersion);

	/**
	 * @return instance of the previous Migration required if the current
	 * version is to old for this migration. NB: This will only be null
	 * if this is the tip of the chain and there are no other earlier
	 * migrations.
	 */
	Migration getPreviousMigration();

	/**
	 * @return the target (old) version which will be migrated from.
	 */
	int getSourceVersion();

	/**
	 * @return the new version which will result from the migration being
	 * applied.
	 */
	int getMigratedVersion();
}
