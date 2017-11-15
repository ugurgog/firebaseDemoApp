package com.uur.firebaseloginapp;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DevlerLigiRegister extends AppCompatActivity implements View.OnClickListener, FirebaseAuth.AuthStateListener{

    //Set the radius of the Blur. Supported range 0 < radius <= 25
    private static final float BLUR_RADIUS = 10f;

    private String emailText;
    private String passwordText;

    private Button registerButton;
    private Button sendVerifButton;

    private EditText usernameEditText;
    private EditText nameEditText;
    private EditText surnameEditText;
    private EditText phoneEditText;
    private EditText birthdateEditText;
    private TextView genderTextView;

    private FirebaseAuth mAuth;
    private DatabaseReference mDbref;

    public String tag_users = "users";

    public String male = "male";
    public String female = "female";
    public String gender = "male";

    Handler handler;
    private boolean onCreateOk = false;
    private boolean runnableOk = false;

    public Calendar myCalendar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devler_ligi_register);

        RelativeLayout backGrounRelLayout = (RelativeLayout) findViewById(R.id.registerRelLayout);

        ImageView imageView = (ImageView) findViewById(R.id.road_pic);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.back_image);
        Bitmap blurredBitmap = blur(bitmap);
        imageView.setImageBitmap(blurredBitmap);

        mAuth = FirebaseAuth.getInstance();

        registerButton = findViewById(R.id.registerButton);
        sendVerifButton = findViewById(R.id.sendVerifyMailBtn);

        Switch maleOrFemale;

        usernameEditText = findViewById(R.id.usernameEditText);
        nameEditText = findViewById(R.id.nameEditText);
        surnameEditText = findViewById(R.id.surnameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        birthdateEditText = findViewById(R.id.birthdateEditText);
        genderTextView = findViewById(R.id.genderTextView);

        /*if(birthdateEditText.length() > 0) {
            birthdateEditText.getText().clear();
            TextKeyListener.clear(birthdateEditText.getText());
        }*/

        getCalender();


        maleOrFemale = (Switch) findViewById(R.id.maleOrFemaleSwitch);

        maleOrFemale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                if(isChecked){
                    gender = female;
                }else{
                    gender = male;
                }
            }
        });

        getMailAndPassword();

        findViewById(R.id.registerButton).setOnClickListener(this);
        findViewById(R.id.sendVerifyMailBtn).setOnClickListener(this);
        findViewById(R.id.birthdateEditText).setOnClickListener(this);

        backGrounRelLayout.setOnClickListener(this);



        /*birthdateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Log.e("b", "" + s);

                String birthText = s.toString();

                Log.e("Info", "birthText:" + birthText);

                if(birthText.length() <= 10){

                    if(s.length() == 2){
                        s.append('/' + birthText);
                    }else if(s.length() == 5){
                        s.append('/' + birthText);
                    }
                }else{
                    birthText
                }




                if (s.length() <= 10) {

                    if (s.length() == 2) {
                        s.append('/');
                    } else if (s.length() == 5) {
                        s.append('/');
                    }
                }else{

                }

            }

        });*/

        if(runnableOk == false)
            checkMailVerified();
    }


    public void getCalender(){

        Log.i("Info", "getCalender");

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date;

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        birthdateEditText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(DevlerLigiRegister.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {

        Log.i("Info", "  >>updateLabel");
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        birthdateEditText.setText(sdf.format(myCalendar.getTime()));
    }

    private void checkMailVerified() {

        Log.i("Info", "runnableOK:" + runnableOk);

            handler = new Handler();

            final Runnable r = new Runnable() {
                @Override
                public void run() {

                    if (onCreateOk && !runnableOk) {
                        mAuth.getCurrentUser().reload();

                        FirebaseUser user = mAuth.getCurrentUser();


                        Log.i("Info", "runnnnable func");

                        Log.i("Info", "isEmailVerified:" + user.isEmailVerified());
                        Log.i("Info", "usermail:" + user.getEmail());


                        if (user.isEmailVerified()) {
                            Log.i("Info", "    >>visible");
                            //contunieButton.setVisibility(View.VISIBLE);
                            runnableOk = true;

                            Intent intent = new Intent(getApplicationContext(), MapListActivity.class);
                            startActivity(intent);


                        }
                    }
                    handler.postDelayed(this, 2000);
                }
            };
            handler.postDelayed(r, 2000);
        }




    private void getMailAndPassword() {

        try {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();

            if (extras != null) {
                Log.i("Info", "extras.get(email):" + extras.get("email"));
                Log.i("Info", "extras.get(password):" + extras.get("password"));

                emailText = extras.get("email").toString();
                passwordText = extras.get("password").toString();
            }
        }catch (Exception e){
            Log.i("Info", "getMailAndPassword error:" + e.toString());
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

    private void createAccount(String email, String password){

        Log.i("Info","createAccount method=====");

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.i("TaskExp","TaskExp:" + task.getException());

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i("Info","CreateUserEmail:Success");

                            FirebaseUser user = mAuth.getCurrentUser();

                            saveUserInfo(user);
                            sendEmailVerification();

                            registerButton.setVisibility(View.GONE);
                            sendVerifButton.setVisibility(View.VISIBLE);

                            onCreateOk = true;

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.i("Info","CreateUserEmail:Failed:" + task.getException());

                            Toast.makeText(DevlerLigiRegister.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
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

                        Toast.makeText(DevlerLigiRegister.this,
                                "Verification email sent to " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();

                    } else {
                        Log.i("Info", "sendEmailVerification:failed:" + task.getException());

                        Toast.makeText(DevlerLigiRegister.this,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }catch (Exception e){
            Log.i("Info", "sendEmailVerification error:" + e.toString());
        }
    }

    public void saveUserInfo(FirebaseUser currentUser){

        String userId = currentUser.getUid();

        Map<String, String> values = new HashMap<>();

        Log.i("Info","userId:" + userId);

        mDbref = FirebaseDatabase.getInstance().getReference().child(tag_users);

        values.put("email", currentUser.getEmail());
        setValuesToCloud(userId, values);

        values.put("gender", gender);
        setValuesToCloud(userId, values);

        values.put("username", usernameEditText.getText().toString());
        setValuesToCloud(userId, values);

        values.put("name", nameEditText.getText().toString());
        setValuesToCloud(userId, values);

        values.put("surname", surnameEditText.getText().toString());
        setValuesToCloud(userId, values);

        values.put("phone", phoneEditText.getText().toString());
        setValuesToCloud(userId, values);

        values.put("birthdate", birthdateEditText.getText().toString());
        setValuesToCloud(userId, values);
    }

    public void setValuesToCloud(String userId, Map<String, String> values){

        mDbref.child(userId).setValue(values, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i("Info","databaseError:" + databaseError);
            }
        });
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();

        Log.i("Info","onClick works!");

        switch (i){
            case R.id.registerButton:
                if(!validateForm()){
                    return;
                }

                createAccount(emailText, passwordText);
                break;

            case R.id.birthdateEditText:
                getCalender();
                break;

            case R.id.sendVerifyMailBtn:
                sendEmailVerification();
                break;

            case R.id.registerRelLayout:
                hideKeyBoard();
                break;

            default:
                Toast.makeText(this, "Error occured!!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void hideKeyBoard(){

        Log.i("Info", "hideKeyBoard");

        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private boolean validateForm() {
        boolean valid = true;

        String username = usernameEditText.getText().toString();

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Required.");
            valid = false;
        } else {
            usernameEditText.setError(null);
        }

        String name = nameEditText.getText().toString();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Required.");
            valid = false;
        } else {
            nameEditText.setError(null);
        }

        String surname = surnameEditText.getText().toString();

        if (TextUtils.isEmpty(surname)) {
            surnameEditText.setError("Required.");
            valid = false;
        } else {
            surnameEditText.setError(null);
        }

        String phoneText = phoneEditText.getText().toString();

        if (TextUtils.isEmpty(phoneText)) {
            phoneEditText.setError("Required.");
            valid = false;
        } else {
            phoneEditText.setError(null);
        }

        String birthdateText = birthdateEditText.getText().toString();

        if (TextUtils.isEmpty(birthdateText)) {
            birthdateEditText.setError("Required.");
            valid = false;
        } else {
            birthdateEditText.setError(null);
        }

        if(gender == " " || gender == null){
            genderTextView.setError("Required.");
            valid = false;
        } else {
            genderTextView.setError(null);
        }

        return valid;
    }


    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

        FirebaseUser user = mAuth.getCurrentUser();

        if(user.isEmailVerified()){
            Log.i("Info", "    >>visible");
        }
    }
}
