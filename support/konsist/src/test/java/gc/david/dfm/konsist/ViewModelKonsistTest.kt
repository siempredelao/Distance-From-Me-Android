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

package gc.david.dfm.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withParentNamed
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class ViewModelKonsistTest {

    private val viewModels = Konsist.scopeFromProduction()
        .classes()
        .withParentNamed("ViewModel")

    @Test
    fun `view models are named with the ViewModel suffix`() {
        viewModels.assertTrue { it.name.endsWith("ViewModel") }
    }

    @Test
    fun `view models reside in a presentation package`() {
        viewModels.assertTrue { it.resideInPackage("..presentation..") }
    }

    @Test
    fun `view models do not expose mutable flows`() {
        viewModels
            .flatMap { it.properties(includeNested = false) }
            .filter { it.hasPublicOrDefaultModifier }
            .assertFalse { property ->
                val declaredType = property.type?.bareSourceType ?: property.value.orEmpty()
                declaredType.startsWith("Mutable")
            }
    }
}
