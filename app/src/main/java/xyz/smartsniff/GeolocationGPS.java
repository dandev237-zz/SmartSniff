package xyz.smartsniff;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;


/**
 * Implementation of a geolocation method using Google Play Services API.
 *
 * <p/>
 * Author: Daniel Castro García
 * Email: dandev237@gmail.com
 * Date: 16/05/2016
 */
public class GeolocationGPS implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private static final String TAG = "GeolocationGPS";
    private static final int REQUEST_CHECK_SETTINGS = 1113;

    private GoogleApiClient googleApiClient;
    private Activity mainActivity;
    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder builder;
    private SharedPreferences preferences;

    private double latitude, longitude;
    private boolean requestingLocationUpdates;


    public GeolocationGPS(Context appContext, Activity mainActivity) {

        this.mainActivity = mainActivity;

        preferences = appContext.getSharedPreferences(Utils.PREFS_NAME, Context.MODE_PRIVATE);

        googleApiClient = new GoogleApiClient.Builder(appContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        builder = new LocationSettingsRequest.Builder();
        requestingLocationUpdates = true;
    }

        public void connect() {
        googleApiClient.connect();
    }


    public void disconnect() {
        if (googleApiClient.isConnected()) {
            stopLocationUpdates();
            googleApiClient.disconnect();
        }
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();
        int scanInterval = preferences.getInt(Utils.PREF_SCAN_INTERVAL, Utils.SCAN_INTERVAL_DEFAULT);
        locationRequest.setInterval(scanInterval);
        locationRequest.setFastestInterval(scanInterval - 1000);
        // Wifi-Internet by default
        locationRequest.setPriority(preferences.getInt(Utils.PREF_GPS_PRIORITY, Utils.GPS_PRIORITY_DEFAULT));

        builder.addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates states = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        //All location settings are satisfied
                        requestingLocationUpdates = true;
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //Location settings are not satisfied
                        try {
                            status.startResolutionForResult(mainActivity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            //Ignore the error
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        //Location settings are not satisfied, and we have no way to fix them.
                        break;
                }
            }
        });
    }

    protected void startLocationUpdates() throws SecurityException {
        //Permissions check at runtime
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection failed: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    //Getters

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}
