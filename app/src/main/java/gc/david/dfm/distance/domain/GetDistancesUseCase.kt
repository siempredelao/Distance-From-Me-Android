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

package gc.david.dfm.distance.domain

import gc.david.dfm.database.Distance

class GetDistancesUseCase(
    private val repository: DistanceRepository
) {

    suspend operator fun invoke(): Result<List<Distance>> {
        return try {
            val distances = repository.loadDistances()
            Result.success(distances)
        } catch (exception: Throwable) {
            Result.failure(exception)
        }
    }
}
