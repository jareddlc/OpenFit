package com.solderbyte.openfit;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;


public class LocationInfo {
    private static final String LOG_TAG = "OpenFit:Location";
    
    private static List<Address> addresses = null;
    private static LocationManager locationManager = null;
    //private static Criteria criteria = null;
    private static Location location = null;
    private static Geocoder geocoder = null;
    private static List<String> providers = null;

    private static String cityName = null;
    private static String StateName = null;
    private static String CountryName = null;
    private static String CountryCode = null;
    private static double latitude = 0;
    private static double longitude = 0;

    public static void init(Context context) {
        Log.d(LOG_TAG, "Initializing Location");
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        geocoder = new Geocoder(context, Locale.getDefault());

        updateLastKnownLocation();
    }

    public static void updateLastKnownLocation() {
        providers = locationManager.getProviders(true);

        for(String provider : providers) {
            Location loc = locationManager.getLastKnownLocation(provider);
            if(loc != null) {
                if(location != null) {
                    if(loc.getAccuracy() < location.getAccuracy()) {
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
                    Log.d(LOG_TAG, "Location: "+ cityName + ", " + StateName + " " + CountryName);
                }
            }
            catch (Exception e) {
                Log.e(LOG_TAG, "Error: "+ e);
            }
        }
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
}
