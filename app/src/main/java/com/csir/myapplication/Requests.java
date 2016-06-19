package com.csir.myapplication;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Requests extends AppCompatActivity {

    TextView txtView;
    Button button;

    private static final String TAG = "CONNECTION";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        txtView = (TextView) findViewById(R.id.textView);
        txtView.setText("Waiting for response..");

        button = (Button) findViewById(R.id.button_id);

        /*Async tasks and threads should be used for connections and requests*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void load(View view) {
        //Testing internet connection
        HttpURLConnection urlConnection = null;
        URL url;
        try {

            url = new URL("http://moutonf.co.za:5000/test-api");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            int code = urlConnection.getResponseCode();
            Log.i(TAG, "Response code=" + code);
//            txtView.setText("Response code: " + code);

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            InputStreamReader isw = new InputStreamReader(in);
            String response = "";
            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                response += current;
            }
            txtView.append(response);

        }catch(IOException ex){
            ex.printStackTrace();
        }
        finally {
            if (urlConnection !=null){
                urlConnection.disconnect();

            }

        }
    }
}
