package com.csir.myapplication;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class FindLocation extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

        protected static final String TAG = "MOVER_LOCATION_SERVICE";
        private GoogleMap mMap;
        private GregorianCalendar calendar;
        private boolean mapReady;
        /**
         * Provides the entry point to Google Play services.
         */
        protected GoogleApiClient mGoogleApiClient;
        /**
         * Represents a geographical location.
         */
        protected Location mCurrentLocation;
        private Location mPreviousLocation;
        protected String mLatitudeLabel, mLongitudeLabel,mUpdateTimeLabel,mSpeedLabel, mDistanceLabel;
        protected TextView mLatitudeText,mLongitudeText,mSpeedText, mDistanceText ;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_find_location);

            calendar = new GregorianCalendar();
            mapReady = false;

            /*DISPLAY LABELS AND TEXT BOXES*/
            mLatitudeLabel = getResources().getString(R.string.latitude_label);
            mLongitudeLabel = getResources().getString(R.string.longitude_label);
            mUpdateTimeLabel = getResources().getString(R.string.update_time_label);
            mSpeedLabel = getResources().getString(R.string.speed_label);
            mDistanceLabel = getResources().getString(R.string.distance_label);
            mDistanceText= (TextView) findViewById((R.id.distance_text));
            mLatitudeText = (TextView) findViewById((R.id.latitude_text));
            mLongitudeText = (TextView) findViewById((R.id.longitude_text));
            mLastUpdateTimeText = (TextView) findViewById((R.id.last_update_time));
            mSpeedText = (TextView) findViewById((R.id.speed_text));
            mRequestingLocationUpdates = true;

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

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

            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mCurrentLocation != null) {
                updateUI();
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
        mLocationRequest.setInterval(LOCATION_INTERVAL); //10 seconds
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    final private int LOCATION_INTERVAL = 10000; //10 seconds
    public double calculateSpeed(Double distance){

        return ((60000 / LOCATION_INTERVAL) * distance) * 60; //distance travelled in 1 minute * 60 = distance/hr
    }

    String mLastUpdateTime;
    @Override
    public void onLocationChanged(Location location) {
        mPreviousLocation = mCurrentLocation;
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        if (mapReady){
            updateMapLocation();
        }
    }
    TextView mLastUpdateTimeText;
    private void updateUI() {

        double distance = mPreviousLocation!=null?mCurrentLocation.distanceTo(mPreviousLocation):0.0;
        double speed = calculateSpeed(distance);
        mLatitudeText.setText(String.format("%s: %f", mLatitudeLabel,
                mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.format("%s: %f", mLongitudeLabel,
                mCurrentLocation.getLongitude()));
        mLastUpdateTimeText.setText(String.format("%s: %s", mUpdateTimeLabel,
                mLastUpdateTime));
        mSpeedText.setText(String.format("%s: %f", mSpeedLabel,
                speed));
        mDistanceText.setText(String.format("%s: %f", mDistanceLabel,
                distance));
    }

    /*GOOGLE MAPS*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.moveCamera(CameraUpdateFactory.zoomBy(15));
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {

            mMap.setMyLocationEnabled(true);
        }

        mapReady = true;

        // Add a marker in Sydney and move the camera

        updateMapLocation();
    }
    public void updateMapLocation(){
        LatLng location;

        if (mCurrentLocation != null){
            location = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        }else{
            location = new LatLng(-34, 151);
        }

        mMap.addMarker(new MarkerOptions()
                .position(location)
                .title(String.format("%s", new Date().getTime()))
                .alpha(0.5f)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
    }

}
