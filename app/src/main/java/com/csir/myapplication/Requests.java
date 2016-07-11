package com.csir.myapplication;

import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*OkHTTP classes*/
import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Requests extends AppCompatActivity {

    TextView txtView;
    Button button, postBtn;
    OkHttpClient client;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "CONNECTION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        txtView = (TextView) findViewById(R.id.textView);
        txtView.setText("Waiting for response..");

        button = (Button) findViewById(R.id.get_button);
        postBtn = (Button) findViewById(R.id.post_button);

        client  = new OkHttpClient();
        /*Requests should be async, this is dirty*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }



    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
    public void getRequest(View view) {
        String response;
        try {
            response = run("http://moutonf.co.za:5000/get-api");
            txtView.append(response);
        }catch(IOException ex){
            ex.printStackTrace();
        }

    }
    /*POST requests*/
    /*The server currently checks id and msg arguments*/
    String post(String url) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("id", "gavin")
                .add("message", "this is a message from gavin")
                .build();
//        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public void postRequest(View view) {
        String response;
        try {
        //MUST BUILD A JSON
            response=  post("http://moutonf.co.za:5000/post-api");
            txtView.append(response);

        }catch(IOException ex){
            ex.printStackTrace();
        }

    }


}
