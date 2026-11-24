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
import com.lemonappdev.konsist.api.ext.list.properties
import com.lemonappdev.konsist.api.ext.list.withAnnotationNamed
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.jupiter.api.Test

/**
 * Keeps composable parameters stable. A List, Set or Map may be mutable, so Compose treats whatever holds one as
 * unstable: use ImmutableList, ImmutableSet or ImmutableMap from kotlinx-collections-immutable instead. A var makes its
 * class unstable too, and Compose doesn't notice when that class is a subclass of a sealed one.
 */
class ComposeStabilityKonsistTest {

    @Test
    fun `composable parameters use immutable collections`() {
        Konsist.scopeFromProduction()
            .functions()
            .withAnnotationNamed("Composable")
            .flatMap { it.parameters }
            .assertFalse { MUTABLE_COLLECTION.containsMatchIn(it.type.text) }
    }

    @Test
    fun `presentation models use immutable collections`() {
        Konsist.scopeFromProduction()
            .files
            .withPackage("gc.david.dfm..presentation.model..")
            .flatMap { file -> file.classes().properties() }
            .filterNot { it.name in ALLOWED_PROPERTIES }
            .assertFalse { property -> property.type?.let { MUTABLE_COLLECTION.containsMatchIn(it.text) } ?: false }
    }

    @Test
    fun `presentation models have no var`() {
        Konsist.scopeFromProduction()
            .files
            .withPackage("gc.david.dfm..presentation.model..")
            .flatMap { file -> file.classes().properties() }
            .assertFalse { it.isVar }
    }

    private companion object {

        // The word boundary keeps ImmutableList and PersistentList out
        val MUTABLE_COLLECTION = Regex("\\b(Mutable)?(Collection|List|Set|Map)<")

        val ALLOWED_PROPERTIES = setOf(
            // DrawDistanceUiModel, the input of MapStateMapper
            "positionList",
            // MainUiState, which MainActivity hands over to ElevationViewModel
            "triggerElevationUpdate",
            // SaveDialogData, which ShowInfoActivity hands over to SaveDistanceViewModel
            "positionsList",
        )
    }
}
