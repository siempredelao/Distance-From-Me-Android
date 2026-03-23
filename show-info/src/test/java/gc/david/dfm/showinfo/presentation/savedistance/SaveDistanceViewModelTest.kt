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

package gc.david.dfm.showinfo.presentation.savedistance

import gc.david.dfm.common.Coordinates
import gc.david.dfm.common.presentation.ResourceProvider
import gc.david.dfm.core.distances.domain.SaveDistanceUseCase
import gc.david.dfm.core.distances.domain.model.NewDistance
import gc.david.dfm.showinfo.R
import gc.david.dfm.testsupport.CoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class SaveDistanceViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutineExtension = CoroutineExtension()

    private val saveDistanceUseCase = mock<SaveDistanceUseCase>()
    private val resourceProvider = mock<ResourceProvider>()

    private val viewModel = SaveDistanceViewModel(saveDistanceUseCase, resourceProvider)

    @Test
    fun `onStart stores input parameters correctly`() {
        val positions = listOf(Coordinates(1.0, 2.0), Coordinates(3.0, 4.0))
        val distance = "10 km"

        viewModel.onStart(positions, distance)

        // No exception should be thrown
    }

    @Test
    fun `onSave with name success shows success message`() = runTest {
        val positions = listOf(Coordinates(1.0, 2.0))
        val distance = "10 km"
        val name = "My Distance"
        viewModel.onStart(positions, distance)

        whenever(saveDistanceUseCase(any())).thenReturn(Result.success(Unit))
        whenever(resourceProvider.get(R.string.alias_dialog_with_name_toast, name))
            .thenReturn("Saved as $name")

        viewModel.onSave(name)
        testScheduler.advanceUntilIdle()

        val message = viewModel.userMessage.value
        assertEquals("Saved as $name", message)
        verify(saveDistanceUseCase).invoke(any<NewDistance>())
    }

    @Test
    fun `onSave without name success shows success message`() = runTest {
        val positions = listOf(Coordinates(1.0, 2.0))
        val distance = "10 km"
        viewModel.onStart(positions, distance)

        whenever(saveDistanceUseCase(any())).thenReturn(Result.success(Unit))
        whenever(resourceProvider.get(R.string.alias_dialog_no_name_toast))
            .thenReturn("Saved without name")

        viewModel.onSave("")
        testScheduler.advanceUntilIdle()

        val message = viewModel.userMessage.value
        assertEquals("Saved without name", message)
    }

    @Test
    fun `onSave failure shows error message`() = runTest {
        val positions = listOf(Coordinates(1.0, 2.0))
        val distance = "10 km"
        viewModel.onStart(positions, distance)

        val exception = Exception("Database error")
        whenever(saveDistanceUseCase(any())).thenReturn(Result.failure(exception))
        whenever(resourceProvider.get(R.string.save_distance_error))
            .thenReturn("Error saving distance")

        viewModel.onSave("My Distance")
        testScheduler.advanceUntilIdle()

        val message = viewModel.userMessage.value
        assertEquals("Error saving distance", message)
    }

    @Test
    fun `onUserMessageShown clears message`() = runTest {
        val positions = listOf(Coordinates(1.0, 2.0))
        val distance = "10 km"
        viewModel.onStart(positions, distance)

        whenever(saveDistanceUseCase(any())).thenReturn(Result.success(Unit))
        whenever(resourceProvider.get(R.string.alias_dialog_no_name_toast))
            .thenReturn("Saved")

        viewModel.onSave("")
        testScheduler.advanceUntilIdle()
        assertEquals("Saved", viewModel.userMessage.value)

        viewModel.onUserMessageShown()

        assertNull(viewModel.userMessage.value)
    }
}


