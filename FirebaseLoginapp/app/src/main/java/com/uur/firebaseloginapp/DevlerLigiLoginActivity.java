package com.uur.firebaseloginapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.HashMap;
import java.util.Map;

public class DevlerLigiLoginActivity extends AppCompatActivity implements View.OnClickListener{

    //Set the radius of the Blur. Supported range 0 < radius <= 25
    private static final float BLUR_RADIUS = 10f;

    private FirebaseAuth mAuth;
    private boolean signInOk = false;
    private DatabaseReference mDbref;

    private EditText emailEditText;
    private EditText passwordEditText;

    private TwitterLoginButton mLoginButton;
    private static final String TAG = "TwitterLogin";

    private CallbackManager mCallbackManager;
    private boolean relLayClk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // FullScreencall();

        // Configure Twitter SDK
        TwitterAuthConfig authConfig =  new TwitterAuthConfig(
                getString(R.string.twitter_consumer_key),
                getString(R.string.twitter_consumer_secret));

        TwitterConfig twitterConfig = new TwitterConfig.Builder(this)
                .twitterAuthConfig(authConfig)
                .build();

        Twitter.initialize(twitterConfig);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_devler_login);

        RelativeLayout backGrounRelLayout = (RelativeLayout) findViewById(R.id.loginLayout);
        backGrounRelLayout.setOnClickListener(this);



        try {
            ImageView imageView = (ImageView) findViewById(R.id.road_pic);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.back_image);
            Bitmap blurredBitmap = blur(bitmap);
            imageView.setImageBitmap(blurredBitmap);

            mAuth = FirebaseAuth.getInstance();

            // Views
            emailEditText = findViewById(R.id.emailEditText);
            passwordEditText = findViewById(R.id.passwordEditText);

            findViewById(R.id.signInButton).setOnClickListener(this);

            // [START initialize_twitter_login]
            mLoginButton = findViewById(R.id.twitterLoginButton);

            mLoginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {

                    Log.i("Info","twitterLogin:success" + result);

                    handleTwitterSession(result.data);
                }

                @Override
                public void failure(TwitterException exception) {

                    Log.i("Info","twitterLogin:failure:" + exception);
                }
            });

            // Initialize Facebook Login button
            mCallbackManager = CallbackManager.Factory.create();

            LoginButton loginButton = findViewById(R.id.facebookLoginButton);

            loginButton.setReadPermissions("email", "public_profile");

            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {

                    Log.i("Info","facebook:onSucces:" + loginResult);

                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.i("Info","facebook:onCancel");
                }

                @Override
                public void onError(FacebookException error) {

                    Log.i("Info","facebook:onError:" + error);
                }
            });

        }catch (Exception e){
            Log.i("Info","DevlerLigi login err:" + e.toString());
        }
    }

    public void FullScreencall() {
        if(Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if(Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public Bitmap blur(Bitmap image) {
        if (null == image) return null;

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(this);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        //Intrinsic Gausian blur filter
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    @Override
    public void onClick(View v) {


        Log.i("Info", "onClick clk====================");


        int i = v.getId();

        if (i == R.id.signInButton) {
            signIn(emailEditText.getText().toString(), passwordEditText.getText().toString());
        }else if(i == R.id.loginLayout){

            Log.i("Info", "loginLayout clk");

            if(relLayClk == false)
                relLayClk = true;
            else
                relLayClk = false;

            hideNavBar();
            hideKeyBoard();
        }

        /*else if (i == R.id.sendVerifyEmailButton){
            sendEmailVerification();
        }*/
    }

    public void hideKeyBoard(){

        Log.i("Info", "hideKeyBoard");

        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    public void hideNavBar(){

        Log.i("Info", "hideNavBar");

        View decorView = getWindow().getDecorView();

        Log.i("Info", "  >>relLayClk:" + relLayClk);

        if(relLayClk) {
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
        }else{
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void signIn(String email, String password) {

        Log.i("Info","SignIn:" + email);

        if (!validateForm()) {
            return;
        }

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i("Info","signInWithEmail:success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            if(!user.isEmailVerified()){

                                sendEmailVerification();

                                Toast.makeText(DevlerLigiLoginActivity.this, "Please verify sended mail!", Toast.LENGTH_SHORT).show();}
                            else{

                                Intent intent = new Intent(getApplicationContext(), MapListActivity.class);
                                startActivity(intent);
                            }


                            signInOk = true;



                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i("Info","signInWithEmail:failure:" + task.getException());


                            FirebaseUser user = mAuth.getCurrentUser();

                            Log.i("Info","signInWithEmail:failure1:" + task.getException());

                            if(task.getException().toString().contains("FirebaseAuthInvalidUserException")) {
                                startRegisterPage();
                            }

                            Log.i("Info","signInWithEmail:failure2:" + task.getException());

                        }

                       // if (!task.isSuccessful()) {
                       //     mStatusTextView.setText(R.string.auth_failed);
                       // }

                    }
                });
    }

    private void sendEmailVerification() {

        try {
            // Send verification email
            final FirebaseUser user = mAuth.getCurrentUser();

            user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {

                        Log.i("Info", "sendEmailVerification:success");

                        Toast.makeText(DevlerLigiLoginActivity.this,
                                "Verification email sent to " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Log.i("Info", "sendEmailVerification:failed:" + task.getException());

                        Toast.makeText(DevlerLigiLoginActivity.this,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e){
            Log.i("Info", "sendEmailVerification error:" + e.toString());
        }
    }

    public void startRegisterPage(){

        Intent intent = new Intent(DevlerLigiLoginActivity.this, DevlerLigiRegister.class);

        intent.putExtra("email",emailEditText.getText());
        intent.putExtra("password",passwordEditText.getText());

        startActivity(intent);
    }


    private boolean validateForm() {
        boolean valid = true;


        String email = emailEditText.getText().toString();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Required.");
            valid = false;
        } else {
            emailEditText.setError(null);
        }

        String password = passwordEditText.getText().toString();

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Required.");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }

        return valid;
    }

    // [START auth_with_twitter]
    private void handleTwitterSession(TwitterSession session) {

        Log.i("Info","handleTwitterSession:" + session);

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.i("Info","signInWithCredential:success");

                            Intent intent = new Intent(getApplicationContext(), MapListActivity.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.

                            Log.i("Info","signInWithCredential:failure:" + task.getException());

                            Toast.makeText(DevlerLigiLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the Twitter login button.
        mLoginButton.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {

        Log.i("Info","handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Log.i("Info","signInWithCredential:success" );

                            Intent intent = new Intent(getApplicationContext(), MapListActivity.class);
                            startActivity(intent);

                        } else {
                            // If sign in fails, display a message to the user.

                            Log.i("Info","signInWithCredential:failure:" + task.getException());

                            Toast.makeText(DevlerLigiLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
