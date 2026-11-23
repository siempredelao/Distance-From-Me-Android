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
package gc.david.dfm.location.data

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import gc.david.dfm.core.coordinates.domain.model.Coordinates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Answers
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.Executor

@OptIn(ExperimentalCoroutinesApi::class)
class BaseLocationRepositoryTest {

    // Returns itself from addOn*Listener, as the real Task does, so the listeners can be chained
    private val lastLocationTask = mock<Task<Location>>(defaultAnswer = Answers.RETURNS_SELF)
    private val fusedLocationClient = mock<FusedLocationProviderClient> {
        on { lastLocation } doReturn lastLocationTask
        on { requestLocationUpdates(any(), any<Executor>(), any<LocationCallback>()) } doReturn mock<Task<Void>>()
    }
    private val repository = BaseLocationRepository(fusedLocationClient)

    @Test
    fun `emits the last known location`() = runTest {
        val results = collect()

        lastLocationListener().onSuccess(location(LONDON))

        assertEquals(listOf(LONDON), results)
    }

    @Test
    fun `emits location updates`() = runTest {
        val results = collect()

        locationCallback().onLocationResult(locationResult(LONDON))
        locationCallback().onLocationResult(locationResult(PARIS))

        assertEquals(listOf(LONDON, PARIS), results)
    }

    @Test
    fun `skips a missing last location`() = runTest {
        val results = collect()

        lastLocationListener().onSuccess(null)
        locationCallback().onLocationResult(locationResult(LONDON))

        assertEquals(listOf(LONDON), results)
    }

    @Test
    fun `skips an update without a location`() = runTest {
        val results = collect()

        locationCallback().onLocationResult(mock<LocationResult>())

        assertTrue(results.isEmpty())
    }

    @Test
    fun `drops a location equal to the previous one`() = runTest {
        val results = collect()

        lastLocationListener().onSuccess(location(LONDON))
        locationCallback().onLocationResult(locationResult(LONDON))

        assertEquals(listOf(LONDON), results)
    }

    @Test
    fun `stops location updates when collection stops`() = runTest {
        val collection = launch(UnconfinedTestDispatcher(testScheduler)) { repository.observeLocation().collect {} }
        val callback = locationCallback()

        collection.cancel()

        verify(fusedLocationClient).removeLocationUpdates(callback)
    }

    @Test
    fun `completes without emitting when location permission is missing`() = runTest {
        whenever(fusedLocationClient.requestLocationUpdates(any(), any<Executor>(), any<LocationCallback>()))
            .doThrow(SecurityException("Missing location permission"))

        val results = repository.observeLocation().toList()

        assertTrue(results.isEmpty())
    }

    private fun TestScope.collect(): List<Coordinates> {
        val results = mutableListOf<Coordinates>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { repository.observeLocation().toList(results) }
        return results
    }

    private fun lastLocationListener(): OnSuccessListener<Location> =
        argumentCaptor<OnSuccessListener<Location>>().apply {
            verify(lastLocationTask).addOnSuccessListener(any<Executor>(), capture())
        }.firstValue

    private fun locationCallback(): LocationCallback =
        argumentCaptor<LocationCallback>().apply {
            verify(fusedLocationClient).requestLocationUpdates(any(), any<Executor>(), capture())
        }.firstValue

    private fun locationResult(coordinates: Coordinates): LocationResult {
        val location = location(coordinates)
        return mock { on { lastLocation } doReturn location }
    }

    private fun location(coordinates: Coordinates) = mock<Location> {
        on { latitude } doReturn coordinates.latitude
        on { longitude } doReturn coordinates.longitude
    }

    private companion object {

        val LONDON = Coordinates(51.5074, -0.1278)
        val PARIS = Coordinates(48.8566, 2.3522)
    }
}
