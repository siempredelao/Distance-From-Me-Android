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
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class UseCaseKonsistTest {

    private val useCases = Konsist.scopeFromProduction()
        .classes()
        .withNameEndingWith("UseCase")

    @Test
    fun `use cases reside in a domain package`() {
        useCases.assertTrue { it.resideInPackage("..domain..") }
    }

    @Test
    fun `use cases expose a single public operator invoke function`() {
        useCases.assertTrue { useCase ->
            val publicFunctions = useCase.functions(includeNested = false, includeLocal = false)
                .filter { it.hasPublicOrDefaultModifier }
            publicFunctions.singleOrNull()?.let { it.name == "invoke" && it.hasOperatorModifier } == true
        }
    }

    @Test
    fun `suspending use cases return a Result and streaming ones a Flow`() {
        useCases.assertTrue { useCase ->
            val invoke = useCase.functions(includeNested = false, includeLocal = false)
                .firstOrNull { it.name == "invoke" }
                ?: return@assertTrue false
            val returnType = invoke.returnType?.bareSourceType
            if (invoke.hasSuspendModifier) returnType == "Result" else returnType == "Flow"
        }
    }
}
