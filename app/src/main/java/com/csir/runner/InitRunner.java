package com.csir.runner;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PowerManager;

/**
 * Created by brandseye-gavin on 2016/10/02.
 */

public class InitRunner extends Application {

    private static final String TAG = "RUNNER_INITIALIZE";

    PowerManager.WakeLock mWakeLock;
    private static InitRunner singleton = null;

    public static InitRunner getInstance() {
        return singleton;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        singleton = this;
        super.onCreate();

        /* This code together with the one in onDestroy()
         * will make the screen be always on until this Activity gets destroyed. */
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Application wakelock");
        /*wakelock will be released when the process is killed, very bad coding practice. hack fix*/
        this.mWakeLock.acquire();
    }

    public boolean internetAvailable(){

        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
    }

}
