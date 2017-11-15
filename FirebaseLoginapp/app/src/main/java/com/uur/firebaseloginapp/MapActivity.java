package com.uur.firebaseloginapp;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Interpolator;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Criteria;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener
        , SensorEventListener, GeoFire.CompletionListener, GeoQueryEventListener, GoogleMap.OnCameraChangeListener {

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;

    private GeoFire geoFire;
    private GeoQuery geoQuery;
    private GeoLocation geoLocation;

    private Marker marker;

    private Map<String, Marker> markers;

    private Circle searchCircle;

    private static final float SHAKE_THRESHOLD = 3.25f; // m/S**2
    private static final int MIN_TIME_BETWEEN_SHAKES_MILLISECS = 1000;
    private long mLastShakeTime;

    private SensorManager mSensorMgr;

    private static final GeoLocation INITIAL_CENTER = new GeoLocation(37.7789, -122.4017);
    private static final int INITIAL_ZOOM_LEVEL = 14;
    private static final String GEO_FIRE_DB = "https://fir-demo-app1.firebaseio.com";
    private static final String GEO_FIRE_REF = GEO_FIRE_DB + "/_geofire";

    private String appId = "geofire";
    private double radius = 1.0f;

    public Criteria criteria;
    public String bestProvider;

    private FirebaseAuth mAuth;
    private  DatabaseReference mDbref;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        try {

            Log.i("Info", "onRequestPermissionsResult============");

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (lastKnownLocation != null) {
                        try {
                            Log.i("Info", "centerMapOnLocation4");
                            centerMapOnLocation(lastKnownLocation, "Your location");
                        } catch (Exception e) {
                            Log.i("Info", "CenterMapOnLoc:Error:" + e.toString());
                        }
                    }
                }


            }
        }catch (Exception e){
            Log.i("Info","onRequestPermissionsResult Error:" + e.toString());
        }

    }

    public void centerMapOnLocation(Location location, String title) {

        Log.i("Info", "centerMapOnLocation============");

        if (location == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
                locationManager.requestLocationUpdates(bestProvider, 0, 0, locationListener);
            }

        }

        try {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.clear();

            if (title != "Your location") {

                mMap.addMarker(new MarkerOptions().position(userLocation).title(title));

            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14));
        }catch (Exception e){
            Log.i("Info","Error1:" + e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            Log.i("Info", "onCreate============");

            mapFragment.getMapAsync(this);

            mAuth = FirebaseAuth.getInstance();

            // Get a sensor manager to listen for shakes
            mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

            // Listen for shakes
            Sensor accelerometer = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                mSensorMgr.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }

            FirebaseOptions options = new FirebaseOptions.Builder().
                    setApplicationId(appId).setDatabaseUrl(GEO_FIRE_DB).build();

            Log.i("Info", "FirebaseOptions.AppId:" + options.getApplicationId());


            //FirebaseApp app = FirebaseApp.initializeApp(this, options, appId);


            // setup GeoFire
            this.geoFire = new GeoFire(FirebaseDatabase.getInstance().getReferenceFromUrl(GEO_FIRE_REF));
            // radius in km
            this.geoQuery = this.geoFire.queryAtLocation(INITIAL_CENTER, 1);

            this.markers = new HashMap<String, Marker>();


        }catch (Exception e) {
            Log.i("Info", "Error_x1:" + e.toString());
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
        try {
            mMap = googleMap;

            Log.i("Info", "onMapReady============");

            mMap.setOnMapLongClickListener(this);

            mMap.setOnMarkerClickListener(this);

            Intent intent = getIntent();

            if (intent.getIntExtra("placeNumber", 0) == 0) {

                // zoom in on user's location

                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        Log.i("Info", "centerMapOnLocation3");
                        centerMapOnLocation(location, "Your location");

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {

                    }
                };

                // if (Build.VERSION.SDK_INT < 26) {
//
                //              locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//
                //          } else {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                    Log.i("Info", "lastKnownLocation:Latitude:" + lastKnownLocation.getLatitude());
                    Log.i("Info", "lastKnownLocation:Longitude:" + lastKnownLocation.getLongitude());

                    Log.i("Info", "centerMapOnLocation1");
                    centerMapOnLocation(lastKnownLocation, "Your location");

                } else {

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                }
                //}


            } else {

                Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
                placeLocation.setLatitude(MapListActivity.locations.get(intent.getIntExtra("placeNumber", 0)).latitude);
                placeLocation.setLongitude(MapListActivity.locations.get(intent.getIntExtra("placeNumber", 0)).longitude);

                Log.i("Info", "centerMapOnLocation2");
                centerMapOnLocation(placeLocation, MapListActivity.places.get(intent.getIntExtra("placeNumber", 0)));

            }
        }catch (Exception e){
            Log.i("Info","onMapReadyException Error:" + e.toString());
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        try {
            Log.i("Info", "onMapLongClick============");
            saveCurrLocation(latLng);
        }catch (Exception e){
            Log.i("Info","onMapLongClick Error:" + e.toString());
        }
    }

    public void saveCurrLocation(LatLng latLng){

        Log.i("Info", "saveCurrLocation============");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        //saveUserInfo(currentUser);

        //DatabaseReference mDbref = FirebaseDatabase.getInstance().getReference().child("items_location");
        geoLocation = new GeoLocation(latLng.latitude, latLng.longitude);

        mDbref = FirebaseDatabase.getInstance().getReference().child("users");

        String userId = currentUser.getUid();

        Log.i("Info","  >>userId:" + userId);

        String itemId = mDbref.child(userId).child("items_location").push().getKey();

        Log.i("Info","  >>itemId:" + itemId);



        try {


            Log.i("Info","Latlng latitude :" + latLng.latitude);
            Log.i("Info","Latlng longitude:" + latLng.longitude);

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            String address = "";

            try {

                List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if (listAddresses != null && listAddresses.size() > 0) {

                    Log.i("Info","Thorougfare   :" + listAddresses.get(0).getThoroughfare());
                    Log.i("Info","CountryName   :" + listAddresses.get(0).getCountryName());
                    Log.i("Info","PostalCode    :" + listAddresses.get(0).getPostalCode());
                    Log.i("Info","Locality      :" + listAddresses.get(0).getLocality());
                    Log.i("Info","CountryCode   :" + listAddresses.get(0).getCountryCode());
                    Log.i("Info","SubThorougfare:" + listAddresses.get(0).getSubThoroughfare());


                    if (listAddresses.get(0).getThoroughfare() != null) {

                        if (listAddresses.get(0).getSubThoroughfare() != null) {

                            address += listAddresses.get(0).getSubThoroughfare() + " ";

                        }

                        address += listAddresses.get(0).getThoroughfare();

                    }


                }

                Map<String, String> values = new HashMap<>();
                values.put("Thoroughfare",listAddresses.get(0).getThoroughfare());
                addItemLocation(currentUser,values, itemId, mDbref);

                values.put("SubThoroughfare",listAddresses.get(0).getSubThoroughfare());
                addItemLocation(currentUser,values, itemId, mDbref);

                values.put("CountryName",listAddresses.get(0).getCountryName());
                addItemLocation(currentUser,values, itemId, mDbref);

                values.put("PostalCode",listAddresses.get(0).getPostalCode());
                addItemLocation(currentUser,values, itemId, mDbref);

                values.put("Locality",listAddresses.get(0).getLocality());
                addItemLocation(currentUser,values, itemId, mDbref);

                values.put("CountryCode",listAddresses.get(0).getCountryCode());
                addItemLocation(currentUser,values, itemId, mDbref);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (address == "") {

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");

                address = sdf.format(new Date());

            }

            Log.i("Info","adress:" + address);

            marker = mMap.addMarker(new MarkerOptions().position(latLng).title(address));


            MapListActivity.places.add(address);
            MapListActivity.locations.add(latLng);

            MapListActivity.arrayAdapter.notifyDataSetChanged();





            mDbref.child(userId).child("items_location").child(itemId).child("geolocation").setValue(geoLocation, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Log.i("Info","databaseError:" + databaseError);
                }
            });






            /*
            GeoFire geoFire = new GeoFire(mDbref);

            Log.i("Info","itemId:" + itemId);
            Log.i("Info","geoFire.getDatabaseReference:" + geoFire.getDatabaseReference());

            geoFire.setLocation(itemId, new GeoLocation(geoLocation.latitude, geoLocation.longitude)
                    , new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            Log.i("Info","Yazamiyom");
                            Log.i("Info", "Kayitlari yazdik mi acaba???");
                            Log.i("Info", "Key=" + key);
                            Log.i("Info", "DbError:" + error.toString());
                        }
                    });
*/
            Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();






        /*

        geoQuery = geoFire.queryAtLocation(geoLocation, radius);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
*/
        }catch (Exception e){
            Log.i("Info","SaveCurrLocation Error:" + e.toString());
        }

    }

    public void addItemLocation(FirebaseUser user, Map values, String itemId, DatabaseReference mDbref){

        String userId = user.getUid();

        mDbref.child(userId).child("items_location").child(itemId).setValue(values, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i("Info","databaseError:" + databaseError);
            }
        });

    }

    public void saveUserInfo(FirebaseUser currentUser){




        String userId = currentUser.getUid();


        Map<String, String> values = new HashMap<>();

        Log.i("Info","userId:" + userId);

        mDbref = FirebaseDatabase.getInstance().getReference().child("users");

        values.put("email", currentUser.getEmail());

        mDbref.child(userId).setValue(values, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i("Info","databaseError:" + databaseError);
            }
        });

        values.put("phoneNum", currentUser.getPhoneNumber());

        mDbref.child(userId).setValue(values, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i("Info","databaseError:" + databaseError);
            }
        });

        values.put("name", currentUser.getDisplayName());

        mDbref.child(userId).setValue(values, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Log.i("Info","databaseError:" + databaseError);
            }
        });

    }



    public void saveDataToFB(){


    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Log.i("Info","onMarkerClick============");

        Toast.makeText(this, "Marker clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {



        try {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long curTime = System.currentTimeMillis();
                if ((curTime - mLastShakeTime) > MIN_TIME_BETWEEN_SHAKES_MILLISECS) {

                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    double acceleration = Math.sqrt(Math.pow(x, 2) +
                            Math.pow(y, 2) +
                            Math.pow(z, 2)) - SensorManager.GRAVITY_EARTH;

                    //  Log.i("Info","Acceleration is:" + acceleration + "m/s^2");

                    if (acceleration > SHAKE_THRESHOLD) {
                        mLastShakeTime = curTime;

                        Log.i("Info", "Shake, Rattle, and Roll");

                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                            LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                            saveCurrLocation(userLocation);

                        } else {

                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                        }
                    }
                }
            }
        }catch (Exception e){
            Log.i("Info","onSensorChanged Error:" + e.toString());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {


    }

    @Override
    public void onComplete(String key, DatabaseError error) {

    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {

        try {
            Log.i("Info", "onKeyEntered============");
            // Add a new marker to the map
            Marker marker = this.mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
            this.markers.put(key, marker);
        }catch (Exception e){
            Log.i("Info","onKeyEntered Error:" + e.toString());
        }
    }

    @Override
    public void onKeyExited(String key) {
        try {
            // Remove any old marker
            Log.i("Info", "onKeyExited============");
            Marker marker = this.markers.get(key);
            if (marker != null) {
                marker.remove();
                this.markers.remove(key);
            }
        }catch (Exception e){
            Log.i("Info","onKeyExcited Error:" + e.toString());
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        Log.i("Info","onKeyMoved============");
// Move the marker
        Marker marker = this.markers.get(key);
        if (marker != null) {
            this.animateMarkerTo(marker, location.latitude, location.longitude);
        }
    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        try {
            Log.i("Info", "onGeoQueryError============");
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                    .setPositiveButton(android.R.string.ok, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }catch (Exception e){
            Log.i("Info","onQueryError Error:" + e.toString());
        }
    }

    // Animation handler for old APIs without animation support
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        try {
            Log.i("Info", "animateMarkerTo============");
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long DURATION_MS = 3000;
            final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            final LatLng startPosition = marker.getPosition();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    float elapsed = SystemClock.uptimeMillis() - start;
                    float t = elapsed / DURATION_MS;
                    float v = interpolator.getInterpolation(t);

                    double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                    double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                    marker.setPosition(new LatLng(currentLat, currentLng));

                    // if animation is not finished yet, repeat
                    if (t < 1) {
                        handler.postDelayed(this, 16);
                    }
                }
            });
        }catch (Exception e){
            Log.i("Info","animateMarkerTo Error:" + e.toString());
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        try {
            // Update the search criteria for this geoQuery and the circle on the map
            LatLng center = cameraPosition.target;
            double radius = zoomLevelToRadius(cameraPosition.zoom);
            this.searchCircle.setCenter(center);
            this.searchCircle.setRadius(radius);
            this.geoQuery.setCenter(new GeoLocation(center.latitude, center.longitude));
            // radius in km
            this.geoQuery.setRadius(radius / 1000);
        }catch (Exception e){
            Log.i("Info","onCameraChanged Error:" + e.toString());
        }
    }

    private double zoomLevelToRadius(double zoomLevel) {
        // Approximation to fit circle into view
        return 16384000/Math.pow(2, zoomLevel);
    }

    @Override
    protected void onStop() {
        try {
            Log.i("Info", "onStop============");
            super.onStop();
            // remove all event listeners to stop updating in the background
            this.geoQuery.removeAllListeners();
            for (Marker marker : this.markers.values()) {
                marker.remove();
            }
            this.markers.clear();
        }catch (Exception e){
            Log.i("Info","onStop Error:" + e.toString());
        }
    }

    @Override
    protected void onStart() {
        try {
            Log.i("Info", "onStart============");
            super.onStart();
            // add an event listener to start updating locations again

            if (!FirebaseApp.getApps(this).isEmpty()) {
                FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            }

            try {
                this.geoQuery.addGeoQueryEventListener(this);
            } catch (Exception e) {
                Log.i("Info", "Error_x2:" + e.toString());
            }
        }catch (Exception e){
            Log.i("Info","onStart Error:" + e.toString());
        }
    }
}
