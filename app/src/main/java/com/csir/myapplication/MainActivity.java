package com.csir.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    Intent intent;

    private String userId;
    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        userId = intent.getStringExtra(getString(R.string.USER_ID_EXTRA));
        username = intent.getStringExtra(getString(R.string.USERNAME_EXTRA));

        this.setTitle("User:" + userId);
    }

    public void getRunner(View view) {
        intent = new Intent(this, RunnerMan.class);
        intent.putExtra(getString(R.string.USER_ID_EXTRA), userId);
        intent.putExtra(getString(R.string.USERNAME_EXTRA), username);
        this.startActivity(intent);
    }


}
