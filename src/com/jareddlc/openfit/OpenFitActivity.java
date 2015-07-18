package com.jareddlc.openfit;

import java.util.Set;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

        private String mDeviceName;
        private String mDeviceAddress;
        private  Handler mHandler;
        private static BluetoothLeService bluetoothLeService;
        private OpenFitSavedPreferences oPrefs;

        // UI preferences
        private static SwitchPreference preference_switch_bluetooth;
        private static CheckBoxPreference preference_checkbox_connect;
        private static CheckBoxPreference preference_checkbox_phone;
        private static CheckBoxPreference preference_checkbox_sms;
        private static CheckBoxPreference preference_checkbox_time;
        private static ListPreference preference_list_devices;
        private static Preference preference_scan;
        private static Preference preference_test;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Load the preferences from an XML resource
            Log.d(LOG_TAG, "adding preferences from resource");
            this.addPreferencesFromResource(R.xml.preferences);

            // load saved preferences
            oPrefs = new OpenFitSavedPreferences(getActivity());

            this.setupMessageHandler();

            // initialize BluetoothLE
            final ServiceConnection mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder service) {
                    Log.d(LOG_TAG, "onService Connected");
                    bluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
                    if(!bluetoothLeService.initialize()) {
                        Log.e(LOG_TAG, "Unable to initialize BluetoothLE");
                    }
                    bluetoothLeService.setHandler(mHandler);
                    restorePreferences(oPrefs);
                    
                    if(BluetoothLeService.isEnabled) {
                        preference_switch_bluetooth.setChecked(true);
                    }
                    else {
                        preference_switch_bluetooth.setChecked(false);
                    }
                    // Automatically connects to the device upon successful start-up initialization.
                    //bluetoothLeService.connect(mDeviceAddress);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                    bluetoothLeService = null;
                }
            };
            Intent gattServiceIntent = new Intent(this.getActivity(), BluetoothLeService.class);
            this.getActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            Intent notificationIntent = new Intent(this.getActivity(), NotificationService.class);
            this.getActivity().startService(notificationIntent);
            Intent serviceIntent = new Intent(this.getActivity(), OpenFitService.class);
            this.getActivity().startService(serviceIntent);
            //this.getActivity().stopService(notificationIntent);
            
            // App Listener 
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(addApplicationReceiver, new IntentFilter("addApplication"));
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(delApplicationReceiver, new IntentFilter("delApplication"));

            this.setupUIListeners();
        }

        public void setupMessageHandler() {
            // setup message handler
            Log.d(LOG_TAG, "Setting up message handler");
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.d(LOG_TAG, "handleMessage: "+msg.getData());
                    String bluetoothMessage = msg.getData().getString("bluetooth");
                    String bluetoothDevice = msg.getData().getString("bluetoothDevice");
                    if(bluetoothMessage != null && !bluetoothMessage.isEmpty()) {
                        if(bluetoothMessage.equals("isEnabled")) {
                            Toast.makeText(getActivity(), "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
                            preference_switch_bluetooth.setChecked(true);
                        }
                        if(bluetoothMessage.equals("isEnabledFailed")) {
                            Toast.makeText(getActivity(), "Failed", Toast.LENGTH_SHORT).show();
                            preference_switch_bluetooth.setChecked(false);
                        }
                        if(bluetoothMessage.equals("isConnected")) {
                            Log.d(LOG_TAG, "Bluetooth Connected");
                            Toast.makeText(getActivity(), "Bluetooth Connected", Toast.LENGTH_SHORT).show();
                            preference_checkbox_connect.setChecked(true);
                        }
                        if(bluetoothMessage.equals("isDisconnected")) {
                            Log.d(LOG_TAG, "Bluetooth Disconnected");
                            Toast.makeText(getActivity(), "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
                            preference_checkbox_connect.setChecked(false);
                        }
                        if(bluetoothMessage.equals("isConnectedFailed")) {
                            Log.d(LOG_TAG, "Bluetooth Connected Failed");
                            Toast.makeText(getActivity(), "Bluetooth Connected failed", Toast.LENGTH_SHORT).show();
                            preference_checkbox_connect.setChecked(false);
                        }
                        if(bluetoothMessage.equals("isConnectedRfcomm")) {
                            Log.d(LOG_TAG, "Bluetooth RFcomm Connected");
                            Toast.makeText(getActivity(), "Bluetooth Connected", Toast.LENGTH_SHORT).show();
                            preference_checkbox_connect.setChecked(true);
                        }
                        if(bluetoothMessage.equals("isDisconnectedRfComm")) {
                            Log.d(LOG_TAG, "Bluetooth Disconnected");
                            Toast.makeText(getActivity(), "Bluetooth Disconnected", Toast.LENGTH_SHORT).show();
                            preference_checkbox_connect.setChecked(false);
                        }
                        if(bluetoothMessage.equals("isConnectedRfcommFailed")) {
                            Log.d(LOG_TAG, "Bluetooth RFcomm Failed");
                            Toast.makeText(getActivity(), "Bluetooth Rfcomm Failed", Toast.LENGTH_SHORT).show();
                        }
                        if(bluetoothMessage.equals("scanStopped")) {
                            Log.d(LOG_TAG, "Bluetooth scanning done");
                            preference_list_devices.setEntries(BluetoothLeService.getEntries());
                            preference_list_devices.setEntryValues(BluetoothLeService.getEntryValues());
                            CharSequence text = "Scanning complete. Please select device";
                            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(bluetoothDevice != null && !bluetoothDevice.isEmpty()) {
                        String[] sDevice = bluetoothDevice.split(",");
                        String sDeviceName = sDevice[0];
                        String sDeviceAddress = sDevice[1];
                        Log.d(LOG_TAG, "Bluetooth device name: "+sDeviceName+" address: "+sDeviceAddress);
                    }
                }
            };
        }

        private void setupUIListeners() {
            // UI listeners
            Log.d(LOG_TAG, "Setting up UI Listeners");
            preference_switch_bluetooth = (SwitchPreference) getPreferenceManager().findPreference("preference_switch_bluetooth");
            preference_switch_bluetooth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        preference_switch_bluetooth.setChecked(false);
                        bluetoothLeService.enableBluetooth();
                        Toast.makeText(getActivity(), "Enabling...", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        bluetoothLeService.disableBluetooth();
                    }
                    return true;
                }
            });

            preference_scan = (Preference) getPreferenceManager().findPreference("preference_scan");
            preference_scan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), "Scanning...", Toast.LENGTH_SHORT).show();
                    bluetoothLeService.scanLeDevice();
                    return true;
                }
            });

            preference_list_devices = (ListPreference) getPreferenceManager().findPreference("preference_list_devices");
            preference_list_devices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    updateDevices();
                    return true;
                }
            });
            preference_list_devices.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mDeviceAddress = newValue.toString();
                    CharSequence[] entries = preference_list_devices.getEntries();
                    int index = preference_list_devices.findIndexOfValue(mDeviceAddress);
                    mDeviceName = entries[index].toString();
                    BluetoothLeService.setDevice(mDeviceAddress);
                    preference_list_devices.setSummary(mDeviceName);
                    oPrefs.saveString("preference_list_devices_value", mDeviceAddress);
                    oPrefs.saveString("preference_list_devices_entry", mDeviceName);
                    return true;
                }
            });

            preference_checkbox_connect = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_connect");
            preference_checkbox_connect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        //bluetoothLeService.connect(mDeviceAddress);
                        bluetoothLeService.connectRfcomm();
                        return false;
                    }
                    else {
                        //bluetoothLeService.disconnect();
                        bluetoothLeService.disconnectRfcomm();
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
                        return true;
                    }
                    else {
                        oPrefs.saveBoolean("preference_checkbox_phone", false);
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
                        return true;
                    }
                    else {
                        oPrefs.saveBoolean("preference_checkbox_sms", false);
                        return true;
                    }
                }
            });

            preference_checkbox_time = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_time");
            preference_checkbox_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        oPrefs.saveBoolean("preference_checkbox_time", true);
                        return true;
                    }
                    else {
                        oPrefs.saveBoolean("preference_checkbox_time", false);
                        return true;
                    }
                }
            });

            preference_test = (Preference) getPreferenceManager().findPreference("preference_test");
            preference_test.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), "Testing...", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "test");
                    return true;
                }
            });
        }

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

        public void updateDevices() {
            BluetoothLeService.setEntries();
            preference_list_devices.setEntries(BluetoothLeService.getEntries());
            preference_list_devices.setEntryValues(BluetoothLeService.getEntryValues());
        }

        public void restorePreferences(OpenFitSavedPreferences oPrefs) {
            this.restoreDevicesList(oPrefs);
            this.restoreListeningApps(oPrefs);
            this.restoreDeviceListeners(oPrefs);
        }

        public void restoreDevicesList(OpenFitSavedPreferences oPrefs) {
            Log.d(LOG_TAG, "Selected device: "+mDeviceName+":"+mDeviceAddress);
            if(oPrefs.preference_list_devices_value != "DEFAULT") {
                mDeviceAddress = oPrefs.preference_list_devices_value;
                mDeviceName = oPrefs.preference_list_devices_entry;
                preference_list_devices.setSummary(mDeviceName);
                updateDevices();
                BluetoothLeService.setDevice(mDeviceAddress);
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

        public void notifyService(String intentName) {
            Log.d(LOG_TAG, "Notifying Service");
            Intent msg = new Intent(intentName);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(msg);
        }

        @Override
        public void onDestroy() {
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(addApplicationReceiver);
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(delApplicationReceiver);
            super.onDestroy();
        }
    }
}
