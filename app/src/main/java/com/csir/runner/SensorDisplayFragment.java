package com.csir.runner;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorDisplayFragment.OnSensorFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SensorDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorDisplayFragment extends Fragment implements SensorEventListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private OnSensorFragmentInteractionListener mListener;

    LoggingService log;
    LoggingService accidentLog;

    private SensorManager mSensorManager;
    private static final String SENSOR_SERVICE_TAG = "MOV_SENSOR_SERVICE";
    private static final String TAG = "MOVER_SENSOR";

    private Sensor mAccelerometer;
    HashMap<String, TextView> sensorTextViews;
    private List<Sensor> deviceSensors;
    private ViewGroup viewGroup;
    private Requests requester;

    private LinearLayout.LayoutParams lparams;

    static final float LOW_PASS_ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.
    final float GRAVITY_ALPHA = 0.8f;

    Float maxX,maxY,maxZ;
    View view;
    RunnerMan activity;
    float [] filterValues;
    String sensorName;
    TextView sensor;
    DecimalFormat f = new DecimalFormat("0.000");
    float[] gravity;
    Double maxMagnitude;

    private String username;
    private String userID;

    List<Sensor> allSensors;
    private LinearLayout mFlParent;

    public SensorDisplayFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SensorDisplayFragment newInstance() {
        return new SensorDisplayFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sensor_display, container, false);
        mFlParent  = (LinearLayout) view.findViewById(R.id.fl_frag_sensor_display_parent);
        return view;
    }
    /*Enforces the interface*/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSensorFragmentInteractionListener) {
            mListener = (OnSensorFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSensorFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSensorFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSensorFragmentInteraction(Uri uri);
    }
    /*Interface method*/
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        activity = (RunnerMan)getActivity();
        log = new LoggingService(view.getContext(), "SENSORS",null);
//        accidentLog = new LoggingService(view.getContext(), "SENSORS","incidents");

        username = activity.getUsername();
        userID = activity.getUserID();

        allSensors =  mSensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor s: allSensors){
            Log.i(SENSOR_SERVICE_TAG,s.getName());
        }
        allSensors =  mSensorManager.getSensorList(Sensor.TYPE_ALL);
        /*UNCALIBRATED_GYROSCOPE and ROTATION_VECTOR > 4 event values*/
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        deviceSensors = new ArrayList<Sensor>();
        //A list of all potentially useful sensors
        if (mAccelerometer != null){
            Log.i(TAG, "Acceleromter added");
            deviceSensors.add(mAccelerometer);
        }

        lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        /*Adding an individual TextView for each sensor*/
        TextView tv;
        sensorTextViews = new HashMap<String,TextView>();
        for (Sensor s: deviceSensors){
            tv = new TextView(view.getContext());
            tv.setLayoutParams(lparams);
            mFlParent.addView(tv);
            Log.d(TAG,"Adding textview " + s.getName());
            sensorTextViews.put(s.getName(), tv);
        }
        requester = new Requests();
        maxMagnitude = null;
    }

    /*Ref: https://github.com/Bhide/Low-Pass-Filter-To-Android-Sensors*/
    //If new values are unusually larger, only a dampened output is added
    //If new values are unusually smaller, the change is dampened
    //Previous values with added dampened  values are returned
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ){
            Log.d(TAG,"Setting initial pass values");
            return input;
        }
        for ( int i=0; i<input.length; i++ ) {
            /*Appending a dampened value from the new inputs*/
            output[i] = output[i] + LOW_PASS_ALPHA * (input[i] - output[i]);
        }
        return output;
    }
    double magnitude;
    /*Interface method*/
    /*https://developer.android.com/reference/android/hardware/SensorEvent.html - HIGH PASS FILTER FOR GRAVITY*/
    @Override
    public final void onSensorChanged(SensorEvent event) {
        /*Low-pass filter values*/
        sensorName = event.sensor.getName();
        sensor = (TextView)sensorTextViews.get(sensorName);
        sensor.setText(String.valueOf(sensorName +": "));
        String output;
        if (sensor != null){

            /*Accelerometer is the most important sensor as it is low-passed and magnitude is calculated*/
            if (sensorName.toUpperCase().equals("ACCELEROMETER")){

                /*Only accelerometer is low-passed*/
                filterValues = lowPass(event.values.clone(), filterValues);

                if (gravity==null){
                    Log.d(TAG,"Setting initial gravity values");
                    gravity = filterValues;
                }
                /*Must reduce gravity values*/
                gravity[0] = GRAVITY_ALPHA * gravity[0] + (1 - GRAVITY_ALPHA) * event.values[0]; //GRAVITY_ALPHA = 0.8f
                gravity[1] = GRAVITY_ALPHA * gravity[1] + (1 - GRAVITY_ALPHA) * event.values[1];
                gravity[2] = GRAVITY_ALPHA * gravity[2] + (1 - GRAVITY_ALPHA) * event.values[2];

                /*Establish linear values without the affect of gravity*/
                float linearX = event.values[0] - gravity[0];
                float linearY = event.values[1] - gravity[1];
                float linearZ = event.values[2] - gravity[2];
                magnitude = Math.sqrt(
                        Math.pow(linearX,2) +
                                Math.pow(linearY,2) +
                                Math.pow(linearZ,2)
                );

                if (maxMagnitude == null){
                    maxMagnitude = magnitude;
                }else{
                    if (Math.abs(magnitude) > Math.abs(maxMagnitude)){
                        maxMagnitude = magnitude;
                    }
                }
                if (maxX == null && maxY == null  && maxZ == null ){
                    maxX = linearX;
                    maxY = linearY;
                    maxZ = linearZ;
                }else{
                    //the original signed value is stored, not the absolute value
                    if (Math.abs(linearX) > Math.abs(maxX)){
                        maxX = linearX;
                    }
                    if (Math.abs(linearY) > Math.abs(maxY)){
                        maxY = linearY;
                    }
                    if (Math.abs(linearZ) > Math.abs(maxZ)){
                        maxZ = linearZ;
                    }
                }
                /*only write values to log every 20 seconds to minimize size and IO*/
                output = String.format("X,%f,Y,%f,Z,%f,Max,X,%f,Y,%f,Z,%f, Magnitude, %f",linearX,linearY,linearZ,maxX,maxY,maxZ, magnitude);
                log.writeLog(TAG, output);

                if(isAccident(magnitude)){
//                    showAccidentScreen();
                    Toast.makeText(activity, "An accident may have occurred.", Toast.LENGTH_SHORT).show();
//                    accidentLog.writeLog(TAG, output);
                }

                for (int i = 0; i < filterValues.length; i++){
                    sensor.append(
                            String.valueOf(f.format(event.values[i] - gravity[i]) + " ")
                    );
                }




            }else{
                //Non-accelerometer values; unfiltered - set sensor name and append value
                for (int i = 0; i < event.values.length; i++){
                    sensor.append(String.valueOf(event.values[i]) + " ");
                }
            }
        }
    }
public void sendAccident(){
    Location currentLocation;
    currentLocation = activity.getLocation();
    if(InitRunner.getInstance().internetAvailable()){
        try {
            String stringResponse;
            JSONObject response = requester.sendAccident("runner",currentLocation.getLatitude(),currentLocation.getLongitude(), new Date(), Integer.parseInt(userID));
            if (response!=null){
                stringResponse = response.getString("result");
                if (stringResponse.toLowerCase().equals("success")){
                    Toast.makeText(activity, "Accident has been sent and services are notified",
                            Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(activity, "Accident could not be sent",
                            Toast.LENGTH_LONG).show();
                }

            }
        }catch(IOException e){
            Log.e(TAG, "Accident report could not be sent, internet may not be available.");
        }catch(JSONException e){
            Log.e(TAG, "JSON response object could not be accessed");
        }

    }else{
        Toast.makeText(activity, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
    }
}
    private void showAccidentScreen(){

        activity.showCountDownTimer();
    }
    public final int THRESHOLD = 15;
    private boolean isAccident(double magnitude){
        /*need to determine a proper detection algorithm*/
        if (magnitude >= THRESHOLD) return true;

        return false;
    }
    /*Received at the same time as the activity*/
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"Fragment onResume");
        for (Sensor s: deviceSensors){
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }
    @Override
    public void onPause() {
        Log.d(TAG,"Fragment onPause");
        super.onPause();
    }
    @Override
    public void onDestroyView(){
        log.closeLogFile();
        Log.d(TAG,"Fragment onDestroyView");
        mSensorManager.unregisterListener(this);
        super.onDestroyView();
    }
}
