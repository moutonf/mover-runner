package com.csir.runner;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

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
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            intent = new Intent(this, RunnerMan.class);
            intent.putExtra(getString(R.string.USER_ID_EXTRA), userId);
            intent.putExtra(getString(R.string.USERNAME_EXTRA), username);
            this.startActivity(intent);
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, null,
                    RC_LOCATION_WRITE_PERM, perms);
        }
    }
        @AfterPermissionGranted(RC_LOCATION_WRITE_PERM)
        private void startIntent() {
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};
            if (EasyPermissions.hasPermissions(this, perms)) {
                intent = new Intent(this, RunnerMan.class);
                intent.putExtra(getString(R.string.USER_ID_EXTRA), userId);
                intent.putExtra(getString(R.string.USERNAME_EXTRA), username);
                this.startActivity(intent);
            } else {
                EasyPermissions.requestPermissions(this, null,
                        RC_LOCATION_WRITE_PERM, perms);
            }
        }
    public static final int RC_LOCATION_WRITE_PERM = 45;
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
