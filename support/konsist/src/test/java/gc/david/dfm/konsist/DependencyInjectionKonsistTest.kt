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
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class DependencyInjectionKonsistTest {

    private val productionScope = Konsist.scopeFromProduction()

    private val koinModules = productionScope
        .properties(includeNested = false)
        .filter { it.isTopLevel && it.value.orEmpty().matches(KOIN_MODULE_DECLARATION) }

    @Test
    fun `Koin modules are named with the Module suffix`() {
        koinModules.assertTrue { it.name.endsWith("Module") }
    }

    @Test
    fun `Koin modules are registered in startKoin`() {
        val startKoin = productionScope.classes()
            .withName("DFMApplication")
            .single()
            .functions()
            .withName("onCreate")
            .single()
            .text

        koinModules.assertTrue { Regex("""\b${it.name}\b""").containsMatchIn(startKoin) }
    }

    private companion object {

        val KOIN_MODULE_DECLARATION = Regex("""^module\s*\{.*""", RegexOption.DOT_MATCHES_ALL)
    }
}
