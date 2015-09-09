package com.solderbyte.openfit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.solderbyte.openfit.HttpClient.AsyncResponse;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Weather {
    private static final String LOG_TAG = "OpenFit:Weather";

    private static String APIKEY = "APPID=00042ee1a3e6f5dbb2a3c63e4e8fb50a";
    private static String APIURL = "http://api.openweathermap.org/data/2.5/weather";
    private static String QUERY = "?q=";
    private static String AMP = "&";
    private static String UNITS = "units=";

    private static String units = "imperial"; //Fahrenheit = imperial, Celsius = metric, Default = Kelvin

    private static String WEATHER = "weather";
    private static String WEATHER_MAIN = "main";
    private static String WEATHER_DESC = "description";
    private static String WEATHER_ICON = "icon";
    private static String MAIN = "main";
    private static String MAIN_TEMP = "temp";
    private static String MAIN_PRES = "pressure";
    private static String MAIN_HUMD = "humidity";
    private static String MAIN_TMIN = "temp_min";
    private static String MAIN_TMAX = "temp_max";
    private static String NAME = "name";

    private static String name = null;
    private static String tempCur = null;
    private static String tempMin = null;
    private static String tempMax = null;
    private static String humidity = null;
    private static String pressure = null;
    private static String weather = null;
    private static String description = null;
    private static String icon = null;
    private static String tempUnit = null;

    private static HttpClient http = null;
    private static Context context;

    public static void init(Context cntxt) {
        Log.d(LOG_TAG, "Initializing Weather");
        context = cntxt;
        http = new HttpClient(cntxt);
    }

    public static void getWeather(String cityName) {
        Log.d(LOG_TAG, "Getting weather info for: " + cityName);
        http.get(APIURL + QUERY + cityName + AMP + UNITS + units + AMP + APIKEY, new AsyncResponse() {
            @Override
            public void callback(JSONObject res) {
                Log.d(LOG_TAG, "Weather callback");
                if(res != null) {
                    try {
                        JSONObject main = res.getJSONObject(MAIN);
                        tempCur = main.getString(MAIN_TEMP);
                        tempMin = main.getString(MAIN_TMIN);
                        tempMax = main.getString(MAIN_TMAX);
                        humidity = main.getString(MAIN_HUMD);
                        pressure = main.getString(MAIN_PRES);

                        if(units.equals("imperial")) {
                            tempUnit = "°F";
                        }
                        else if(units.equals("metric")) {
                            tempUnit = "°C";
                        }
                        else {
                            tempUnit = "K";
                        }

                        JSONArray w = res.getJSONArray(WEATHER);
                        for(int i = 0; i < w.length(); i++) {
                            JSONObject wo = w.getJSONObject(i);
                            weather = wo.getString(WEATHER_MAIN);
                            description = wo.getString(WEATHER_DESC);
                            icon = wo.getString(WEATHER_ICON);
                        }

                        name = res.getString(NAME);
                    } 
                    catch(JSONException e) {
                        e.printStackTrace();
                    }

                    Intent msg = new Intent("weather");
                    msg.putExtra("name", name);
                    msg.putExtra("weather", weather);
                    msg.putExtra("description", description);
                    msg.putExtra("tempCur", tempCur);
                    msg.putExtra("tempMin", tempMin);
                    msg.putExtra("tempMax", tempMax);
                    msg.putExtra("humidity", humidity);
                    msg.putExtra("pressure", pressure);
                    msg.putExtra("icon", icon);
                    msg.putExtra("tempUnit", tempUnit);
                    context.sendBroadcast(msg);
                }
            }
        });
    }
}
