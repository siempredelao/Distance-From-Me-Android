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
import com.lemonappdev.konsist.api.ext.list.withPackage
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
    fun `presentation models do not hold domain models`() {
        Konsist.scopeFromProduction()
            .files
            .withPackage("gc.david.dfm..presentation.model..")
            .assertTrue { file ->
                val domainModels = file.imports.map { it.name }.filter { DOMAIN_MODEL.matches(it) }
                DOMAIN_MODELS_ALLOWED_IN[file.name].orEmpty().containsAll(domainModels)
            }
    }

    @Test
    fun `packages match their directories`() {
        Konsist.scopeFromProject()
            .packages
            .assertTrue { it.hasMatchingPath }
    }

    private companion object {

        val DOMAIN_MODEL = Regex("gc\\.david\\.dfm\\..*domain\\.model\\..*")

        // The known exceptions. An enum is stable wherever it comes from, but a class such as Coordinates isn't,
        // because its module doesn't run the Compose compiler
        val DOMAIN_MODELS_ALLOWED_IN = mapOf(
            // The input of MapStateMapper
            "DrawDistanceUiModel" to setOf(
                "gc.david.dfm.core.coordinates.domain.model.Coordinates",
                "gc.david.dfm.core.settings.domain.model.CameraAnimation",
                "gc.david.dfm.distance.domain.model.DistanceMode",
            ),
            // triggerElevationUpdate, which MainActivity hands over to ElevationViewModel
            "MainUiState" to setOf(
                "gc.david.dfm.core.coordinates.domain.model.Coordinates",
                "gc.david.dfm.distance.domain.model.DistanceMode",
            ),
            // SaveDialogData, which ShowInfoActivity hands over to SaveDistanceViewModel. It keeps the state unstable
            "ShowInfoUiState" to setOf("gc.david.dfm.core.coordinates.domain.model.Coordinates"),
        )
    }
}
