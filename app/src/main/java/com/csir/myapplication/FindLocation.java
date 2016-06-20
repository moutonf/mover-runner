package com.csir.myapplication;

import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import java.text.DateFormat;
import java.util.Date;

public class FindLocation extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

        protected static final String TAG = "MOVER_LOCATION_SERVICE";


        /**
         * Provides the entry point to Google Play services.
         */
        protected GoogleApiClient mGoogleApiClient;

        /**
         * Represents a geographical location.
         */
        protected Location mLastLocation;

        protected String mLatitudeLabel;
        protected String mLongitudeLabel;
        protected TextView mLatitudeText;
        protected TextView mLongitudeText;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_find_location);

            mLatitudeLabel = getResources().getString(R.string.latitude_label);
            mLongitudeLabel = getResources().getString(R.string.longitude_label);
            mLatitudeText = (TextView) findViewById((R.id.latitude_text));
            mLongitudeText = (TextView) findViewById((R.id.longitude_text));
            mLastUpdateTimeText = (TextView) findViewById((R.id.last_update_time));
            mRequestingLocationUpdates = true;

            buildGoogleApiClient();
        }

        /**
         * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
         */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    Boolean mRequestingLocationUpdates;
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        try{
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                mLatitudeText.setText(String.format("%s: %f", mLatitudeLabel,
                        mLastLocation.getLatitude()));
                mLongitudeText.setText(String.format("%s: %f", mLongitudeLabel,
                        mLastLocation.getLongitude()));
            } else {
                Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();


            }

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }catch(SecurityException e){
            Log.e(TAG,"User doesn't have location permission");
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();

        }

    }

    protected void startLocationUpdates() {
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }catch(SecurityException e){
            Log.e(TAG,"User doesn't have location permission");
            Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /*SET UP INTERVAL LOCATION UPDATES*/
    private LocationRequest mLocationRequest;
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    String mLastUpdateTime;
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }
    TextView mLastUpdateTimeText;
    private void updateUI() {
        mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
        mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        mLastUpdateTimeText.setText(mLastUpdateTime);
    }


}
