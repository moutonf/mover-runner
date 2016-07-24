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

    String filename;
    File logFile;
    Context context;
    /*Filename is date and time when activity is run*/
    public LoggingService(Context context){
        this.context = context;
        this.filename = new Date().toString();
        if (isExternalStorageWritable()){
            logFile = new File(this.context.getExternalFilesDir(null),filename);

        }else{
            //internal storage
            logFile = new File(this.context.getFilesDir(), filename);
        }
    }
    FileOutputStream outputStream;

    public void writeLog(String input, String TAG){

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(input.getBytes());
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
