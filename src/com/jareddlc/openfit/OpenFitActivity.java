package com.jareddlc.openfit;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class OpenFitActivity extends Activity {
    private static final String LOG_TAG = "OpenFit:OpenFitActivity";

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
        Log.d(LOG_TAG, "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for(ApplicationInfo packageInfo : packages) {
            // filter out system apps
            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                Log.d(LOG_TAG, "Installed package :" + packageInfo.packageName);
            }
        }
        /*switch (item.getItemId()) {
            case R.id.new_game:
                newGame();
                return true;
            case R.id.help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }*/
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");
        getFragmentManager().beginTransaction().replace(android.R.id.content, new OpenFitFragment()).commit();
    }

    public static class OpenFitFragment extends PreferenceFragment {
        private static final String LOG_TAG = "OpenFit:OpenFitFragment";

        private String mDeviceName;
        private String mDeviceAddress;
        private static Handler mHandler;
        private static BluetoothLeService bluetoothLeService;

        // preferences
        private static SwitchPreference preference_switch_bluetooth;
        private static CheckBoxPreference preference_checkbox_connect;;
        private static ListPreference preference_list_devices;
        private static Preference preference_scan;
        private static Preference preference_test;
        private static Preference preference_foo;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            
            // Load the preferences from an XML resource
            Log.d(LOG_TAG, "adding preferences from resource");
            addPreferencesFromResource(R.xml.preferences);

            // load saved preferences
            final OpenFitSavedPreferences oPrefs = new OpenFitSavedPreferences(getActivity());
            
            // check to see if accessibility is enabled

            // setup message handler
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
            getActivity().bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(new Intent(getActivity(), NotificationService.class));

            // UI listeners
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
                    oPrefs.save("preference_list_devices_value", mDeviceAddress);
                    oPrefs.save("preference_list_devices_entry", mDeviceName);
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

            preference_test = (Preference) getPreferenceManager().findPreference("preference_test");
            preference_test.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), "Testing...", Toast.LENGTH_SHORT).show();
                    Log.d(LOG_TAG, "test");
                    bluetoothLeService.test();
                    return true;
                }
            });
        }
        
        public void updateDevices() {
            BluetoothLeService.setEntries();
            preference_list_devices.setEntries(BluetoothLeService.getEntries());
            preference_list_devices.setEntryValues(BluetoothLeService.getEntryValues());
        }

        public void restorePreferences(OpenFitSavedPreferences oPrefs) {
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
    }
}
