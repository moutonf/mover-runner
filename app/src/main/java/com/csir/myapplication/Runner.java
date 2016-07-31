package com.csir.myapplication;

import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import layout.SensorDisplay;

public class Runner extends FragmentActivity implements SensorDisplay.OnFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_runner);
        /*Fragments are added in the XML layout, not programmatically*/
    }

    public void onFragmentInteraction(Uri uri){

    }


}
