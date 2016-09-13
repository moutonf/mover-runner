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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

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
//    final String POST_DEV_URL = "http://10.0.0.6:5000";
    final String POST_DEV_URL = "http://139.162.178.79:4000";

    final String LOGIN = "login";
    final String REGISTER = "register";
    final String ACCIDENT = "accident";
    public Requests(){
        client  = new OkHttpClient();
        /*Requests should be async, this is dirty*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
    private JSONObject call(Request request) throws IOException {
        JSONObject result;
        try{
            Response response = client.newCall(request).execute();
            result = new JSONObject(response.body().string()); //throws JSONObject error
        }catch(IOException e){
            Log.e(TAG, "No result. IO error");
            return null;
        }catch(JSONException e){
            Log.e(TAG, "No result. JSON error");
            return null;
        }
        Log.i(TAG, result.toString());
        return result;
    }
    public JSONObject login(String email, String password) throws IOException
    {
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url(POST_DEV_URL + "/" + LOGIN)
                .post(formBody)
                .build();

        return call(request);

    }
    public JSONObject register(String email, String password, String passwordConfirm) throws IOException
    {
        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("password", password)
                .add("password_confirm", passwordConfirm)
                .build();
        Request request = new Request.Builder()
                .url(POST_DEV_URL + "/" + REGISTER)
                .post(formBody)
                .build();
        return call(request);
    }

    public JSONObject sendAccident(String type, double longitude, double latitude, Date timeOfAccident, int userId ) throws IOException
    {
        long unixTimestamp = timeOfAccident.getTime()/1000; //unix timestamp is defined in seconds, not milliseconds
        RequestBody formBody = new FormBody.Builder()
                .add("type", type)
                .add("longitude", String.format("%.3f",longitude))
                .add("latitude", String.format("%.3f",latitude))
                .add("time-of-accident", String.valueOf(unixTimestamp))
                .add("userId", String.valueOf(userId))
                .build();
        Request request = new Request.Builder()
                .url(POST_DEV_URL + "/" + ACCIDENT)
                .post(formBody)
                .build();
        return call(request);
    }
}
