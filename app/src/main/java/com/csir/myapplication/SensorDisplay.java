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

import java.util.ArrayList;
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

    private SensorManager mSensorManager;
    private static final String TAG = "SENSOR";
    private TextView sensorInfo;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_display);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sensorInfo = (TextView) findViewById(R.id.sensor_info);
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
            deviceSensors.add(mAccelerometer);
        }
        if (mGyro != null){
            deviceSensors.add(mGyro);
        }
        if (mLight != null){
            deviceSensors.add(mLight);
        }

        if (mLinearAcceleration != null){
            deviceSensors.add(mLinearAcceleration);
        }
        if (mRotationVector != null){
            deviceSensors.add(mRotationVector);
        }
        if (mSignificantMotion != null){
            deviceSensors.add(mSignificantMotion);
        }




        lparams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
        TextView tv;


        String info;
        sensorTextViews = new HashMap<String,TextView>();
        for (Sensor s: deviceSensors){
            info = s.toString() + " " + s.getMinDelay() + "\n";
            Log.i(TAG, "Available: " + info );
            sensorInfo.append(info);
            tv = new TextView(this);
            tv.setLayoutParams(lparams);
            viewGroup.addView(tv);
            Log.d(TAG,"Adding textview " + s.getName());
            sensorTextViews.put(s.getName(), tv);
        }
    }

    /*Interface method*/
    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    /*Interface method*/
    /*https://developer.android.com/reference/android/hardware/SensorEvent.html - HIGH PASS FILTER FOR GRAVITY*/

    @Override
    public final void onSensorChanged(SensorEvent event) {
        // The light sensor returns a single value.
        // Many sensors return 3 values, one for each axis.
        //_sensorTextView.Text = string.Format("x={0:f}, y={1:f}, y={2:f}", e.Values[0], e.Values[1], e.Values[2]);

        String sensorName = event.sensor.getName();
        Log.i(TAG, "Sensor: " + sensorName);
        Log.i(TAG, "Timestamp: " + event.timestamp);
        TextView sensor = (TextView)sensorTextViews.get(sensorName);

        if (sensor != null){
            sensor.setText(String.valueOf(sensorName +": "));
            for (int i = 0; i < event.values.length; i++){
                sensor.append(String.valueOf(event.values[i] + " "));
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
