/*
 * Copyright (c) 2022 David Aguiar Gonzalez
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

package gc.david.dfm.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import gc.david.dfm.database.Distance.Companion.TABLE_NAME
import java.util.*

@Entity(tableName = TABLE_NAME)
data class Distance(
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Long?,
        @ColumnInfo(name = "NAME") val name: String,
        @ColumnInfo(name = "DISTANCE") val distance: String,
        @ColumnInfo(name = "DATE") val date: Date
) {

    companion object {

        const val TABLE_NAME = "DISTANCE"
    }
}