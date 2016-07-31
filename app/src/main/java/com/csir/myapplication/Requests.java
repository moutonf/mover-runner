package com.csir.myapplication;

import android.location.Location;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*OkHTTP classes*/
import java.io.IOException;
import java.net.URL;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Requests {

    OkHttpClient client;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "CONNECTION";

    final String POST_BASE_URL =
            "http://moutonf.co.za:5000/post-api";
    final String GET_BASE_URL =
            "http://moutonf.co.za:5000/get-api";

    final String ID_PARAM = "id";
    final String MESSAGE_PARAM = "message";

    public Requests(){
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
    public String getRequest() {
        String response;
        try {
            Uri builtUri = Uri.parse(GET_BASE_URL)
                    .buildUpon()
                    .appendQueryParameter(ID_PARAM, "get-id")
                    .appendQueryParameter(MESSAGE_PARAM, "get-message")
                    .build();
            response = run(builtUri.toString());
            return response;
        }catch(IOException ex){
//            ex.printStackTrace();
            return ("A problem occurred");
        }
    }
    /*POST requests*/
    /*The server currently checks id and msg arguments*/
    String post(String url, Location location) throws IOException {

        String latitude = String.valueOf(location.getLatitude());
        String longitude = String.valueOf(location.getLongitude());

        RequestBody formBody = new FormBody.Builder()
                .add("id", "gavin")
                .add("message", "this is a message from gavin")
                .add("latitude",latitude)
                .add("longitude",longitude)
                .build();
//        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String sendAccident(Location location) {
        String response;
        try {
        //MUST BUILD A JSON
            response =  post("http://moutonf.co.za:5000/accident", location);
            return response;
        }catch(IOException ex){
            ex.printStackTrace();
            return ("A problem occurred");
        }

    }


}
