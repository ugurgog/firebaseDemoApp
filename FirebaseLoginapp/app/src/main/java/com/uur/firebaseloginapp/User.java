package com.uur.firebaseloginapp;

import java.util.ArrayList;

/**
 * Created by mac on 9.11.2017.
 */

public class User {

    String email;
    String phoneNum;
    String name;
    ArrayList<Location> locations;

    public User(){

        email = "";
        phoneNum = "";
        name = "";
        locations.clear();
    }

    public void addLocation(Location location){

        locations.add(location);
    }



    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setName(String name) {
        this.name = name;
    }
}
