package com.cuichen.jt808_sdk.sdk.jt808bean;

public class Jt808MapLocation {

    public Jt808MapLocation() {

    }

    public Jt808MapLocation(double latitude, double longitude, double altitud, float speed, float bearing, float accuracy, String time) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitud = altitud;
        this.speed = speed;
        this.bearing = bearing;
        this.accuracy = accuracy;
        this.time = time;
    }

    private double latitude;
    private double longitude;
    private double altitud;
    private float speed;
    private float bearing;
    private float accuracy;
    private String time;

    public double getLat() {
        return latitude;
    }

    public void setLat(double latitude) {
        this.latitude = latitude;
    }

    public double getLng() {
        return longitude;
    }

    public void setLng(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitud() {
        return altitud;
    }

    public void setAltitud(double altitud) {
        this.altitud = altitud;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Jt808MapLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", altitud=" + altitud +
                ", speed=" + speed +
                ", bearing=" + bearing +
                ", accuracy=" + accuracy +
                ", time='" + time + '\'' +
                '}';
    }
}
