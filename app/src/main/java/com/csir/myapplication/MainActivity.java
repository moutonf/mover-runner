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
        Intent intent = getIntent();
        String message = intent.getStringExtra(LoginActivity.USER_ID);
        this.setTitle("User:" + message);
    }

    public void getRunner(View view) {
        intent = new Intent(this, RunnerMan.class);
        this.startActivity(intent);
    }


}
