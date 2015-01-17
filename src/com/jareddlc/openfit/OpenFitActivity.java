package com.jareddlc.openfit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

public class OpenFitActivity extends Activity {
    private static final String LOG_TAG = "OpenFit:OpenFitActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");
        getFragmentManager().beginTransaction().replace(android.R.id.content, new OpenFitFragment()).commit();
    }

    public static class OpenFitFragment extends PreferenceFragment {
        private static final String LOG_TAG = "OpenFit:OpenFitFragment";

        // preferences
        private static SwitchPreference preference_switch_bluetooth;
        private static CheckBoxPreference preference_checkbox_connect;;
        private static ListPreference preference_list_paired;
        private static Preference preference_scan;
        private static Preference preference_test;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Load the preferences from an XML resource
            Log.d(LOG_TAG, "adding preferences from resource");
            addPreferencesFromResource(R.xml.preferences);

            // load saved preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final Editor editor = preferences.edit();

            // UI listeners
            preference_switch_bluetooth = (SwitchPreference) getPreferenceManager().findPreference("preference_switch_bluetooth");
            preference_switch_bluetooth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        preference_switch_bluetooth.setChecked(false);
                        //bluetoothLeService.enableBluetooth();
                        Toast.makeText(getActivity(), "Enabling...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //bluetoothLeService.disableBluetooth();
                    }
                    return true;
                }
            });

            preference_scan = (Preference) getPreferenceManager().findPreference("preference_scan");
            preference_scan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), "Scanning...", Toast.LENGTH_SHORT).show();
                    //bluetoothLeService.scanLeDevice();
                    return true;
                }
            });

            preference_list_paired = (ListPreference) getPreferenceManager().findPreference("preference_list_paired");
            preference_list_paired.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //BluetoothLeService.setEntries();
                    //preference_list_paired.setEntries(BluetoothLeService.getEntries());
                    //preference_list_paired.setEntryValues(BluetoothLeService.getEntryValues());
                    return true;
                }
            });
            preference_list_paired.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    //BluetoothLeService.setDevice(newValue.toString());
                    int index = preference_list_paired.findIndexOfValue(newValue.toString());
                    CharSequence[] entries = preference_list_paired.getEntries();
                    preference_list_paired.setSummary(entries[index].toString());
                    return true;
                }
            });

            preference_checkbox_connect = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_connect");
            preference_checkbox_connect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        //bluetoothLeService.connect(mDeviceAddress);
                        return false;
                    }
                    else {
                        //bluetoothLeService.disconnect();
                        return true;
                    }
                }
            });

            preference_test = (Preference) getPreferenceManager().findPreference("preference_test");
            preference_test.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), "Testing...", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
    }
}
