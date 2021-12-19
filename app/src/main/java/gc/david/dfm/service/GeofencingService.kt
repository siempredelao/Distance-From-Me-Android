/*
 * Copyright (c) 2021 David Aguiar Gonzalez
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

package gc.david.dfm.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.os.bundleOf
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import gc.david.dfm.map.LocationUtils
import timber.log.Timber

@SuppressLint("MissingPermission")
class GeofencingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var previousLatLng = LatLng(-180.0, -180.0)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopLocationUpdates()
        return super.onUnbind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                sendUpdate(locationResult.lastLocation)
            }
        }

        fusedLocationClient
            .lastLocation
            .addOnSuccessListener {
                    lastKnownLocation -> sendUpdate(lastKnownLocation)
            }
            .addOnFailureListener {
                Timber.tag(TAG).e("Error trying to get last GPS location")
            }

        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS
            // Set the interval ceiling to one minute
            fastestInterval = LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS
            fusedLocationClient
                .requestLocationUpdates(this, locationCallback!!, Looper.getMainLooper())
        }

        return START_STICKY
    }

    override fun onDestroy() {
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    private fun sendUpdate(location: Location) {
        if (!isLocationUpdate(location.toLatLng(), previousLatLng)) return

        val locationIntent = Intent(GEOFENCE_RECEIVER_ACTION).apply {
            val bundle =
                bundleOf(
                    GEOFENCE_RECEIVER_LATITUDE_KEY to location.latitude,
                    GEOFENCE_RECEIVER_LONGITUDE_KEY to location.longitude
                )
            putExtras(bundle)
        }
        applicationContext.sendBroadcast(locationIntent)
        previousLatLng = location.toLatLng()
    }

    private fun isLocationUpdate(location: LatLng, previousLatLng: LatLng) =
        location.latitude != previousLatLng.latitude ||
                location.longitude != previousLatLng.longitude

    private fun Location.toLatLng() = LatLng(this.latitude, this.longitude)

    companion object {

        const val GEOFENCE_RECEIVER_ACTION = "geofence.receiver.action"
        const val GEOFENCE_RECEIVER_LATITUDE_KEY = "geofence.receiver.latitude.key"
        const val GEOFENCE_RECEIVER_LONGITUDE_KEY = "geofence.receiver.longitude.key"

        private const val TAG = "GeofencingService"
    }
}
