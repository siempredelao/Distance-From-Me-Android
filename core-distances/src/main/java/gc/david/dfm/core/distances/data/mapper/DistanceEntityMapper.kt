/*
 * Copyright (c) 2026 David Aguiar Gonzalez
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

package gc.david.dfm.core.distances.data.mapper

import gc.david.dfm.core.distances.data.database.DistanceEntity
import gc.david.dfm.core.distances.domain.model.Distance

/**
 * Mapper class used to transform [DistanceEntity] (Room entity) in the Data layer
 * to [Distance] in the Domain layer.
 */
class DistanceEntityMapper {

    fun toDomain(entity: DistanceEntity): Distance {
        return Distance(
            id = entity.id ?: 0L,
            name = entity.name,
            distance = entity.distance,
            date = entity.date
        )
    }

    fun toDomainList(entities: List<DistanceEntity>): List<Distance> {
        return entities.map { toDomain(it) }
    }
}

