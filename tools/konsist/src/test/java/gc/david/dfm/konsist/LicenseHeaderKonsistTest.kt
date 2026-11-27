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
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class LicenseHeaderKonsistTest {

    @Test
    fun `every Kotlin file starts with the Apache license header`() {
        Konsist.scopeFromProject()
            .files
            .assertTrue { LICENSE_HEADER.containsMatchIn(it.text) }
    }

    private companion object {

        // Any year is accepted so files written in earlier years keep passing.
        val LICENSE_HEADER = Regex(
            """
            |^/\*
            | \* Copyright \(c\) \d{4} David Aguiar Gonzalez
            | \*
            | \* Licensed under the Apache License, Version 2\.0 \(the "License"\);
            """.trimMargin()
        )
    }
}
