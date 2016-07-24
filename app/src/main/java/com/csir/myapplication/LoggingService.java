package com.csir.myapplication;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/*Ref: https://developer.android.com/training/basics/data-storage/files.html*/

/**
 * Created by GavinW on 2016-07-24.
 */
public class LoggingService {
    String LOG_TAG = "MOVER - LOGGING";
    String filename;
    File logFile;
    Context context;
    /*Filename is date and time when activity is run*/
    public LoggingService(Context context){
        this.context = context;
        this.filename = String.valueOf(new Date().getTime());
        if (isExternalStorageWritable()){
            Log.i(LOG_TAG,"External storage used");
            logFile = new File(this.context.getExternalFilesDir(null),filename);
        }else{
            //internal storage
            Log.i(LOG_TAG,"Internal storage used");
            logFile = new File(this.context.getFilesDir(), filename);
        }
        Log.i(LOG_TAG,"Log file location: " + logFile.getAbsolutePath());
    }
    FileOutputStream outputStream;

    public void writeLog(String TAG, String input){
        String output = new Date().toString() + " " + TAG + " " + input;
        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(output.getBytes());
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG,"Log output could not write");
            e.printStackTrace();
        }
    }


    /* Checks if external storage is available for read and write - External storage is used unless unavailable */
    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
