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

package gc.david.dfm.migration

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import gc.david.dfm.model.DaoMaster
import org.greenrobot.greendao.database.Database

/**
 * A simple helper which determines which migration (if any) is required to be
 * applied when a database is opened.
 *
 *
 * Created by David on 29/10/2014.
 */
class UpgradeHelper(
        context: Context,
        name: String,
        factory: SQLiteDatabase.CursorFactory?
) : DaoMaster.OpenHelper(context, name, factory) {

    /**
     * Apply the appropriate migrations to update the database.
     */
    override fun onUpgrade(database: Database, oldVersion: Int, newVersion: Int) {
        when (newVersion) {
            2 -> MigrateV1ToV2().applyMigration(database, oldVersion)
            else -> { /* nothing*/ }
        }
    }
}
