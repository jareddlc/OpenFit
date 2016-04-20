package com.solderbyte.openfit;

public class GPSData {

    private float totalDistance;
    private float speed;
    private float lon;
    private float lat;
    private float altitude;
    private float accuracy;
    private long timeStamp;

    public GPSData(float tD, float s, float lo, float la, float alt, long tS) {
        totalDistance = tD;
        speed = s;
        lon = lo;
        lat = la;
        altitude = alt;
        accuracy = 0;
        timeStamp = tS;
    }

    public float getTotalDistance() { return totalDistance; }

    public float getSpeed() { return speed; }

    public float getLon() { return lon; }

    public float getLat() { return lat; }

    public float getAltitude() { return altitude; }

    public float getAccuracy() { return accuracy; }

    public long getTimeStamp() {
        return timeStamp;
    }
}
