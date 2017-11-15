package com.uur.firebaseloginapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.security.spec.ECField;

public class StartPageActivity extends AppCompatActivity implements View.OnClickListener{



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);


        try {
            findViewById(R.id.emailVerifyButton).setOnClickListener(this);
            findViewById(R.id.githubVerifyButton).setOnClickListener(this);
            findViewById(R.id.googleVerifyButton).setOnClickListener(this);
            findViewById(R.id.phoneVerifyButton).setOnClickListener(this);
            findViewById(R.id.facebookVerifyButton).setOnClickListener(this);
            findViewById(R.id.twitterVerifyButton).setOnClickListener(this);
            findViewById(R.id.anonimVerifyButton).setOnClickListener(this);
        }catch (Exception e){
            Log.i("Info","startPage oncreate error:" + e.toString());
        }
    }

    @Override
    public void onClick(View view) {

        try {
            Log.i("Info", "onClick method");
            int id = view.getId();

            Intent intent;

            switch (id) {

                case R.id.emailVerifyButton:
                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    break;
                case R.id.googleVerifyButton:
                    intent = new Intent(getApplicationContext(), GoogleSignInActivity.class);
                    startActivity(intent);
                    break;
                case R.id.phoneVerifyButton:
                    intent = new Intent(getApplicationContext(), PhoneAuthActivity.class);
                    startActivity(intent);
                    break;
                case R.id.facebookVerifyButton:
                    intent = new Intent(getApplicationContext(), FacebookLoginActivity.class);
                    startActivity(intent);
                    break;
                case R.id.twitterVerifyButton:
                    intent = new Intent(getApplicationContext(), TwitterLoginActivity.class);
                    startActivity(intent);
                    break;
                default:
                    Toast.makeText(this, "Error button id", Toast.LENGTH_SHORT).show();
                    break;

            }
        }catch (Exception e){
            Log.i("Info","StartPage on click error:" + e.toString());
        }
    }
}
