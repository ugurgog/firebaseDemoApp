package com.uur.firebaseloginapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MapListActivity extends AppCompatActivity {

    static ArrayList<String> places = new ArrayList<>();
    static ArrayList<LatLng> locations = new ArrayList<>();
    static ArrayAdapter arrayAdapter;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);

        ListView listView = (ListView) findViewById(R.id.listView);

        places.add("Add a new place...");
        locations.add(new LatLng(0, 0));

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, places);

        try {

            mAuth = FirebaseAuth.getInstance();

            listView.setAdapter(arrayAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                    intent.putExtra("placeNumber", i);

                    startActivity(intent);
                }

            });
        }catch (Exception e){
            Log.i("Info:", "Error:" + e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.chose_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        FirebaseUser user = mAuth.getCurrentUser();

        if(item.getItemId() == R.id.showMyPin){

            Intent intent = new Intent(getApplicationContext(), ShowMarkersActivity.class);
            startActivity(intent);

        }else if (item.getItemId() == R.id.logout){

            Log.i("Info","logout icindeyiz");
            try {
                mAuth.signOut();
                finish();
                Intent intent = new Intent(this, StartPageActivity.class);
                startActivity(intent);

            }catch (Exception e){
                Log.i("Info","Logout Error:" + e.toString());
            }

        }

        return super.onOptionsItemSelected(item);
    }
}