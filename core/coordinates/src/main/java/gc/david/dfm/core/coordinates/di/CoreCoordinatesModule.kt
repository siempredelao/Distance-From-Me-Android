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

package gc.david.dfm.core.coordinates.di

import gc.david.dfm.core.coordinates.data.BaseCoordinatesRepository
import gc.david.dfm.core.coordinates.data.CoordinatesMemoryDataSource
import gc.david.dfm.core.coordinates.domain.CoordinatesRepository
import gc.david.dfm.core.coordinates.domain.DistanceCalculator
import org.koin.dsl.module

val coreCoordinatesModule = module {
    single { DistanceCalculator() }
    // One list of points for the whole app: the map adds them and Show info reads them
    single { CoordinatesMemoryDataSource() }
    single<CoordinatesRepository> { BaseCoordinatesRepository(get()) }
}
