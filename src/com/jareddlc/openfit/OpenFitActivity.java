package com.jareddlc.openfit;

import java.util.Set;
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

        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");
        this.getFragmentManager().beginTransaction().replace(android.R.id.content, new OpenFitFragment()).commit();
        appManager = new ApplicationManager();
        appManager.getInstalledAdapter(getBaseContext());
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
        private static ListPreference preference_list_devices;
        private static Preference preference_scan;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Load the preferences from an XML resource
            Log.d(LOG_TAG, "adding preferences from resource");
            this.addPreferencesFromResource(R.xml.preferences);

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

            // load saved preferences
            oPrefs = new OpenFitSavedPreferences(getActivity());
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
                    return true;
                }
            });

            preference_list_devices = (ListPreference) getPreferenceManager().findPreference("preference_list_devices");
            preference_list_devices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    sendIntent("bluetooth", "setEntries");
                    return true;
                }
            });
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
            preference_checkbox_phone.setIcon(appManager.getDailerIcon());

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
            preference_checkbox_sms.setIcon(appManager.getSmsIcon());

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
            preference_checkbox_time.setIcon(appManager.getClockIcon());
        }

        public void handleBluetoothMessage(String message, Intent intent) {
            // setup message handler
            if(message != null && !message.isEmpty()) {
                if(message.equals("OpenFitService")) {
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
                    Toast.makeText(getActivity(), "Bluetooth Connected", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(true);
                }
                if(message.equals("isDisconnected")) {
                    Log.d(LOG_TAG, "Bluetooth Disconnected");
                    Toast.makeText(getActivity(), "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(false);
                }
                if(message.equals("isConnectedFailed")) {
                    Log.d(LOG_TAG, "Bluetooth Connected Failed");
                    Toast.makeText(getActivity(), "Bluetooth Connected failed", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(false);
                }
                if(message.equals("isConnectedRfcomm")) {
                    Log.d(LOG_TAG, "Bluetooth RFcomm Connected");
                    Toast.makeText(getActivity(), "Bluetooth Connected", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(true);
                }
                if(message.equals("isDisconnectedRfComm")) {
                    Log.d(LOG_TAG, "Bluetooth Disconnected");
                    Toast.makeText(getActivity(), "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(false);
                }
                if(message.equals("isConnectedRfcommFailed")) {
                    Log.d(LOG_TAG, "Bluetooth RFcomm Failed");
                    Toast.makeText(getActivity(), "Bluetooth Rfcomm Failed", Toast.LENGTH_SHORT).show();
                }
                if(message.equals("scanStopped")) {
                    Log.d(LOG_TAG, "Bluetooth scanning done");
                    sendIntent("bluetooth", "setEntries");
                    Toast.makeText(getActivity(), "Scanning complete. Please select device", Toast.LENGTH_SHORT).show();
                }
                if(message.equals("bluetoothDevicesList")) {
                    Log.d(LOG_TAG, "Bluetooth device list");
                    Toast.makeText(getActivity(), "Device list updated", Toast.LENGTH_SHORT).show();
                    preference_list_devices.setEntries(intent.getCharSequenceArrayExtra("bluetoothEntries"));
                    preference_list_devices.setEntryValues(intent.getCharSequenceArrayExtra("bluetoothEntryValues"));
                }
            }
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
                category.addPreference(app);
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
            }
        };

        private BroadcastReceiver bluetoothUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String message = intent.getStringExtra("message");
                Log.d(LOG_TAG, "Recieved bluetoothUI: " + message);
                handleBluetoothMessage(message, intent);
            }
        };

        private BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "Stopping Activity");
                getActivity().finish();
            }
        };

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
                        return true;
                    }
                    else {
                        oPrefs.saveBoolean(packageName, false);
                        Log.d(LOG_TAG, appName+": Disabled");
                        return true;
                    }
                }
            });
            app.setIcon(appManager.getIcon(packageName));
            return app;
        }

        public void restorePreferences(OpenFitSavedPreferences oPrefs) {
            this.restoreDevicesList(oPrefs);
            this.restoreListeningApps(oPrefs);
            this.restoreDeviceListeners(oPrefs);
        }

        public void restoreDevicesList(OpenFitSavedPreferences oPrefs) {
            Log.d(LOG_TAG, "Resotoring devices list");
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
            for(String packageName : listeningPackageNames) {
                boolean value = oPrefs.getBoolean(packageName);
                String appName = oPrefs.getString(packageName);
                Log.d(LOG_TAG, "Listening App: " + packageName + ":" + value);
                CheckBoxPreference app = createAppPreference(packageName, appName, value);
                category.addPreference(app);
                appManager.addInstalledApp(packageName);
            }
        }

        public void restoreDeviceListeners(OpenFitSavedPreferences oPrefs) {
            Log.d(LOG_TAG, "Restoring device listeners");
            preference_checkbox_phone.setChecked(oPrefs.preference_checkbox_phone);
            preference_checkbox_sms.setChecked(oPrefs.preference_checkbox_sms);
            preference_checkbox_time.setChecked(oPrefs.preference_checkbox_time);
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
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(addApplicationReceiver);
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(delApplicationReceiver);
            this.getActivity().unregisterReceiver(bluetoothUIReceiver);
            this.getActivity().unregisterReceiver(stopServiceReceiver);
            super.onDestroy();
        }
    }
}
