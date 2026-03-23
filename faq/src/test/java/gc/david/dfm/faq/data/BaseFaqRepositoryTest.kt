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

package gc.david.dfm.faq.data

import gc.david.dfm.faq.data.mapper.FaqEntityDataMapper
import gc.david.dfm.faq.data.model.FaqEntity
import gc.david.dfm.faq.domain.model.Faq
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class BaseFaqRepositoryTest {

    private val diskDataSource = mock<FaqDiskDataSource>()
    private val mapper = FaqEntityDataMapper()

    private val repository = BaseFaqRepository(diskDataSource, mapper)

    @Test
    fun `getFaqs returns mapped set of faqs`() = runTest {
        val entities = setOf(
            FaqEntity("Question 1", "Answer 1"),
            FaqEntity("Question 2", "Answer 2")
        )
        whenever(diskDataSource.getFaqs()).thenReturn(entities)

        val result = repository.getFaqs()

        assertEquals(2, result.size)
        assertTrue(result.contains(Faq("Question 1", "Answer 1")))
        assertTrue(result.contains(Faq("Question 2", "Answer 2")))
    }

    @Test
    fun `getFaqs returns empty set when no faqs available`() = runTest {
        whenever(diskDataSource.getFaqs()).thenReturn(emptySet())

        val result = repository.getFaqs()

        assertEquals(0, result.size)
    }

    @Test
    fun `getFaqs removes duplicates by converting to set`() = runTest {
        val entities = setOf(
            FaqEntity("Question 1", "Answer 1"),
            FaqEntity("Question 2", "Answer 2")
        )
        whenever(diskDataSource.getFaqs()).thenReturn(entities)

        val result = repository.getFaqs()

        assertEquals(2, result.size)
    }
}


