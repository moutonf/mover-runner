package com.csir.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

public class SensorDisplay extends AppCompatActivity implements SensorEventListener {
    LoggingService log;
    private SensorManager mSensorManager;
    private static final String TAG = "MOVER_SENSOR";
    private TextView sensorInfo,sensorMax;

    private Sensor mAccelerometer;
    private Sensor mGyro;
    private Sensor mLight;
    private Sensor mLinearAcceleration;
    private Sensor mRotationVector;
    private Sensor mSignificantMotion;
    HashMap<String, TextView> sensorTextViews;
    private List<Sensor> deviceSensors;
    private ViewGroup viewGroup;

    private LayoutParams lparams;

    static final float LOW_PASS_ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.
    final float GRAVITY_ALPHA = 0.8f;

    Float maxX,maxY,maxZ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_display);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensorInfo = (TextView) findViewById(R.id.sensor_info);
        sensorMax = (TextView) findViewById(R.id.accelerometer_max);
        log = new LoggingService(this);
        sensorInfo.setText("SENSOR INFO:\n");
        viewGroup = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);

        /*Get potentially useful sensors*/
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSignificantMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

        //List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        deviceSensors = new ArrayList<Sensor>();
        //A list of all potentially useful sensors
        if (mAccelerometer != null){
            Log.i(TAG, "Acceleromter added");
            deviceSensors.add(mAccelerometer);
        }
        if (mGyro != null){
            Log.i(TAG, "Gyro sensor added");

            deviceSensors.add(mGyro);
        }
        if (mLight != null){
            Log.i(TAG, "Light sensor added");

            deviceSensors.add(mLight);
        }

        if (mLinearAcceleration != null){
            Log.i(TAG, "Linear Acceleration sensor added");

            deviceSensors.add(mLinearAcceleration);
        }
        if (mRotationVector != null){
            Log.i(TAG, "Roation Vector sensor added");

            deviceSensors.add(mRotationVector);
        }
        if (mSignificantMotion != null){
            Log.i(TAG, "Significant Motion sensor added");

            deviceSensors.add(mSignificantMotion);
        }
        lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
        TextView tv;
        String info;
        sensorTextViews = new HashMap<String,TextView>();
        for (Sensor s: deviceSensors){
            tv = new TextView(this);
            tv.setLayoutParams(lparams);
            viewGroup.addView(tv);
            Log.d(TAG,"Adding textview " + s.getName());
            sensorTextViews.put(s.getName(), tv);
        }

        date1 = new Date();
    }

    /*Interface method*/
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
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
    /*Interface method*/
    /*https://developer.android.com/reference/android/hardware/SensorEvent.html - HIGH PASS FILTER FOR GRAVITY*/
    float [] filterValues;
    String sensorName;
    TextView sensor;
    DecimalFormat f = new DecimalFormat("0.000");
    Date date1,date2;
    float[] gravity;
    @Override
    public final void onSensorChanged(SensorEvent event) {
        /*Low-pass filter values*/
        date2 = new Date();
        filterValues = lowPass(event.values.clone(), filterValues);

        sensorName = event.sensor.getName();
        Log.i(TAG, "Sensor: " + sensorName);
        Log.i(TAG, "Timestamp: " + event.timestamp);

        sensor = (TextView)sensorTextViews.get(sensorName);

        double magnitude;

        if (sensor != null){
            if (sensorName.toUpperCase().equals("ACCELEROMETER")){

                if (gravity==null){
                    Log.d(TAG,"Setting initial gravity values");

                    gravity = filterValues;
                }

                /*Must reduce gravity values*/
                gravity[0] = GRAVITY_ALPHA * gravity[0] + (1 - GRAVITY_ALPHA) * event.values[0]; //GRAVITY_ALPHA = 0.8f
                gravity[1] = GRAVITY_ALPHA * gravity[1] + (1 - GRAVITY_ALPHA) * event.values[1];
                gravity[2] = GRAVITY_ALPHA * gravity[2] + (1 - GRAVITY_ALPHA) * event.values[2];

                float linearX = event.values[0] - gravity[0];
                float linearY = event.values[1] - gravity[1];
                float linearZ = event.values[2] - gravity[2];

//                magnitude = Math.sqrt(
//                        Math.pow(event.values[0],2) +
//                        Math.pow(event.values[1],2) +
//                        Math.pow(event.values[2],2)
//                     );
                if (maxX == null && maxY == null  && maxZ == null ){
                    maxX = linearX;
                    maxY = linearY;
                    maxZ = linearZ;
                }else{
                    if (Math.abs(linearX) > maxX){
                        maxX = linearX;
                    }
                    if (Math.abs(linearY) > maxY){
                        maxY = linearY;
                    }
                    if (Math.abs(linearZ) > maxZ){
                        maxZ = linearZ;
                    }
                }
                /*only write values to log every five seconds to minimize size and IO*/
                if ((date2.getTime() - date1.getTime()) > 5000 ){

                    log.writeLog(TAG,String.format("Accelerometer: X %f Y %f Z%f Max: X %f Y %f Z %f",linearX,linearY,linearZ,maxX,maxY,maxZ ));
                }
            }
            sensor.setText(String.valueOf(sensorName +": "));
            sensorMax.setText(String.format("maxX: %f | maxY: %f | maxZ: %f",maxX,maxY,maxZ));
            for (int i = 0; i < filterValues.length; i++){
                sensor.append(
                        String.valueOf(f.format(event.values[i] - gravity[i]) + " ")
                );
//                sensor.append(String.valueOf(event.values[i] + " "));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (Sensor s: deviceSensors){
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
