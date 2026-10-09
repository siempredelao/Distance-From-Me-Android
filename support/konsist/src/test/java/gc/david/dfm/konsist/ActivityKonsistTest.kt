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
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class ActivityKonsistTest {

    private val activities = Konsist.scopeFromProduction()
        .classes()
        .withParentNamed("ComponentActivity", "AppCompatActivity", "FragmentActivity")

    @Test
    fun `activities are named with the Activity suffix`() {
        activities.assertTrue { it.name.endsWith("Activity") }
    }

    @Test
    fun `activities reside in a presentation package`() {
        activities.assertTrue { it.resideInPackage("..presentation..") }
    }

    @Test
    fun `activities are launched through a companion open function`() {
        activities
            // The launcher activity and the map it hands over to aren't opened from another screen.
            .withoutName("OnboardActivity", "MainActivity")
            .assertTrue { activity ->
                activity.objects(includeNested = false)
                    .any { it.hasCompanionModifier && it.hasFunctionWithName("open") }
            }
    }
}
