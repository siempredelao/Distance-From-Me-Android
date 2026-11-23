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
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class ArchitectureKonsistTest {

    private val domain = Layer("Domain", "gc.david.dfm..domain..")
    private val data = Layer("Data", "gc.david.dfm..data..")
    private val presentation = Layer("Presentation", "gc.david.dfm..presentation..")

    @Test
    fun `domain layer does not depend on the data or presentation layers`() {
        Konsist.scopeFromProduction().assertArchitecture {
            domain.doesNotDependOn(data, presentation)
        }
    }

    @Test
    fun `data layer does not depend on the presentation layer`() {
        Konsist.scopeFromProduction().assertArchitecture {
            data.doesNotDependOn(presentation)
        }
    }

    @Test
    fun `presentation layer does not depend on the data layer`() {
        Konsist.scopeFromProduction().assertArchitecture {
            presentation.doesNotDependOn(data)
        }
    }

    @Test
    fun `domain layer does not use the Android framework`() {
        Konsist.scopeFromProduction()
            .slice { it.hasPackage("..domain..") }
            .imports
            .assertFalse { it.name.startsWith("android.") || it.name.startsWith("androidx.") }
    }

    @Test
    fun `packages match their directories`() {
        Konsist.scopeFromProject()
            .packages
            .assertTrue { it.hasMatchingPath }
    }
}
