package com.csir.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getRequests(View view) {
         intent = new Intent(this, Requests.class);
        this.startActivity(intent);
    }

    public void getSensors(View view) {
         intent = new Intent(this, SensorDisplay.class);
        this.startActivity(intent);
    }

    public void getLocation(View view) {
         intent = new Intent(this, FindLocation.class);
        this.startActivity(intent);
    }

    public void getMap(View view) {
        intent = new Intent(this, MapsActivity.class);
        this.startActivity(intent);
    }
}
