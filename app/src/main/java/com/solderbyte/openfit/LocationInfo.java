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

    private static Context context;

    public static void init(Context cntxt) {
        Log.d(LOG_TAG, "Initializing Location");
        context = cntxt;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(context, Locale.getDefault());

        updateLastKnownLocation();
        listenForLocation();
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

    public static void listenForLocation() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationManager.removeUpdates(this);
                if(location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if(addresses.size() > 0) {
                            cityName = addresses.get(0).getLocality();
                            StateName = addresses.get(0).getAdminArea();
                            CountryName = addresses.get(0).getCountryName();
                            CountryCode = addresses.get(0).getCountryCode();
                            Log.d(LOG_TAG, "onLocationChanged: "+ cityName + ", " + CountryCode);

                            Intent msg = new Intent(OpenFitIntent.INTENT_SERVICE_LOCATION);
                            msg.putExtra("cityName", cityName);
                            msg.putExtra("StateName", StateName);
                            msg.putExtra("CountryName", CountryName);
                            msg.putExtra("CountryCode", CountryCode);
                            context.sendBroadcast(msg);
                        }
                    }
                    catch (Exception e) {
                        Log.e(LOG_TAG, "Error: "+ e);
                    }
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderDisabled(String provider) {}
            @Override
            public void onProviderEnabled(String provider) {}
           };

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
        //locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    if(locationListener != null) {
                        Log.d(LOG_TAG, "Removing Location updates");
                        locationManager.removeUpdates(locationListener);
                        if(latitude == 0 && longitude == 0) {
                            updateLastKnownLocation();
                        }
                    }
                }
        }, 20000);
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
}
