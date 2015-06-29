package com.jareddlc.openfit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class OpenFitSavedPreferences {
    private static final String LOG_TAG = "OpenFit:OpenFitSavedPreferences";
    public static final String PREFS_NAME = "OpenFitSettings";
    public static final String PREFS_DEFAULT = "DEFAULT";
    
    private SharedPreferences preferences;
    private Editor editor;

    public String preference_list_devices_value;
    public String preference_list_devices_entry;

    public OpenFitSavedPreferences(Context context) {
        this.load(context);
    }

    public void load(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        //Editor editor = preferences.edit();
        Log.d(LOG_TAG, "Loading saved preferences");
        preference_list_devices_value = preferences.getString("preference_list_devices_value", PREFS_DEFAULT);
        preference_list_devices_entry = preferences.getString("preference_list_devices_entry", PREFS_DEFAULT);
    }

    public void save(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public void save(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }
}
