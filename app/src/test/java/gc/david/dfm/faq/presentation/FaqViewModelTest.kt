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

package gc.david.dfm.faq.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import gc.david.dfm.CoroutineDispatcherRule
import gc.david.dfm.R
import gc.david.dfm.ResourceProvider
import gc.david.dfm.faq.data.model.Faq
import gc.david.dfm.faq.domain.GetFaqsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class FaqViewModelTest {

    private val useCase = mock<GetFaqsUseCase>()
    private val resourceProvider = mock<ResourceProvider>()

    private val viewModel = FaqViewModel(useCase, resourceProvider)

    @get:Rule var instantTaskRule: TestRule = InstantTaskExecutorRule()
    @get:Rule val coroutinesDispatcherRule = CoroutineDispatcherRule()

    @Test
    fun `onStart Given use case succeeds Then returns FAQs`() = runTest {
        val faqSet = setOf(mock<Faq>())
        whenever(useCase()).thenReturn(Result.success(faqSet))

        viewModel.onStart()

        verify(useCase)()
        assertEquals(faqSet, viewModel.faqList.value)
    }

    @Test
    fun `onStart Given use case fails Then returns error message`() = runTest {
        whenever(useCase()).thenReturn(Result.failure(Throwable()))
        val errorMessage = "error message"
        whenever(resourceProvider.get(R.string.faq_error_message)).thenReturn(errorMessage)

        viewModel.onStart()

        verify(useCase)()
        assertEquals(errorMessage, viewModel.errorMessage.value)
    }
}