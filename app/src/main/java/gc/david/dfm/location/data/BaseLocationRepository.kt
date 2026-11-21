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

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import gc.david.dfm.common.Coordinates
import gc.david.dfm.location.domain.LocationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import java.util.concurrent.Executor

class BaseLocationRepository(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationRepository {

    // The permission is checked by catching SecurityException, which also covers a revocation while collecting
    @SuppressLint("MissingPermission")
    override fun observeLocation(): Flow<Coordinates> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it.toCoordinates()) }
            }
        }

        try {
            fusedLocationClient
                .lastLocation
                .addOnSuccessListener(DIRECT_EXECUTOR) { location ->
                    location?.let { trySend(it.toCoordinates()) }
                }
                .addOnFailureListener(DIRECT_EXECUTOR) {
                    Timber.tag(TAG).e(it, "Could not get the last location")
                }

            fusedLocationClient.requestLocationUpdates(locationRequest(), DIRECT_EXECUTOR, callback)
        } catch (e: SecurityException) {
            Timber.tag(TAG).w(e, "Location permission missing")
            close()
        }

        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }.distinctUntilChanged()

    private fun locationRequest() =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MILLIS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MILLIS)
            .build()

    private fun Location.toCoordinates() = Coordinates(latitude, longitude)

    private companion object {

        const val TAG = "BaseLocationRepository"
        const val UPDATE_INTERVAL_MILLIS = 5_000L
        const val MIN_UPDATE_INTERVAL_MILLIS = 1_000L

        // trySend is thread-safe, so results can be handled on whichever thread delivers them
        val DIRECT_EXECUTOR = Executor(Runnable::run)
    }
}
