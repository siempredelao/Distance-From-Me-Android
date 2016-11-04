package gc.david.dfm.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import gc.david.dfm.map.LocationUtils;

public class GeofencingService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
                                                                GoogleApiClient.OnConnectionFailedListener,
                                                                LocationListener {

    public static final String GEOFENCE_RECEIVER_ACTION = "geofence.receiver.action";
    public static final String GEOFENCE_RECEIVER_LATITUDE_KEY = "geofence.receiver.latitude.key";
    public static final String GEOFENCE_RECEIVER_LONGITUDE_KEY = "geofence.receiver.longitude.key";

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    public GeofencingService() {
        super("GeofencingService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createGoogleApiClient();
        googleApiClient.connect();
        createLocationRequest();

        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // nothing
    }

    @Override
    public void onConnected(Bundle bundle) {
        final Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastKnownLocation != null) {
            sendUpdate(lastKnownLocation);
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // nothing
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // nothing
    }

    @Override
    public void onLocationChanged(Location location) {
        sendUpdate(location);
    }

    private synchronized void createGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                                                           .addConnectionCallbacks(this)
                                                           .addOnConnectionFailedListener(this)
                                                           .build();
    }

    private void stopLocationUpdates() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        // Set the interval ceiling to one minute
        locationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void sendUpdate(final Location location) {
        final Intent locationIntent = new Intent(GEOFENCE_RECEIVER_ACTION);
        locationIntent.putExtra(GEOFENCE_RECEIVER_LATITUDE_KEY, location.getLatitude());
        locationIntent.putExtra(GEOFENCE_RECEIVER_LONGITUDE_KEY, location.getLongitude());
        getApplicationContext().sendBroadcast(locationIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        googleApiClient.disconnect();
    }
}
