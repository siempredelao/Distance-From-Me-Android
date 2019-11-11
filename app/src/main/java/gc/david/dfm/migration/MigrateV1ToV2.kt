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

import org.greenrobot.greendao.database.Database
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Migration from Version1 to Version2
 *
 *
 * Created by David on 29/10/2014.
 */
@Deprecated("To be removed, no longer needed (class created on 2014...)")
class MigrateV1ToV2 : MigrationImpl() {

    override fun getSourceVersion(): Int = 1

    override fun getMigratedVersion(): Int = 2

    override fun getPreviousMigration(): Migration? = null

    override fun applyMigration(db: Database, currentVersion: Int): Int {
        prepareMigration(db, currentVersion)

        val previousDaoMaster = gc.david.dfm.model.v1.DaoMaster(db)
        val previousDaoSession = previousDaoMaster.newSession()
        val previousEntryDao = previousDaoSession.entryDao

        val newDaoMaster = gc.david.dfm.model.v2.DaoMaster(db)
        val newDaoSession = newDaoMaster.newSession()
        val newDistanceDao = newDaoSession.distanceDao
        val newPositionDao = newDaoSession.positionDao

        val previousEntries = previousEntryDao.loadAll()

        // WTF?!
        gc.david.dfm.model.v2.DistanceDao.createTable(db, true)
        gc.david.dfm.model.v2.PositionDao.createTable(db, true)

        for (previousEntry in previousEntries) {
            val newDistance = gc.david.dfm.model.v2.Distance().apply {
                name = previousEntry.nombre
                distance = previousEntry.distancia
                date = fromStringToDateObject(previousEntry.fecha)
            }
            val distanceId = newDistanceDao.insert(newDistance)

            val newPositionOrigin = gc.david.dfm.model.v2.Position().apply {
                latitude = previousEntry.lat_origen
                longitude = previousEntry.lon_origen
                this.distanceId = distanceId
            }
            newPositionDao.insert(newPositionOrigin)

            val newPositionDestination = gc.david.dfm.model.v2.Position().apply {
                latitude = previousEntry.lat_destino
                longitude = previousEntry.lon_destino
                this.distanceId = distanceId
            }
            newPositionDao.insert(newPositionDestination)
        }

        return getMigratedVersion()
    }

    /**
     * Converts a string containing a date to a Date object.
     *
     * @param stringDate string containing the date in "yyyy-MM-dd" format
     * @return A Date object with year = yyyy, month = MM, day = dd
     */
    private fun fromStringToDateObject(stringDate: String): Date? {
        return try {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            simpleDateFormat.parse(stringDate)
        } catch (exception: ParseException) {
            exception.printStackTrace()
            null
        }
    }
}
