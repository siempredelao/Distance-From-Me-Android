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

package gc.david.dfm.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import gc.david.dfm.PermissionChecker
import gc.david.dfm.service.GeofencingService

class GeofencingLocationManager(
    private val context: Context,
    private val permissionChecker: PermissionChecker,
) : DefaultLifecycleObserver {

    private var onLocationChanged: ((Location) -> Unit)? = null

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val latitude = intent.getDoubleExtra(GeofencingService.GEOFENCE_RECEIVER_LATITUDE_KEY, 0.0)
            val longitude = intent.getDoubleExtra(GeofencingService.GEOFENCE_RECEIVER_LONGITUDE_KEY, 0.0)
            val location = Location("").apply {
                this.latitude = latitude
                this.longitude = longitude
            }
            onLocationChanged?.invoke(location)
        }
    }

    fun setOnLocationChangedListener(listener: (Location) -> Unit) {
        onLocationChanged = listener
    }

    override fun onStart(owner: LifecycleOwner) {
        if (permissionChecker.isLocationPermissionGranted()) {
            launchService()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        try {
            context.unregisterReceiver(locationReceiver)
        } catch (_: IllegalArgumentException) {
            // Receiver was not registered, nothing to do
        }
        context.stopService(Intent(context, GeofencingService::class.java))
    }

    fun startAfterPermissionGranted() {
        launchService()
    }

    private fun launchService() {
        ContextCompat.registerReceiver(
            context,
            locationReceiver,
            IntentFilter(GeofencingService.GEOFENCE_RECEIVER_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        context.startService(Intent(context, GeofencingService::class.java))
    }
}
