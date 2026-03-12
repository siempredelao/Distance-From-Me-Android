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

package gc.david.dfm.opensource.di

import gc.david.dfm.opensource.data.BaseOpenSourceRepository
import gc.david.dfm.opensource.data.OpenSourceDiskDataSource
import gc.david.dfm.opensource.domain.GetOpenSourceLibrariesUseCase
import gc.david.dfm.opensource.domain.OpenSourceRepository
import gc.david.dfm.opensource.presentation.mapper.LicenseMapper
import gc.david.dfm.opensource.presentation.OpenSourceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import gc.david.dfm.opensource.domain.OpenSourceLibraryMapper as OpenSourceLibraryDomainMapper
import gc.david.dfm.opensource.presentation.mapper.OpenSourceLibraryMapper as OpenSourceLibraryUiMapper

val openSourceModule = module {

    viewModel { OpenSourceViewModel(get(), get(), get()) }
    factory { GetOpenSourceLibrariesUseCase(get(), get()) }
    factory { OpenSourceLibraryUiMapper(get()) }
    factory { OpenSourceLibraryDomainMapper() }
    factory { LicenseMapper(get()) }
    single<OpenSourceRepository> { BaseOpenSourceRepository(get()) }
    single { OpenSourceDiskDataSource() }
}
