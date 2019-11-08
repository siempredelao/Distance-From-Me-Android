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

package gc.david.dfm.migration

import org.greenrobot.greendao.database.Database

/**
 * An abstract base class with the basic implementation to promote reuse.
 *
 *
 * Created by David on 29/10/2014.
 */
abstract class MigrationImpl : Migration {

    /**
     * A helper method which helps the migration prepare by passing the call up the chain.
     *
     * @param db             Database.
     * @param currentVersion Version before migration.
     */
    protected fun prepareMigration(db: Database, currentVersion: Int) {
        require(currentVersion >= 1) {
            ("Lowest supported schema version is 1, unable to prepare for migration from version: $currentVersion")
        }

        if (currentVersion < getSourceVersion()) {
            val previousMigration = getPreviousMigration()

            if (previousMigration == null) {
                // This is the first migration
                check(currentVersion == getSourceVersion()) {
                    ("Unable to apply migration as Version: $currentVersion is not suitable for this Migration.")
                }
            }
            check(previousMigration!!.applyMigration(db, currentVersion) == getSourceVersion()) {
                // For all other migrations ensure that after the earlier
                // migration has been applied the expected version matches
                "Error, expected migration parent to update database to appropriate version"
            }
        }
    }
}
