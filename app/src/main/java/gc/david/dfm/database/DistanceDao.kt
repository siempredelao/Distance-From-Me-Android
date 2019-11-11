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

package gc.david.dfm.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface DistanceDao {

    @Query("SELECT * FROM DISTANCE")
    fun loadAll(): List<Distance>

    @Query("DELETE FROM DISTANCE")
    fun deleteAll()

    @Insert
    fun insert(distance: Distance): Long

}