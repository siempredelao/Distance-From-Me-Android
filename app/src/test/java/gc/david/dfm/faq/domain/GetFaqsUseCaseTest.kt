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

package gc.david.dfm.faq.domain

import gc.david.dfm.faq.data.model.Faq
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class GetFaqsUseCaseTest {

    private val repository = mock<FaqRepository>()

    private val useCase = GetFaqsUseCase(repository)

    @Test
    fun `returns faqs set on success`() = runTest {
        val faqsSet = emptySet<Faq>()
        whenever(repository.getFaqs()).thenReturn(faqsSet)

        val result = useCase.invoke()

        assertEquals(Result.success(faqsSet), result)
    }

    @Test
    fun `returns error message on failure`() = runTest {
        val throwable = Throwable()
        whenever(repository.getFaqs()).thenAnswer { throw throwable }

        val result = useCase.invoke()

        assertEquals(Result.failure<Set<Faq>>(throwable), result)
    }
}