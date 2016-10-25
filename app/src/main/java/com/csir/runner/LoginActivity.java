package com.csir.runner;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    Intent intent;
    private Requests requester;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mPasswordConfirmView;

    private View mProgressView;
    private View mLoginFormView;

    private UserAuthTask mAuthTask = null;
    private static final String REGISTER_URL = "REGISTER";
    private static final String LOGIN_URL = "LOGIN";
    private static final String TAG = "MOVER_LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordConfirmView = (EditText) findViewById(R.id.password_confirm);

        requester = new Requests();

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAuthentication(LOGIN_URL);
            }
        });
        Button mRegisterButton = (Button) findViewById(R.id.email_register_button);
        mRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAuthentication(REGISTER_URL);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptAuthentication(String auth) {
        if (mAuthTask != null) {
            return;
        }
        Log.i(TAG,"Clicked");
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passwordConfirm = mPasswordConfirmView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 1. Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        // 2. If the password isn't empty, check if it is valid Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        // 3. If register, confirm passwords
        if (auth.equals(REGISTER_URL)){
            if (!password.equals(passwordConfirm)){
                mPasswordView.setError(getString(R.string.error_no_match_password));
                focusView = mPasswordView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            if (auth.equals(LOGIN_URL)){
                Log.i(TAG, "Login");
                mAuthTask = new UserAuthTask(email, password, null,LOGIN_URL);
                mAuthTask.execute((Void) null);
            }
            if (auth.equals(REGISTER_URL)){
                Log.i(TAG, "Register");
                mAuthTask = new UserAuthTask(email, password, passwordConfirm, REGISTER_URL);
                mAuthTask.execute((Void) null);
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
//        return password.length() > 4;
        return true;
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserAuthTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mPasswordConfirm;
        private String mAuth;

        private JSONObject result;

        UserAuthTask(String email, String password, String passwordConfirm, String auth) {
            mEmail = email;
            mPassword = password;
            mPasswordConfirm = passwordConfirm;
            mAuth = auth;
        }
        @Override
        protected Boolean doInBackground(Void... params)
        {JSONObject response = null;
            try {
                if (mAuth.toUpperCase().equals(LOGIN_URL)){
                    Log.i(TAG, "Login");
                    response = requester.login(mEmail, mPassword);
                }
                if (mAuth.toUpperCase().equals(REGISTER_URL)){
                    Log.i(TAG, "Register");
                    response = requester.register(mEmail, mPassword, mPasswordConfirm);
                }

                if (response != null){
                    String auth = response.getString("auth");
                    if (auth.equals("success")){
                        result  = response;
                        return true;
                    }
                    else if (auth.equals("fail")){
                        return false;
                    }
                }
            } catch(IOException e){
                Log.e("LOGIN","IO error");
                return false;
            }catch(JSONException e){
                Log.e("LOGIN","JSON error");
                return false;
            }
            return false;
        }
        Context context = LoginActivity.this;
        @Override
        protected void onPostExecute(final Boolean success)
        {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                intent = new Intent(context, MainActivity.class);
                try{
                    String username = result.getString("username");
                    String user_id = result.getString("id");
                    intent.putExtra(getString(R.string.USER_ID_EXTRA), user_id);
                    intent.putExtra(getString(R.string.USERNAME_EXTRA), username);
                    startActivity(intent);
                    Toast.makeText(context, R.string.login_successful + ", " + username, Toast.LENGTH_LONG).show();
                    finish();
                }catch(JSONException e){

                }

            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }
        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

