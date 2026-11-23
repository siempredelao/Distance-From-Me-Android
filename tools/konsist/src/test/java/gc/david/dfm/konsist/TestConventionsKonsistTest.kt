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
import com.lemonappdev.konsist.api.declaration.KoClassDeclaration
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.verify.assertFalse
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class TestConventionsKonsistTest {

    private val productionClasses = Konsist.scopeFromProduction().classes()
    private val testClasses = Konsist.scopeFromTest().classes()
    private val testClassNames = testClasses.map { it.name }.toSet()

    @Test
    fun `use cases have a test class`() {
        productionClasses.withNameEndingWith("UseCase").assertTrue { it.hasTestClassByName() }
    }

    @Test
    fun `repositories have a test class`() {
        productionClasses.withNameEndingWith("Repository").assertTrue { it.hasTestClassByName() }
    }

    @Test
    fun `mappers have a test class`() {
        productionClasses.withNameEndingWith("Mapper").assertTrue { it.hasTestClassByName() }
    }

    @Test
    fun `formatters have a test class`() {
        productionClasses.withNameEndingWith("Formatter").assertTrue { it.hasTestClassByName() }
    }

    @Test
    fun `view models have a test class`() {
        productionClasses.withNameEndingWith("ViewModel").assertTrue { it.hasTestClassByName() }
    }

    @Test
    fun `tests use JUnit 5 instead of JUnit 4`() {
        Konsist.scopeFromTest()
            .imports
            .assertFalse { it.name.startsWith("org.junit.") && !it.name.startsWith("org.junit.jupiter.") }
    }

    @Test
    fun `view model tests register the coroutine extension`() {
        testClasses.withNameEndingWith("ViewModelTest").assertTrue { test ->
            test.hasProperty { it.hasAnnotationWithName("RegisterExtension") && it.text.contains("CoroutineExtension") }
        }
    }

    private fun KoClassDeclaration.hasTestClassByName() = "${name}Test" in testClassNames
}
