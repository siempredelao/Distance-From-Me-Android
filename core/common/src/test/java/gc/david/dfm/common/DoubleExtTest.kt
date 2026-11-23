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

package gc.david.dfm.common

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class DoubleExtTest {

    @ParameterizedTest
    @CsvSource(
        "0.0005, 0.00050",
        "-0.0001, -0.00010",
        "0.0, 0.0",
        "51.4778, 51.4778",
        "-180.0, -180.0",
        "1.0E7, 10000000",
    )
    fun `toPlainString never uses scientific notation`(value: Double, expected: String) {
        assertEquals(expected, value.toPlainString())
    }
}
