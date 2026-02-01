/*
 * Copyright (c) 2025 David Aguiar Gonzalez
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

package gc.david.dfm.faq

import gc.david.dfm.faq.data.BaseFaqRepository
import gc.david.dfm.faq.data.FaqDiskDataSource
import gc.david.dfm.faq.domain.FaqRepository
import gc.david.dfm.faq.domain.GetFaqsUseCase
import gc.david.dfm.faq.presentation.viewmodel.FaqViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val faqModule = module {

    viewModel { FaqViewModel(get(), get()) }
    factory { GetFaqsUseCase(get()) }
    single<FaqRepository> { BaseFaqRepository(get()) }
    single { FaqDiskDataSource() }
}

