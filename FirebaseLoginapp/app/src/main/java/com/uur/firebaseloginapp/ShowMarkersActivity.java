package com.uur.firebaseloginapp;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ShowMarkersActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private ValueEventListener mPostListener;
    private DatabaseReference mCommentsReference;
    private String mPostKey;
    private RecyclerView mCommentsRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_markers);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        Log.i("Info","ShowMarkerActivity onCreate");

        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            mAuth = FirebaseAuth.getInstance();
            userRef = FirebaseDatabase.getInstance().getReference().child("users");
            user = mAuth.getCurrentUser();
        }catch (Exception e){
            Log.i("Info", "ShowMarkerActivity onCreate error:" + e.toString());
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.i("Info","  >>ShowMarkerActivity onMapReady");

        try {

            Query q = userRef.orderByKey().equalTo(user.getUid());

            if(q != null)
                Log.i("Info","    >>Queue:" + q.toString());




            /*q.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    User user = new User();
                    user = dataSnapshot.getValue(User.class);

                    Log.i("Info","userInfo:" + user);


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    Log.i("Info", "ShowMarkerActivity db read failed:" + databaseError.getCode() +
                            "-Error text:" + databaseError.toString());
                }
            });*/
        }catch (Exception e){
            Log.i("Info", "    >>ShowMarkerActivity onMapReady error:" + e.toString());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i("Info","  >>onStart");

        try {

            ValueEventListener postListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    User user = new User();
                    user = dataSnapshot.getValue(User.class);

                    if(user != null)
                        Log.i("Info", "    >>userInfo:" + user);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                    Log.i("Info", "    >>ShowMarkerActivity db read failed:" + databaseError.getCode() +
                            "-Error text:" + databaseError.toString());
                }
            };

            userRef.addValueEventListener(postListener);

            // Keep copy of post listener so we can remove it when app stops
            mPostListener = postListener;
        }catch (Exception e){
            Log.i("Info", "    >>onStart Error:" + e.toString());
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            userRef.removeEventListener(mPostListener);
        }
    }

    private void postComment() {

        Log.i("Info","  >>postComment");

        try {
            final String uid = user.getUid();

            FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get user information
                            User user = dataSnapshot.getValue(User.class);

                            if(user != null)
                                Log.i("Info", "    >>userInfo:" + user);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            Log.i("Info", "    >>ShowMarkerActivity db read failed:" + databaseError.getCode() +
                                    "-Error text:" + databaseError.toString());
                        }
                    });
        }catch (Exception e){
            Log.i("Info", "    >>postComment Error:" + e.toString());
        }
    }

    public void showMyPins(){


    }
}
