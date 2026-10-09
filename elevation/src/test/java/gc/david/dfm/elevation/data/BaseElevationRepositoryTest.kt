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

package gc.david.dfm.elevation.data

import gc.david.dfm.elevation.data.mapper.ElevationEntityDataMapper
import gc.david.dfm.elevation.data.model.ElevationEntity
import gc.david.dfm.elevation.domain.ElevationResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

class BaseElevationRepositoryTest {

    private val remoteDataSource = mock<ElevationRemoteDataSource>()
    private val mapper = mock<ElevationEntityDataMapper>()
    private val repository = BaseElevationRepository(remoteDataSource, mapper)

    @Test
    fun `getElevation returns the mapped remote response`() = runTest {
        val entity = ElevationEntity()
        val mapped = ElevationResult.Found(listOf(10.5, 20.3))
        whenever(remoteDataSource.getElevation(PATH, MAX_SAMPLES)).thenReturn(entity)
        whenever(mapper.transform(entity)).thenReturn(mapped)

        val result = repository.getElevation(PATH, MAX_SAMPLES)

        assertEquals(mapped, result)
    }

    @Test
    fun `getElevation propagates remote data source failures`() = runTest {
        val exception = IOException("Network error")
        whenever(remoteDataSource.getElevation(PATH, MAX_SAMPLES)).thenAnswer { throw exception }

        val thrown = runCatching { repository.getElevation(PATH, MAX_SAMPLES) }.exceptionOrNull()

        assertSame(exception, thrown)
    }

    private companion object {

        const val PATH = "40.7128,-74.006|40.7589,-73.9851"
        const val MAX_SAMPLES = 100
    }
}
