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

package gc.david.dfm.core.address.di

import gc.david.dfm.core.address.data.AddressRemoteDataSource
import gc.david.dfm.core.address.data.BaseAddressRepository
import gc.david.dfm.core.address.data.mapper.AddressCollectionEntityDataMapper
import gc.david.dfm.core.address.domain.AddressRepository
import gc.david.dfm.core.address.domain.GetAddressCoordinatesByNameUseCase
import gc.david.dfm.core.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.core.address.presentation.mapper.GeocodingErrorMessageMapper
import gc.david.dfm.geocoding.di.GOOGLE_API_CLIENT
import org.koin.dsl.module

val coreAddressModule = module {
    factory { GetAddressNameByCoordinatesUseCase(get()) }
    factory { GetAddressCoordinatesByNameUseCase(get()) }
    factory { AddressCollectionEntityDataMapper() }
    factory { GeocodingErrorMessageMapper(get()) }
    single<AddressRepository> { BaseAddressRepository(get(), get()) }
    single { AddressRemoteDataSource(get(), get(GOOGLE_API_CLIENT)) }
}
