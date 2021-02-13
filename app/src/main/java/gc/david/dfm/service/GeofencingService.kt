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

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.core.os.bundleOf

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices

import gc.david.dfm.map.LocationUtils

class GeofencingService :
        IntentService("GeofencingService"),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private var googleApiClient: GoogleApiClient? = null
    private var locationRequest: LocationRequest? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createGoogleApiClient()
        googleApiClient?.connect()
        createLocationRequest()

        return Service.START_STICKY
    }

    override fun onHandleIntent(intent: Intent?) {
        // nothing
    }

    override fun onConnected(bundle: Bundle?) {
        val lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
        if (lastKnownLocation != null) {
            sendUpdate(lastKnownLocation)
        }

        startLocationUpdates()
    }

    override fun onConnectionSuspended(cause: Int) {
        // nothing
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // nothing
    }

    override fun onLocationChanged(location: Location) {
        sendUpdate(location)
    }

    @Synchronized
    private fun createGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
    }

    private fun stopLocationUpdates() {
        if (googleApiClient?.isConnected == true) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS
            // Set the interval ceiling to one minute
            fastestInterval = LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS
        }
    }

    private fun startLocationUpdates() {
        val googleApiClient = googleApiClient ?: return
        val locationRequest = locationRequest ?: return
        if (googleApiClient.isConnected) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this)
        }
    }

    private fun sendUpdate(location: Location) {
        val locationIntent = Intent(GEOFENCE_RECEIVER_ACTION).apply {
            val bundle =
                    bundleOf(
                            GEOFENCE_RECEIVER_LATITUDE_KEY to location.latitude,
                            GEOFENCE_RECEIVER_LONGITUDE_KEY to location.longitude
                    )
            putExtras(bundle)
        }
        applicationContext.sendBroadcast(locationIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
        googleApiClient?.disconnect()
    }

    companion object {

        const val GEOFENCE_RECEIVER_ACTION = "geofence.receiver.action"
        const val GEOFENCE_RECEIVER_LATITUDE_KEY = "geofence.receiver.latitude.key"
        const val GEOFENCE_RECEIVER_LONGITUDE_KEY = "geofence.receiver.longitude.key"
    }
}
