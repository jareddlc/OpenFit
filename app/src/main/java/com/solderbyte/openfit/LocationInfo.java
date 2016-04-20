package com.solderbyte.openfit;

import java.util.List;
import java.util.Locale;

import com.solderbyte.openfit.util.OpenFitIntent;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;


public class LocationInfo {
    private static final String LOG_TAG = "OpenFit:Location";

    private static List<Address> addresses = null;
    private static LocationManager locationManager = null;
    //private static Criteria criteria = null;
    private static Location location = null;
    private static Location locationGPS = null;
    private static Location locationNet = null;
    private static LocationListener locationListener = null;
    private static Geocoder geocoder = null;
    private static List<String> providers = null;

    private static String cityName = null;
    private static String StateName = null;
    private static String CountryName = null;
    private static String CountryCode = null;
    private static double latitude = 0;
    private static double longitude = 0;
    private static float accuracy = 0;

    private static float totalDistance = 0.0f;
    private static float currentSpeed = 0.0f;
    private static float currentAltitude = 0.0f;
    private static long timestamp = 0;
    private static Location prevLocation = null;

    private static Context context;

    public static void init(Context cntxt) {
        Log.d(LOG_TAG, "Initializing Location");
        context = cntxt;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(context, Locale.getDefault());

        updateLastKnownLocation();
        listenForLocation(true);
    }

    public static void resetData() {
        totalDistance = 0.0f;
        currentSpeed = 0.0f;
        timestamp = 0;
        prevLocation = null;
    }

    public static void updateLastKnownLocation() {
        providers = locationManager.getProviders(true);

        for(String provider : providers) {
            Location loc = locationManager.getLastKnownLocation(provider);
            if(loc != null) {
                if(location != null) {
                    if(loc.getTime() < location.getTime()) {
                        location = loc;
                    }
                }
                else {
                    location = loc;
                }
            }
        }

        //criteria = new Criteria();
        //locationManager.getBestProvider(criteria, true);
        //location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
        if(location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if(addresses.size() > 0) {
                    cityName = addresses.get(0).getLocality();
                    cityName = addresses.get(0).getLocality();
                    if(cityName == null) {
                        cityName = addresses.get(0).getSubAdminArea();
                    }
                    if(cityName == null) {
                        cityName = addresses.get(0).getAdminArea();
                    }
                    StateName = addresses.get(0).getAdminArea();
                    CountryName = addresses.get(0).getCountryName();
                    CountryCode = addresses.get(0).getCountryCode();
                    Log.d(LOG_TAG, "Location: "+ cityName + ", " + CountryCode);
                }
            }
            catch (Exception e) {
                Log.e(LOG_TAG, "Error: "+ e);
            }
        }
    }

    public static Location getLastBestLocation() {
        locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        long GPSLocationTime = 0;
        long NetLocationTime = 0;

        if(locationGPS != null) {
            GPSLocationTime = locationGPS.getTime();
        }
        if(locationNet != null) {
            NetLocationTime = locationNet.getTime();
        }
        if(GPSLocationTime - NetLocationTime < 0) {
            return locationGPS;
        }
        else {
            return locationNet;
        }
    }

    public static void listenForLocation(final boolean useGeocoder) {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // locationManager.removeUpdates(this);
                if(location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    currentAltitude = (float)location.getAltitude();
                    currentSpeed = location.getSpeed();

                    if (prevLocation != null) {
                        Location loc1 = new Location("");
                        loc1.setLatitude(prevLocation.getLatitude());
                        loc1.setLongitude(prevLocation.getLongitude());
                        Location loc2 = new Location("");
                        loc2.setLatitude(location.getLatitude());
                        loc2.setLongitude(location.getLongitude());
                        totalDistance += loc1.distanceTo(loc2);
                    }
                    prevLocation = location;
                    timestamp = location.getTime();

                    accuracy = location.getAccuracy();

                    if (useGeocoder) {
                        try {
                            addresses = geocoder.getFromLocation(latitude, longitude, 1);
                            if (addresses.size() > 0) {
                                cityName = addresses.get(0).getLocality();
                                if (cityName == null) {
                                    cityName = addresses.get(0).getSubAdminArea();
                                }
                                if (cityName == null) {
                                    cityName = addresses.get(0).getAdminArea();
                                }
                                StateName = addresses.get(0).getAdminArea();
                                CountryName = addresses.get(0).getCountryName();
                                CountryCode = addresses.get(0).getCountryCode();
                                Log.d(LOG_TAG, "onLocationChanged: " + cityName + ", " + CountryCode);
                            }
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "Error: " + e);
                        }
                    }
                    Intent msg = new Intent(OpenFitIntent.INTENT_SERVICE_LOCATION);
                    msg.putExtra("cityName", cityName);
                    msg.putExtra("StateName", StateName);
                    msg.putExtra("CountryName", CountryName);
                    msg.putExtra("CountryCode", CountryCode);
                    context.sendBroadcast(msg);
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(LOG_TAG, "GPS OFF");
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    Intent msg = new Intent(OpenFitIntent.INTENT_SERVICE_LOCATION);
                    msg.putExtra("status", false);
                    context.sendBroadcast(msg);
                }
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(LOG_TAG, "GPS ON");
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    Intent msg = new Intent(OpenFitIntent.INTENT_SERVICE_LOCATION);
                    msg.putExtra("status", false);
                    context.sendBroadcast(msg);
                }
            }
        };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 15, locationListener);
        //locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Log.d(LOG_TAG, "Location not change: " + cityName + ", " + CountryCode);
                        if (locationListener != null) {
                            // Log.d(LOG_TAG, "Removing Location updates");
                            // locationManager.removeUpdates(locationListener);
                            if (latitude == 0 && longitude == 0) {
                                updateLastKnownLocation();
                            }
                        }
                        if (useGeocoder || (cityName != null && CountryCode != null)) {
                            Intent msg = new Intent(OpenFitIntent.INTENT_SERVICE_LOCATION);
                            msg.putExtra("cityName", cityName);
                            msg.putExtra("StateName", StateName);
                            msg.putExtra("CountryName", CountryName);
                            msg.putExtra("CountryCode", CountryCode);
                            context.sendBroadcast(msg);
                        }
                    }
                }, 20000);
    }

    public static void removeUpdates() {
        Log.d(LOG_TAG, "Removing Location updates");
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    public static boolean isGPSEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String getCityName() {
        return cityName;
    }

    public static String getStateName() {
        return StateName;
    }

    public static String getCountryName() {
        return CountryName;
    }

    public static String getCountryCode() {
        return CountryCode;
    }

    public static double getLat() {
        return latitude;
    }

    public static double getLon() {
        return longitude;
    }

    public static float getTotalDistance() { return totalDistance; }

    public static float getCurrentSpeed() { return currentSpeed; }

    public static float getCurrentAltitude() { return currentAltitude; }

    public static long getTimestamp() { return timestamp; }

    public static float getAccuracy() { return accuracy; }

}
