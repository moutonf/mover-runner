package com.csir.myapplication;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class RunnerMan extends FragmentActivity implements SensorDisplayFragment.OnSensorFragmentInteractionListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

        protected static final String TAG = "MOVER_LOCATION_SERVICE";
        private Locale deviceLocale;

        /*LOCATION*/
        private GoogleMap mMap;
        private boolean mapReady;
        protected GoogleApiClient mGoogleApiClient;
        protected Location mCurrentLocation;
        private Location mPreviousLocation;
        Boolean mRequestingLocationUpdates;
        private LocationRequest mLocationRequest;
        final private int LOCATION_INTERVAL = 10000; //10 seconds

    float transparency = 0.5f;
    LatLng location;

    /*DISPLAY VARIABLES*/
        protected String mLatitudeLabel, mLongitudeLabel,mUpdateTimeLabel,mSpeedLabel, mDistanceLabel,mLastUpdateTime;
        protected TextView mLatitudeText,mLongitudeText,mSpeedText, mDistanceText,mLastUpdateTimeText ;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_runner_man);

            mapReady = false;
            mRequestingLocationUpdates = true;

            this.findViewById(android.R.id.content).setKeepScreenOn(true);
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
            /****************/
            deviceLocale = Locale.getDefault();

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            buildGoogleApiClient();
        }
    public void onSensorFragmentInteraction(Uri uri){
        /*sensor shouldn't require any input from location, unless coordinates*/
    }

    public Location getLocation(){
        return mCurrentLocation;
    }

    /*SET UP INTERVAL LOCATION UPDATES*/
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_INTERVAL); //10 seconds
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /*A rough estimate of speed*/
    public double calculateSpeed(Double distance){

        return ((60000 / LOCATION_INTERVAL) * distance) * 60; //distance travelled in 1 minute * 60 = distance/hr
    }
    boolean start;
    @Override
    public void onLocationChanged(Location location) {
        /*Need the previous location for distance and route calculations*/
        if (mCurrentLocation == null ){
            start = true;
        }
        mPreviousLocation = mCurrentLocation;
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        if (mapReady){
            updateMapLocation();
        }
    }
    double distance, speed;
    /*Update the display headers*/
    private void updateUI() {
        distance = mPreviousLocation!=null?mCurrentLocation.distanceTo(mPreviousLocation):0.0;
        speed = calculateSpeed(distance);
        mLatitudeText.setText(String.format(deviceLocale,"%s: %f", mLatitudeLabel,
                mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.format(deviceLocale,"%s: %f", mLongitudeLabel,
                mCurrentLocation.getLongitude()));
        mLastUpdateTimeText.setText(String.format(deviceLocale,"%s: %s", mUpdateTimeLabel,
                mLastUpdateTime));
        mSpeedText.setText(String.format(deviceLocale,"%s: %f", mSpeedLabel,
                speed));
        mDistanceText.setText(String.format(deviceLocale,"%s: %f", mDistanceLabel,
                distance));
    }

    /*GOOGLE MAPS*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ) {
            mMap.setMyLocationEnabled(true);
        }
        mapReady = true;

    }
    /*The main map display and update method*/


    public void updateMapLocation(){
        location = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        //only add a marker if start of the trip
        if (start){
                    mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(String.format("Added: %s", new Date()))
                    .alpha(transparency)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            );
            start = false;
        }

        //draw the route
        if (mPreviousLocation != mCurrentLocation && mPreviousLocation != null){
            mMap.addPolyline(new PolylineOptions()
                    .add(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()),
                            new LatLng(mPreviousLocation.getLatitude(), mPreviousLocation.getLongitude()))
                    .width(3)
                    .color(Color.BLUE));
        }
        //maintain the zoom levels
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, mMap.getCameraPosition().zoom));
    }

    /*Boring Google Maps methods*/

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
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
            /*Location updates are stopped when paused*/
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
            /*Don't sustain connections*/
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
            /*Will drain battery unnecessarily*/
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    public void stop(View view)
    {
        finish();
    }




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

//    @Override
//    public boolean stopActivity(int keyCode, KeyEvent event)
//    {            finish();
//
//        if ((keyCode == KeyEvent.KEYCODE_BACK))
//        {
//        }
//        return super.onKeyDown(keyCode, event);
//    }


}
