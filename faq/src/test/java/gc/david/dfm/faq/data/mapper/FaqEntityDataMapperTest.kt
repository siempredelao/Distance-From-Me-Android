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

package gc.david.dfm.faq.data.mapper

import gc.david.dfm.faq.data.model.FaqEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FaqEntityDataMapperTest {

    private val mapper = FaqEntityDataMapper()

    @Test
    fun `transform maps entity to domain model correctly`() {
        val entity = FaqEntity(
            question = "What is this app?",
            answer = "This is a distance calculator app."
        )

        val result = mapper.transform(entity)

        assertEquals("What is this app?", result.question)
        assertEquals("This is a distance calculator app.", result.answer)
    }

    @Test
    fun `transform handles empty strings`() {
        val entity = FaqEntity(question = "", answer = "")

        val result = mapper.transform(entity)

        assertEquals("", result.question)
        assertEquals("", result.answer)
    }

    @Test
    fun `transform handles special characters`() {
        val entity = FaqEntity(
            question = "¿Cómo funciona?",
            answer = "Es muy fácil: 1) Marca un punto 2) Marca otro punto"
        )

        val result = mapper.transform(entity)

        assertEquals("¿Cómo funciona?", result.question)
        assertEquals("Es muy fácil: 1) Marca un punto 2) Marca otro punto", result.answer)
    }
}

