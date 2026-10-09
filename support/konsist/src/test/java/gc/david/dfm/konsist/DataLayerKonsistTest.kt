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

class DataLayerKonsistTest {

    private val productionScope = Konsist.scopeFromProduction()

    @Test
    fun `repository interfaces reside in a domain package`() {
        productionScope.interfaces()
            .withNameEndingWith("Repository")
            .assertTrue { it.resideInPackage("..domain..") }
    }

    @Test
    fun `repository implementations reside in a data package`() {
        productionScope.classes()
            .withNameEndingWith("Repository")
            .assertTrue { it.resideInPackage("..data..") }
    }

    @Test
    fun `repository implementations are named Base followed by the interface they implement`() {
        productionScope.classes()
            .withNameEndingWith("Repository")
            .assertTrue { it.name.startsWith("Base") && it.hasParentWithName(it.name.removePrefix("Base")) }
    }

    @Test
    fun `data sources reside in a data package`() {
        productionScope.classes()
            .withNameEndingWith("DataSource")
            .assertTrue { it.resideInPackage("..data..") }
    }

    @Test
    fun `entity mappers reside in a data mapper package`() {
        productionScope.classes()
            .withNameEndingWith("EntityMapper", "EntityDataMapper")
            .assertTrue { it.resideInPackage("..data.mapper..") }
    }
}
