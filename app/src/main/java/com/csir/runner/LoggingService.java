package com.csir.runner;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
    String TAG;
    Date date1,date2;

    FileWriter fw;
    BufferedWriter bw;
    /*Filename is date and time when activity is run*/
    public LoggingService(Context context, String TAG){
        this.context = context;

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        // parse the string into Date objec
        this.filename = df.format(new Date());
        this.TAG = TAG;
        this.filename = filename + "-" + TAG + ".csv";
        if (isExternalStorageWritable()){
            Log.i(LOG_TAG,"External storage used");
            logFile = new File(this.context.getExternalFilesDir(null),filename);
        }else{
            //internal storage
            Log.i(LOG_TAG,"Internal storage used");
            logFile = new File(this.context.getFilesDir(), filename);

        }
        /*this isn't the best way. loggingservice should only be created if successful*/
        if (!logFile.exists()){
            try {
                logFile.createNewFile();
                Log.i(LOG_TAG,"Log file successfully created");
                fw = new FileWriter(logFile, true);
                bw = new BufferedWriter(fw);

            }catch(IOException e){
                Log.e(LOG_TAG,"Log file couldn't be created");
                e.printStackTrace();
            }
        }
        Log.i(LOG_TAG,"Log file location: " + logFile.getAbsolutePath());
        date1 = new Date();
    }

    public void writeLog(String TAG, String input){
        date2 = new Date();

        if (logFile!=null){

            if ((date2.getTime() - date1.getTime()) > 1000 ){
                //comma-separate values, easier for analysis
                //if the logger was not correctly created, this will throw exceptions repeatedly
                String [] input_values = input.split(",");
                StringBuilder builder = new StringBuilder();
                for (String value: input_values){
                    builder.append(value + ",");
                }
                String output = String.format("%s, %s, %s\n",new Date().toString(), TAG, builder.toString());

                try {
                    if (bw!=null){
                        bw.write(output);
                    }
                } catch (Exception e) {
                    Log.e(TAG,"Log output could not write");
                    Log.e(TAG,Log.getStackTraceString(e));
                }
                date1 = date2;
            }

        }

    }

    public void closeLogFile(){
        Log.e(LOG_TAG,"Closing the logFile");
        Log.e(TAG,"Closing the logFile");
        try{
            if(bw!=null){
                bw.close();
            }
        }catch(IOException e){
            Log.e(TAG,"Log file could not be closed successfully");
        }finally{
            bw = null;
            fw = null;
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
