package com.csir.runner;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
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

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RunnerMan extends FragmentActivity implements SensorDisplayFragment.OnSensorFragmentInteractionListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "MOVER_LOCATION_SERVICE";
    private Locale deviceLocale;
    LoggingService log;

    /*LOCATION*/
    private GoogleMap mMap;
    private boolean mapReady;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    private Location mPreviousLocation;
    Boolean mRequestingLocationUpdates;
    private LocationRequest mLocationRequest;
    final private int LOCATION_INTERVAL = 2000; //2 seconds

    float transparency = 0.5f;
    LatLng location;

    /*Intent Extras*/

    private String userID;
    boolean start, firstUpdate;
    double distance, speed;
    FragmentManager fm;
    private CountDownTimer timer;

    @BindView(R.id.distance_text) TextView mDistanceText;
    @BindView(R.id.latitude_text) TextView mLatitudeText;
    @BindView(R.id.longitude_text) TextView mLongitudeText;
    @BindView(R.id.speed_text) TextView mSpeedText;
    @BindView(R.id.last_update_time) TextView mLastUpdateTimeText;
    @BindView(R.id.runner_display) LinearLayout runnerDisplay;
    @BindView(R.id.countdown_display) RelativeLayout countdownDisplay;
    @BindView(R.id.countdown_text) TextView countdownText;

    @BindString(R.string.latitude_label) String mLatitudeLabel;
    @BindString(R.string.longitude_label) String mLongitudeLabel;
    @BindString(R.string.update_time_label) String mUpdateTimeLabel;
    @BindString(R.string.speed_label) String mSpeedLabel;
    @BindString(R.string.distance_label) String mDistanceLabel;

    @BindView(R.id.cancel_accident) Button cancelAccident;

     /*DISPLAY VARIABLES*/
    protected String mLastUpdateTime;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_runner_man);

            Intent intent = getIntent();
            userID = intent.getStringExtra(getString(R.string.USER_ID_EXTRA));
            username = intent.getStringExtra(getString(R.string.USERNAME_EXTRA));

            mapReady = false;
            mRequestingLocationUpdates = true;
            fm = getSupportFragmentManager();
            this.findViewById(android.R.id.content).setKeepScreenOn(true);

            ButterKnife.bind(this);
            log = new LoggingService(this, "LOCATION", null);

            countdownDisplay.setBackgroundColor(Color.rgb(255, 117, 151));
            countdownDisplay.setVisibility(RelativeLayout.GONE);
            cancelAccident.setBackgroundColor(Color.LTGRAY);
            countdownText.setTextColor(Color.WHITE);

            deviceLocale = Locale.getDefault();

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            buildGoogleApiClient();

        }

    public String getUserID(){
        return userID;
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
        mLocationRequest.setInterval(LOCATION_INTERVAL); //2 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /*A rough estimate of speed*/
    public double calculateSpeed(Double distance){
        return ((60000 / LOCATION_INTERVAL) * distance) * 60; //distance travelled in 1 minute * 60 = distance/hr
    }

    @Override
    public void onLocationChanged(Location location) {
        /*Need the previous location for distance and route calculations*/
        if (mCurrentLocation == null ){
            start = true;
        }
        mPreviousLocation = mCurrentLocation;
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        log.writeLog(TAG,String.format("lat,%f,long,%f,alt,%f",location.getLatitude(),location.getLongitude(),location.getAltitude()));

        updateUI();
        if (mapReady){
            updateMapLocation();
        }
    }
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
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            mMap.setMyLocationEnabled(true);
            mapReady = true;
            firstUpdate = true;
        }
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

        if (firstUpdate){
            /*zoom to first location*/
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        }else{
            /*maintain the zoom level*/
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, mMap.getCameraPosition().zoom));
        }
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

    public void showCountDownTimer(){
        runnerDisplay.setVisibility(LinearLayout.GONE);
        countdownDisplay.setVisibility(LinearLayout.VISIBLE);

        timer = new CountDownTimer(8000, 100) {

            public void onTick(long millisUntilFinished) {
                countdownText.setText( String.valueOf(millisUntilFinished / 1000.0));
            }

            public void onFinish() {
                SensorDisplayFragment fragment = (SensorDisplayFragment) fm.findFragmentById(R.id.sensor_display_fragment);
                fragment.sendAccident();
                cancelAccident(null);
            }
        }.start();
    }

    public void cancelAccident(View view)
    {
        /*stop the timer and show the display screen again */
        countdownDisplay.setVisibility(LinearLayout.GONE);
        timer.cancel();
        runnerDisplay.setVisibility(LinearLayout.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /*Location updates are stopped when activity is stopped*/
        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy(){
        log.closeLogFile();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
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
}
