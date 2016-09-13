package com.csir.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;

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
            // Have permissions, do the thing!
//            Toast.makeText(this, "Location and logging are required otherwise Mover Runner won't work.", Toast.LENGTH_LONG).show();
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

//        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_WRITE_EXTERNAL_STORAGE);
//        }
//        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    REQUEST_ACCESS_FINE_LOCATION);
//        }
    
    public static final int RC_LOCATION_WRITE_PERM = 45;

    public static final int REQUEST_ACCESS_FINE_LOCATION = 50;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 51;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case REQUEST_ACCESS_FINE_LOCATION: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if (grantResults.length ==2){
//                        intent = new Intent(this, RunnerMan.class);
//                        intent.putExtra(getString(R.string.USER_ID_EXTRA), userId);
//                        intent.putExtra(getString(R.string.USERNAME_EXTRA), username);
//                        this.startActivity(intent);
//                    }else{
//                        Toast.makeText(MainActivity.this, "Mover Runner cannot work if there is no location service, and writing is required for logging", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, "Mover Runner needs permission for location and external writing (logging)", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//            case REQUEST_WRITE_EXTERNAL_STORAGE: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if (grantResults.length ==2){
//                        intent = new Intent(this, RunnerMan.class);
//                        intent.putExtra(getString(R.string.USER_ID_EXTRA), userId);
//                        intent.putExtra(getString(R.string.USERNAME_EXTRA), username);
//                        this.startActivity(intent);
//                    }else{
//                        Toast.makeText(MainActivity.this, "Mover Runner cannot work if there is no location service, and writing is required for logging", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Toast.makeText(MainActivity.this, "Mover Runner needs permission for location and external writing (logging)", Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//        }
//    }
}
