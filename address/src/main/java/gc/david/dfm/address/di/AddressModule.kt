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

package gc.david.dfm.address.di

import gc.david.dfm.address.data.AddressRemoteDataSource
import gc.david.dfm.address.data.BaseAddressRepository
import gc.david.dfm.address.data.mapper.AddressCollectionEntityDataMapper
import gc.david.dfm.address.domain.AddressRepository
import gc.david.dfm.address.domain.GetAddressCoordinatesByNameUseCase
import gc.david.dfm.address.domain.GetAddressNameByCoordinatesUseCase
import gc.david.dfm.address.presentation.AddressViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val addressModule = module {
    viewModel { AddressViewModel(get(), get(), get(), get()) }
    factory { GetAddressNameByCoordinatesUseCase(get(), get()) }
    factory { GetAddressCoordinatesByNameUseCase(get(), get()) }
    factory { AddressCollectionEntityDataMapper() }
    single<AddressRepository> { BaseAddressRepository(get()) }
    single { AddressRemoteDataSource(get()) }
}
