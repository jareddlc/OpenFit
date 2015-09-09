package com.solderbyte.openfit;

import java.util.LinkedHashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class OpenFitSavedPreferences {
    private static final String LOG_TAG = "OpenFit:OpenFitSavedPreferences";
    public static final String PREFS_NAME = "OpenFitSettings";
    public static final String PREFS_DEFAULT = "DEFAULT";
    public static final boolean PREFS_DEFAULT_BOOL = false;

    private SharedPreferences preferences;
    private Editor editor;

    public String preference_list_devices_value;
    public String preference_list_devices_entry;
    public boolean preference_checkbox_phone;
    public boolean preference_checkbox_sms;
    public boolean preference_checkbox_time;
    public boolean preference_checkbox_weather;
    public Set<String> set_packageNames = new LinkedHashSet<String>();

    public OpenFitSavedPreferences(Context context) {
        this.load(context);
    }

    public void load(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();

        Log.d(LOG_TAG, "Loading saved preferences");
        preference_list_devices_value = preferences.getString("preference_list_devices_value"+":string", PREFS_DEFAULT);
        preference_list_devices_entry = preferences.getString("preference_list_devices_entry"+":string", PREFS_DEFAULT);
        preference_checkbox_phone = preferences.getBoolean("preference_checkbox_phone"+":boolean", PREFS_DEFAULT_BOOL);
        preference_checkbox_sms = preferences.getBoolean("preference_checkbox_sms"+":boolean", PREFS_DEFAULT_BOOL);
        preference_checkbox_time = preferences.getBoolean("preference_checkbox_time"+":boolean", PREFS_DEFAULT_BOOL);
        preference_checkbox_weather = preferences.getBoolean("preference_checkbox_weather"+":boolean", PREFS_DEFAULT_BOOL);
        set_packageNames = preferences.getStringSet("set_packageNames", set_packageNames);
    }

    public void saveBoolean(String key, boolean value) {
        //Log.d(LOG_TAG, "Saving: " + key+":boolean :" + value);
        editor.putBoolean(key+":boolean", value);
        editor.commit();
    }

    public void saveString(String key, String value) {
        //Log.d(LOG_TAG, "Saving: " + key+":string :" + value);
        editor.putString(key+":string", value);
        editor.commit();
    }

    public void saveSet(String value) {
        //Log.d(LOG_TAG, "Adding to Set: " + value);
        set_packageNames.add(value);
        editor.putStringSet("set_packageNames", set_packageNames);
        editor.commit();
    }

    public boolean getBoolean(String key) {
        //Log.d(LOG_TAG, "Getting: " + key+":boolean");
        boolean value = preferences.getBoolean(key+":boolean", PREFS_DEFAULT_BOOL);
        return value;
    }

    public String getString(String key) {
        //Log.d(LOG_TAG, "Getting: " + key+":string");
        String value = preferences.getString(key+":string", PREFS_DEFAULT);
        return value;
    }

    public Set<String> getSet() {
        Set<String> packageNames = new LinkedHashSet<String>();
        packageNames = preferences.getStringSet("set_packageNames", packageNames);
        //Log.d(LOG_TAG, "Getting Set["+packageNames.size()+"]: " + packageNames);
        return packageNames;
    }

    public void removeBoolean(String key) {
        //Log.d(LOG_TAG, "Removing: " + key+":boolean");
        editor.remove(key+":boolean");
        editor.commit();
    }

    public void removeString(String key) {
        //Log.d(LOG_TAG, "Removing: " + key+":string");
        editor.remove(key+":string");
        editor.commit();
    }

    public void removeSet(String value) {
        //Log.d(LOG_TAG, "Removing Set: " + value);
        set_packageNames.remove(value);
        editor.putStringSet("set_packageNames", set_packageNames);
        editor.commit();
    }
}
