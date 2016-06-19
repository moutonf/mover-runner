package com.csir.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getRequests(View view) {
        Intent intent = new Intent(this, Requests.class);
        this.startActivity(intent);
    }

    public void getSensors(View view) {
        Intent intent = new Intent(this, SensorDisplay.class);
        this.startActivity(intent);
    }
}
