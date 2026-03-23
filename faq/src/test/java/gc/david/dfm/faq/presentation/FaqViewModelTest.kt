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

package gc.david.dfm.faq.presentation

import gc.david.dfm.testsupport.CoroutineExtension
import gc.david.dfm.faq.R
import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.faq.domain.GetFaqsUseCase
import gc.david.dfm.faq.domain.model.Faq
import gc.david.dfm.faq.presentation.model.FaqUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class FaqViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutineExtension = CoroutineExtension()

    private val useCase = mock<GetFaqsUseCase>()
    private val resourceProvider = mock<ResourceProvider>()

    private val viewModel = FaqViewModel(useCase, resourceProvider)


    @Test
    fun `onStart Given use case succeeds Then returns Content with FAQs`() = runTest {
        val faq = Faq("question", "answer")
        val faqSet = setOf(faq)
        whenever(useCase()).thenReturn(Result.success(faqSet))

        viewModel.onStart()

        verify(useCase)()
        val state = viewModel.uiState.value
        assertTrue(state is FaqUiState.Content)
        assertEquals(faqSet.toList(), (state as FaqUiState.Content).faqs)
    }

    @Test
    fun `onStart Given use case fails Then returns Error with message`() = runTest {
        whenever(useCase()).thenReturn(Result.failure(Throwable()))
        val errorMessage = "error message"
        whenever(resourceProvider.get(R.string.faq_error_message)).thenReturn(errorMessage)

        viewModel.onStart()

        verify(useCase)()
        val state = viewModel.uiState.value
        assertTrue(state is FaqUiState.Error)
        assertEquals(errorMessage, (state as FaqUiState.Error).message)
    }
}
