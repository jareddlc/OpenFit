package com.solderbyte.openfit;

import java.util.Set;

import com.solderbyte.openfit.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class OpenFitActivity extends Activity {
    private static final String LOG_TAG = "OpenFit:OpenFitActivity";

    static ApplicationManager appManager;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if(item.getTitle().equals(getResources().getString(R.string.menu_add))) {
            Log.d(LOG_TAG, "Add selected: "+ item);
            DialogAddApplication d = new DialogAddApplication(appManager.getInstalledAdapter(getBaseContext()), appManager.getInstalledPackageNames(), appManager.getInstalledAppNames());
            d.show(getFragmentManager(), "installed");
        }
        if(item.getTitle().equals(getResources().getString(R.string.menu_del))) {
            Log.d(LOG_TAG, "Remove selected: "+ item);
            DialogDelApplication d = new DialogDelApplication(appManager.getListeningAdapter(getBaseContext()), appManager.getListeningPackageNames(), appManager.getListeningAppNames());
            d.show(getFragmentManager(), "listening");
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appManager = new ApplicationManager();
        appManager.getInstalledAdapter(getBaseContext());
        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");
        
        this.getFragmentManager().beginTransaction().replace(android.R.id.content, new OpenFitFragment()).commit();
    }

    public static class OpenFitFragment extends PreferenceFragment {
        private static final String LOG_TAG = "OpenFit:OpenFitFragment";

        private OpenFitSavedPreferences oPrefs;

        // UI preferences
        private static SwitchPreference preference_switch_bluetooth;
        private static CheckBoxPreference preference_checkbox_connect;
        private static CheckBoxPreference preference_checkbox_phone;
        private static CheckBoxPreference preference_checkbox_sms;
        private static CheckBoxPreference preference_checkbox_time;
        private static CheckBoxPreference preference_checkbox_weather;
        private static ListPreference preference_list_devices;
        private static Preference preference_scan;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            Log.d(LOG_TAG, "Loading preferences from XML resource");
            this.addPreferencesFromResource(R.xml.preferences);

            // load saved preferences
            oPrefs = new OpenFitSavedPreferences(getActivity());

            // setup UI
            this.setupUIListeners();

            // start service
            Intent serviceIntent = new Intent(this.getActivity(), OpenFitService.class);
            this.getActivity().startService(serviceIntent);

            // App Listener 
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(addApplicationReceiver, new IntentFilter("addApplication"));
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(delApplicationReceiver, new IntentFilter("delApplication"));
            this.getActivity().registerReceiver(bluetoothUIReceiver, new IntentFilter("bluetoothUI"));
            this.getActivity().registerReceiver(stopServiceReceiver, new IntentFilter("stopOpenFitService"));
            this.getActivity().registerReceiver(notificationServiceReceiver, new IntentFilter("NotificationService"));
        }

        @Override
        public void onResume() {
            Log.d(LOG_TAG, "onResume");
            this.clearListeningApps(oPrefs);
            this.restorePreferences(oPrefs);
            //this.restoreListeningApps(oPrefs);
            super.onResume();
        }

        private void setupUIListeners() {
            // UI listeners
            Log.d(LOG_TAG, "Setting up UI Listeners");
            preference_switch_bluetooth = (SwitchPreference) getPreferenceManager().findPreference("preference_switch_bluetooth");
            preference_switch_bluetooth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        sendIntent("bluetooth", "enable");
                        preference_switch_bluetooth.setChecked(false);
                        Toast.makeText(getActivity(), "Enabling...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        sendIntent("bluetooth", "disable");
                    }
                    return true;
                }
            });

            preference_scan = (Preference) getPreferenceManager().findPreference("preference_scan");
            preference_scan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), "Scanning...", Toast.LENGTH_SHORT).show();
                    sendIntent("bluetooth", "scan");
                    preference_list_devices.setEnabled(false);
                    preference_scan.setSummary("Scanning...please wait");
                    return true;
                }
            });

            preference_list_devices = (ListPreference) getPreferenceManager().findPreference("preference_list_devices");
            preference_list_devices.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String mDeviceAddress = newValue.toString();
                    CharSequence[] entries = preference_list_devices.getEntries();
                    int index = preference_list_devices.findIndexOfValue(mDeviceAddress);
                    String mDeviceName = entries[index].toString();
                    preference_list_devices.setSummary(mDeviceName);
                    oPrefs.saveString("preference_list_devices_value", mDeviceAddress);
                    oPrefs.saveString("preference_list_devices_entry", mDeviceName);
                    sendIntent("bluetooth", "setDevice", mDeviceAddress);
                    return true;
                }
            });

            preference_checkbox_connect = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_connect");
            preference_checkbox_connect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        //String mDeviceAddress = oPrefs.preference_list_devices_value;
                        String mDeviceName = oPrefs.preference_list_devices_entry;
                        Toast.makeText(getActivity(), "Attempting to connect to: "+mDeviceName, Toast.LENGTH_SHORT).show();
                        sendIntent("bluetooth", "connect");
                        return false;
                    }
                    else {
                        sendIntent("bluetooth", "disconnect");
                        return true;
                    }
                }
            });

            preference_checkbox_phone = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_phone");
            preference_checkbox_phone.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        oPrefs.saveBoolean("preference_checkbox_phone", true);
                        sendIntent("bluetooth", "phone", "true");
                        return true;
                    }
                    else {
                        oPrefs.saveBoolean("preference_checkbox_phone", false);
                        sendIntent("bluetooth", "phone", "false");
                        return true;
                    }
                }
            });

            preference_checkbox_sms = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_sms");
            preference_checkbox_sms.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        oPrefs.saveBoolean("preference_checkbox_sms", true);
                        sendIntent("bluetooth", "sms", "true");
                        return true;
                    }
                    else {
                        oPrefs.saveBoolean("preference_checkbox_sms", false);
                        sendIntent("bluetooth", "sms", "false");
                        return true;
                    }
                }
            });

            preference_checkbox_time = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_time");
            preference_checkbox_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        sendIntent("bluetooth", "time", "true");
                        oPrefs.saveBoolean("preference_checkbox_time", true);
                        return true;
                    }
                    else {
                        sendIntent("bluetooth", "time", "false");
                        oPrefs.saveBoolean("preference_checkbox_time", false);
                        return true;
                    }
                }
            });

            preference_checkbox_weather = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_weather");
            preference_checkbox_weather.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        sendIntent("bluetooth", "weather", "true");
                        oPrefs.saveBoolean("preference_checkbox_weather", true);
                        return true;
                    }
                    else {
                        sendIntent("bluetooth", "weather", "false");
                        oPrefs.saveBoolean("preference_checkbox_weather", false);
                        return true;
                    }
                }
            });
        }

        public void handleBluetoothMessage(String message, Intent intent) {
            // setup message handler
            if(message != null && !message.isEmpty()) {
                if(message.equals("OpenFitService")) {
                    this.clearListeningApps(oPrefs);
                    this.restorePreferences(oPrefs);
                }
                if(message.equals("isEnabled")) {
                    Toast.makeText(getActivity(), "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                    preference_switch_bluetooth.setChecked(true);
                }
                if(message.equals("isEnabledFailed")) {
                    Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                    preference_switch_bluetooth.setChecked(false);
                }
                if(message.equals("isConnected")) {
                    Log.d(LOG_TAG, "Bluetooth Connected");
                    Toast.makeText(getActivity(), "Gear Fit Connected", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(true);
                }
                if(message.equals("isDisconnected")) {
                    Log.d(LOG_TAG, "Bluetooth Disconnected");
                    Toast.makeText(getActivity(), "Gear Fit Disconnected", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(false);
                }
                if(message.equals("isConnectedFailed")) {
                    Log.d(LOG_TAG, "Bluetooth Connected Failed");
                    Toast.makeText(getActivity(), "Gear Fit Connected failed", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(false);
                }
                if(message.equals("isConnectedRfcomm")) {
                    Log.d(LOG_TAG, "Bluetooth RFcomm Connected");
                    Toast.makeText(getActivity(), "Gear Fit Connected", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(true);
                }
                if(message.equals("isDisconnectedRfComm")) {
                    Log.d(LOG_TAG, "Bluetooth Disconnected");
                    Toast.makeText(getActivity(), "Gear Fit Disconnected", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(false);
                }
                if(message.equals("isConnectedRfcommFailed")) {
                    Log.d(LOG_TAG, "Bluetooth RFcomm Failed");
                    Toast.makeText(getActivity(), "Gear Fit Rfcomm Failed", Toast.LENGTH_SHORT).show();
                }
                if(message.equals("scanStopped")) {
                    Log.d(LOG_TAG, "Bluetooth scanning done");
                    preference_list_devices.setEnabled(true);
                    preference_scan.setSummary(R.string.preference_scan_summary);
                    Toast.makeText(getActivity(), "Scanning complete. Please select device", Toast.LENGTH_SHORT).show();
                }
                if(message.equals("bluetoothDevicesList")) {
                    Log.d(LOG_TAG, "Bluetooth device list");
                    CharSequence[] entries = intent.getCharSequenceArrayExtra("bluetoothEntries");
                    CharSequence[] entryValues = intent.getCharSequenceArrayExtra("bluetoothEntryValues");

                    /*for(int i = 0; i < entries.length; i++) {
                        Toast.makeText(getActivity(), "Entries "+i+": " +entries[i], Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, "entries" + entries[i]);
                    }
                    for(int i = 0; i < entryValues.length; i++) {
                        Toast.makeText(getActivity(), "Values "+i+": " +entryValues[i], Toast.LENGTH_SHORT).show();
                        Log.d(LOG_TAG, "entryValues" + entryValues[i]);
                    }*/


                    if(entries != null && entryValues != null) {
                        preference_list_devices.setEntries(entries);
                        preference_list_devices.setEntryValues(entryValues);
                    }
                    else {
                        Toast.makeText(getActivity(), "Error setting devices list", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        public CheckBoxPreference createAppPreference(final String packageName, final String appName, final boolean value) {
            CheckBoxPreference app = new CheckBoxPreference(getActivity());
            app.setTitle(appName);
            app.setKey(packageName);
            app.setChecked(value);
            app.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        oPrefs.saveBoolean(packageName, true);
                        Log.d(LOG_TAG, appName+": Enabled");
                        sendIntentListeningApps();
                        return true;
                    }
                    else {
                        oPrefs.saveBoolean(packageName, false);
                        Log.d(LOG_TAG, appName+": Disabled");
                        sendIntentListeningApps();
                        return true;
                    }
                }
            });
            app.setIcon(appManager.getIcon(packageName));
            return app;
        }

        public Preference createPlaceHolderPreference() {
            Preference ph = new Preference(getActivity());
            ph.setSummary("No applications added. Click the + icon at the top menu to add an application");
            ph.setKey("preference_apps_placeholder");
            return ph;
        }

        public void restorePreferences(OpenFitSavedPreferences oPrefs) {
            this.restoreDevicesList(oPrefs);
            this.restoreListeningApps(oPrefs);
            this.restoreDeviceListeners(oPrefs);
        }

        public void restoreDevicesList(OpenFitSavedPreferences oPrefs) {
            Log.d(LOG_TAG, "Resotoring devices list: " + oPrefs.preference_list_devices_value);
            if(oPrefs.preference_list_devices_value != "DEFAULT") {
                String mDeviceAddress = oPrefs.preference_list_devices_value;
                String mDeviceName = oPrefs.preference_list_devices_entry;
                preference_list_devices.setSummary(mDeviceName);
                sendIntent("bluetooth", "setEntries");
                sendIntent("bluetooth", "setDevice", mDeviceAddress);
                Log.d(LOG_TAG, "Restored device: "+mDeviceName+":"+mDeviceAddress);
            }
        }

        public void restoreListeningApps(OpenFitSavedPreferences oPrefs) {
            Log.d(LOG_TAG, "Restoring listening apps");
            PreferenceCategory category = (PreferenceCategory) findPreference("preference_category_apps");
            Set<String> listeningPackageNames = oPrefs.getSet();
            if(listeningPackageNames.size() <= 0) {
                Preference placeholder = createPlaceHolderPreference();
                category.addPreference(placeholder);
            }
            for(String packageName : listeningPackageNames) {
                boolean value = oPrefs.getBoolean(packageName);
                String appName = oPrefs.getString(packageName);
                Log.d(LOG_TAG, "Listening App: " + packageName + ":" + value);
                CheckBoxPreference app = createAppPreference(packageName, appName, value);
                category.addPreference(app);
                appManager.addInstalledApp(packageName);
            }
            sendIntentListeningApps();
        }

        public void restoreDeviceListeners(OpenFitSavedPreferences oPrefs) {
            Log.d(LOG_TAG, "Restoring device listeners");
            preference_checkbox_phone.setChecked(oPrefs.preference_checkbox_phone);
            preference_checkbox_sms.setChecked(oPrefs.preference_checkbox_sms);
            preference_checkbox_time.setChecked(oPrefs.preference_checkbox_time);
            preference_checkbox_weather.setChecked(oPrefs.preference_checkbox_weather);
            String sms = Boolean.toString(oPrefs.preference_checkbox_sms);
            String phone = Boolean.toString(oPrefs.preference_checkbox_phone);
            String weather = Boolean.toString(oPrefs.preference_checkbox_weather);
            sendIntent("bluetooth", "sms", sms);
            sendIntent("bluetooth", "phone", phone);
            sendIntent("bluetooth", "weather", weather);
            sendIntent("bluetooth", "status");
        }

        public void clearListeningApps(OpenFitSavedPreferences oPrefs) {
            Log.d(LOG_TAG, "Clearing listening apps");
            PreferenceCategory category = (PreferenceCategory) findPreference("preference_category_apps");
            category.removeAll();
            appManager.clearInstalledApp();
        }

        public void sendIntentListeningApps() {
            Log.d(LOG_TAG, "Sending Intent: listeningApps");
            Intent i = new Intent("listeningApps");
            i.putExtra("message", "listeningApps");
            i.putExtra("data", appManager.getInstalledApp());
            getActivity().sendBroadcast(i);
        }

        public void sendIntent(String intentName, String intentMsg) {
            Log.d(LOG_TAG, "Sending Intent: " + intentName + ":" + intentMsg);
            Intent i = new Intent(intentName);
            i.putExtra("message", intentMsg);
            getActivity().sendBroadcast(i);
        }

        public void sendIntent(String intentName, String intentMsg, String IntentData) {
            Log.d(LOG_TAG, "Sending Intent: " + intentName + ":" + intentMsg + ":" + IntentData);
            Intent i = new Intent(intentName);
            i.putExtra("message", intentMsg);
            i.putExtra("data", IntentData);
            getActivity().sendBroadcast(i);
        }

        @Override
        public void onDestroy() {
            Log.d(LOG_TAG, "onDestroy");
            this.getActivity().unregisterReceiver(bluetoothUIReceiver);
            this.getActivity().unregisterReceiver(stopServiceReceiver);
            this.getActivity().unregisterReceiver(notificationServiceReceiver);
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(addApplicationReceiver);
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(delApplicationReceiver);
            super.onDestroy();
        }

        // broadcast receivers
        private BroadcastReceiver addApplicationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String packageName = intent.getStringExtra("packageName");
                final String appName = intent.getStringExtra("appName");
                Log.d(LOG_TAG, "Recieved add application: "+appName+" : "+packageName);
                appManager.addInstalledApp(packageName);
                oPrefs.saveSet(packageName);
                oPrefs.saveBoolean(packageName, true);
                oPrefs.saveString(packageName, appName);
                CheckBoxPreference app = createAppPreference(packageName, appName, true);
                PreferenceCategory category = (PreferenceCategory) findPreference("preference_category_apps");
                Preference placeholder = category.findPreference("preference_apps_placeholder");
                if(placeholder != null) {
                    category.removePreference(placeholder);
                }
                category.addPreference(app);
                sendIntentListeningApps();
            }
        };

        private BroadcastReceiver delApplicationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                final String packageName = intent.getStringExtra("packageName");
                final String appName = intent.getStringExtra("appName");
                Log.d(LOG_TAG, "Recieved del application: "+appName+" : "+packageName);
                appManager.delInstalledApp(packageName);
                oPrefs.removeSet(packageName);
                oPrefs.removeBoolean(packageName);
                oPrefs.removeString(packageName);
                PreferenceCategory category = (PreferenceCategory) findPreference("preference_category_apps");
                CheckBoxPreference app = (CheckBoxPreference) findPreference(packageName);
                category.removePreference(app);
                sendIntentListeningApps();
            }
        };

        private BroadcastReceiver bluetoothUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String message = intent.getStringExtra("message");
                Log.d(LOG_TAG, "Recieved data to update UI: " + message);
                handleBluetoothMessage(message, intent);
            }
        };

        private BroadcastReceiver notificationServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "Received Notification Service");
                sendIntentListeningApps();
            }
        };

        private BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "Stopping Activity");
                getActivity().finish();
            }
        };
    }
}
