package com.suraiya.agdalauncher.model;

import android.os.Bundle;

/**
 * Created by Suraiya on 12/13/2017.
 */

public class Location {
    private double accuracy;
    private double altitude;
    private double bearing;
    private boolean complete;
    private long elapsedRealtimeNanos;
    private Bundle extra;
    private boolean fromMockProvider;
    private double latitude;
    private double longitude;
    private String provider;
    private float speed;
    private long time;

    public Location() {
        accuracy = 0.0;
        altitude = 0.0;
        bearing = 0.0;
        complete = false;
        elapsedRealtimeNanos = 0;
        extra = new Bundle();
        fromMockProvider = false;
        latitude = 0.0;
        longitude = 0.0;
        provider = "";
        speed = 0.0f;
        time = 0;
    }

    public Location(double accuracy, double altitude, double bearing, boolean complete, long elapsedRealtimeNanos, Bundle extra, boolean fromMockProvider, double latitude, double longitude, String provider, float speed, long time) {
        this.accuracy = accuracy;
        this.altitude = altitude;
        this.bearing = bearing;
        this.complete = complete;
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
        this.extra = extra;
        this.fromMockProvider = fromMockProvider;
        this.latitude = latitude;
        this.longitude = longitude;
        this.provider = provider;
        this.speed = speed;
        this.time = time;
    }

    public Location(android.location.Location location){
        this.accuracy = location.getAccuracy();
        this.altitude = location.getAltitude();
        this.bearing = location.getBearing();
        this.complete = true;
        this.elapsedRealtimeNanos = location.getElapsedRealtimeNanos();
        this.extra = location.getExtras();
        this.fromMockProvider = location.isFromMockProvider();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.provider= location.getProvider();
        this.speed = location.getSpeed();
        this.time = location.getTime();
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public long getElapsedRealtimeNanos() {
        return elapsedRealtimeNanos;
    }

    public void setElapsedRealtimeNanos(long elapsedRealtimeNanos) {
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
    }

    public Bundle getExtra() {
        return extra;
    }

    public void setExtra(Bundle extra) {
        this.extra = extra;
    }

    public boolean isFromMockProvider() {
        return fromMockProvider;
    }

    public void setFromMockProvider(boolean fromMockProvider) {
        this.fromMockProvider = fromMockProvider;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
