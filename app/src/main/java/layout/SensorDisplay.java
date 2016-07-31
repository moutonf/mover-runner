package layout;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csir.myapplication.LoggingService;
import com.csir.myapplication.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorDisplay.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SensorDisplay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorDisplay extends Fragment implements SensorEventListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private OnFragmentInteractionListener mListener;

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

    private LinearLayout.LayoutParams lparams;

    static final float LOW_PASS_ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.
    final float GRAVITY_ALPHA = 0.8f;

    Float maxX,maxY,maxZ;
    View view;

    float [] filterValues;
    String sensorName;
    TextView sensor;
    DecimalFormat f = new DecimalFormat("0.000");
    Date date1,date2;
    float[] gravity;
    Double maxMagnitude;

    private FrameLayout mFlParent;

    public SensorDisplay() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SensorDisplay newInstance() {
        return new SensorDisplay();
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
        sensorInfo = (TextView) view.findViewById(R.id.sensor_info);
        sensorMax = (TextView) view.findViewById(R.id.accelerometer_max);
        mFlParent  = (FrameLayout) view.findViewById(R.id.fl_frag_sensor_display_parent);
        return view;
    }

    /*Enforces the interface*/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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

        log = new LoggingService(view.getContext());

        /*Get potentially useful sensors*/
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyro = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mLinearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSignificantMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

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

        maxMagnitude = null;
        date1 = new Date();
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
    @Override
    public final void onSensorChanged(SensorEvent event) {
        /*Low-pass filter values*/
        date2 = new Date();
        filterValues = lowPass(event.values.clone(), filterValues);
        sensorName = event.sensor.getName();
        Log.i(TAG, "Sensor: " + sensorName);
        Log.i(TAG, "Timestamp: " + event.timestamp);
        sensor = (TextView)sensorTextViews.get(sensorName);
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
                double magnitude = Math.sqrt(
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
                /*only write values to log every five seconds to minimize size and IO*/
                if ((date2.getTime() - date1.getTime()) > 5000 ){
                    Log.d(TAG,"Write log @ " + date2.getTime());

                    log.writeLog(TAG,String.format("X,%f,Y,%f,Z%f,Max,X,%f,Y,%f,Z,%f, Magnitude, %f",linearX,linearY,linearZ,maxX,maxY,maxZ, magnitude ));
                    date1 = date2;
                }
                sensorMax.setText(String.format("maxX: %f | maxY: %f | maxZ: %f | magnitude: %f",maxX,maxY,maxZ, maxMagnitude));
            }
            sensor.setText(String.valueOf(sensorName +": "));
            for (int i = 0; i < filterValues.length; i++){
                sensor.append(
                        String.valueOf(f.format(event.values[i] - gravity[i]) + " ")
                );
//                sensor.append(String.valueOf(event.values[i] + " "));
            }
        }
    }
    /*Received at the same time as the activity*/
    public void onResume() {
        super.onResume();
        for (Sensor s: deviceSensors){
            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
}
