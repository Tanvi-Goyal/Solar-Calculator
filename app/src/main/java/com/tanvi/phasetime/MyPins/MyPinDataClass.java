package com.tanvi.phasetime.MyPins;

public class MyPinDataClass {
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public MyPinDataClass(String location, double longitude, double latitude) {

        this.longitude = longitude;
        this.latitude = latitude;
        this.location = location;
    }

    double longitude, latitude;

    public MyPinDataClass() {

    }

    String location;
}