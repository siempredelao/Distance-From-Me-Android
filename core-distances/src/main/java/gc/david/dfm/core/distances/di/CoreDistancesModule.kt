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

package gc.david.dfm.core.distances.di

import androidx.room.Room
import gc.david.dfm.core.distances.data.database.DFMDatabase
import gc.david.dfm.core.distances.data.BaseDistanceRepository
import gc.david.dfm.core.distances.data.DistanceLocalDataSource
import gc.david.dfm.core.distances.domain.ClearDistancesUseCase
import gc.david.dfm.core.distances.domain.DistanceRepository
import gc.david.dfm.core.distances.domain.GetDistancesUseCase
import gc.david.dfm.core.distances.domain.GetPositionListUseCase
import gc.david.dfm.core.distances.domain.SaveDistanceUseCase
import org.koin.dsl.module

val coreDistancesModule = module {
    single { Room.databaseBuilder(get(), DFMDatabase::class.java, "DistanciasDB.db").build() }
    single<DistanceRepository> { BaseDistanceRepository(get()) }
    single { DistanceLocalDataSource(get()) }
    factory { ClearDistancesUseCase(get()) }
    factory { SaveDistanceUseCase(get()) }
    factory { GetPositionListUseCase(get()) }
    factory { GetDistancesUseCase(get()) }
}
