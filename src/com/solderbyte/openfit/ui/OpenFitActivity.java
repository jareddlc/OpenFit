package com.solderbyte.openfit.ui;

import java.util.ArrayList;

import com.solderbyte.openfit.ApplicationManager;
import com.solderbyte.openfit.GoogleFit;
import com.solderbyte.openfit.OpenFitSavedPreferences;
import com.solderbyte.openfit.OpenFitService;
import com.solderbyte.openfit.PedometerData;
import com.solderbyte.openfit.PedometerTotal;
import com.solderbyte.openfit.R;
import com.solderbyte.openfit.util.OpenFitIntent;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class OpenFitActivity extends Activity {
    private static final String LOG_TAG = "OpenFit:OpenFitActivity";

    static ApplicationManager appManager;
    static GoogleFit googleFit;

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
            DialogAddApplication d = new DialogAddApplication(appManager.getInstalledAdapter(), appManager.getInstalledPackageNames(), appManager.getInstalledAppNames());
            d.show(getFragmentManager(), getString(R.string.menu_add));
        }
        if(item.getTitle().equals(getResources().getString(R.string.menu_del))) {
            Log.d(LOG_TAG, "Remove selected: "+ item);
            DialogDelApplication d = new DialogDelApplication(appManager.getNotificationAdapter(), appManager.getListeningPackageNames(), appManager.getListeningAppNames());
            d.show(getFragmentManager(), getString(R.string.menu_del));
        }
        if(item.getTitle().equals(getResources().getString(R.string.menu_help))) {
            Log.d(LOG_TAG, "Help selected");
            DialogHelp d = new DialogHelp();
            d.show(getFragmentManager(), getString(R.string.menu_help));
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appManager = new ApplicationManager();
        appManager.setContext(getBaseContext());

        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");

        this.getFragmentManager().beginTransaction().replace(android.R.id.content, new OpenFitFragment()).commitAllowingStateLoss();
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
        private static ListPreference preference_list_weather;
        private static ListPreference preference_list_devices;
        private static Preference preference_scan;
        private static Preference preference_fitness;
        private static Preference preference_donate;
        private static CheckBoxPreference preference_checkbox_googlefit;

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

            // load news
            DialogNews d = new DialogNews();
            d.show(getFragmentManager(), getString(R.string.dialog_title_news));

            // check notification access
            this.checkNotificationAccess();

            // start service
            Intent serviceIntent = new Intent(this.getActivity(), OpenFitService.class);
            this.getActivity().startService(serviceIntent);

            // App Listener 
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(addApplicationReceiver, new IntentFilter(OpenFitIntent.INTENT_UI_ADDAPPLICATION));
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(delApplicationReceiver, new IntentFilter(OpenFitIntent.INTENT_UI_DELAPPLICATION));
            this.getActivity().registerReceiver(btReceiver, new IntentFilter(OpenFitIntent.INTENT_UI_BT));
            this.getActivity().registerReceiver(serviceStopReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_STOP));
            this.getActivity().registerReceiver(serviceNotificationReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_NOTIFICATION));
        }

        @Override
        public void onResume() {
            Log.d(LOG_TAG, "onResume");
            super.onResume();
        }

        private void checkNotificationAccess() {
            ContentResolver contentResolver = getActivity().getContentResolver();
            String notificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
            String packageName = getActivity().getPackageName();

            if(notificationListeners == null || !notificationListeners.contains(packageName)){
                Log.d(LOG_TAG, "no Notification Access");
                DialogNotificationAccess d = new DialogNotificationAccess();
                d.show(getFragmentManager(), getString(R.string.dialog_title_notification_access));

            }
            else {
                Log.d(LOG_TAG, "Notification Access");
            }
        }

        private void setupUIListeners() {
            // UI listeners
            Log.d(LOG_TAG, "Setting up UI Listeners");
            preference_switch_bluetooth = (SwitchPreference) getPreferenceManager().findPreference("preference_switch_bluetooth");
            preference_switch_bluetooth.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_ENABLE);
                        preference_switch_bluetooth.setChecked(false);
                        Toast.makeText(getActivity(), R.string.toast_bluetooth_enable, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_DISABLE);
                    }
                    return true;
                }
            });

            preference_scan = (Preference) getPreferenceManager().findPreference("preference_scan");
            preference_scan.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_scan, Toast.LENGTH_SHORT).show();
                    sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_SCAN);
                    preference_list_devices.setEnabled(false);
                    preference_scan.setSummary(R.string.preference_scan_summary_scanning);
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
                    sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_SET_DEVICE, mDeviceAddress);
                    return true;
                }
            });

            preference_checkbox_connect = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_connect");
            preference_checkbox_connect.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        //String mDeviceAddress = oPrefs.preference_list_devices_value;
                        String mDeviceName = oPrefs.getString("preference_list_devices_entry");
                        Toast.makeText(getActivity(), getString(R.string.toast_bluetooth_connect) + " " + mDeviceName, Toast.LENGTH_SHORT).show();
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_CONNECT);
                        return false;
                    }
                    else {
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_DISCONNECT);
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
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_PHONE, OpenFitIntent.ACTION_TRUE);
                        return true;
                    }
                    else {
                        oPrefs.saveBoolean("preference_checkbox_phone", false);
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_PHONE, OpenFitIntent.ACTION_FALSE);
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
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_SMS, OpenFitIntent.ACTION_TRUE);
                        return true;
                    }
                    else {
                        oPrefs.saveBoolean("preference_checkbox_sms", false);
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_SMS, OpenFitIntent.ACTION_FALSE);
                        return true;
                    }
                }
            });

            preference_checkbox_time = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_time");
            preference_checkbox_time.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_TIME, OpenFitIntent.ACTION_TRUE);
                        oPrefs.saveBoolean("preference_checkbox_time", true);
                        return true;
                    }
                    else {
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_TIME, OpenFitIntent.ACTION_FALSE);
                        oPrefs.saveBoolean("preference_checkbox_time", false);
                        return true;
                    }
                }
            });

            preference_list_weather = (ListPreference) getPreferenceManager().findPreference("preference_list_weather");
            preference_list_weather.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String weatherValue = newValue.toString();
                    CharSequence[] entries = preference_list_weather.getEntries();
                    int index = preference_list_weather.findIndexOfValue(weatherValue);
                    String weatherName = entries[index].toString();
                    preference_list_weather.setSummary(weatherName);
                    oPrefs.saveString("preference_list_weather_value", weatherValue);
                    oPrefs.saveString("preference_list_weather_entry", weatherName);
                    sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_WEATHER, weatherValue);
                    return true;
                }
            });

            preference_fitness = (Preference) getPreferenceManager().findPreference("preference_fitness");
            preference_fitness.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_FITNESS);
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_fitness, Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            preference_checkbox_googlefit = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_googlefit");
            preference_checkbox_googlefit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        Intent i = new Intent(getActivity(), GoogleFit.class);
                        startActivity(i);

                        //oPrefs.saveBoolean("preference_checkbox_googlefit", true);
                        return true;
                    }
                    else {
                        //oPrefs.saveBoolean("preference_checkbox_googlefit", false);
                        return true;
                    }
                }
            });
            preference_checkbox_googlefit.setEnabled(false);

            preference_donate = (Preference) getPreferenceManager().findPreference("preference_donate");
            preference_donate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=2PLHGNYFEUYK8&lc=US&item_name=Open%20Fit%20Donations&item_number=Open%20Fit%20Donation&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted"));
                    //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.me/jareddlc"));
                    startActivity(browserIntent);
                    return true;
                }
            });
        }

        public void handleServiceMessage(String message, Intent intent) {
            // setup message handler
            if(message != null && !message.isEmpty()) {
                if(message.equals(OpenFitIntent.INTENT_SERVICE_START)) {
                }
                if(message.equals(OpenFitIntent.EXTRA_IS_ENABLED)) {
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_enabled, Toast.LENGTH_SHORT).show();
                    preference_switch_bluetooth.setChecked(true);
                }
                if(message.equals(OpenFitIntent.EXTRA_IS_ENABLED_FAILED)) {
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_enabled_failed, Toast.LENGTH_SHORT).show();
                    preference_switch_bluetooth.setChecked(false);
                }
                if(message.equals(OpenFitIntent.EXTRA_IS_CONNECTED)) {
                    Log.d(LOG_TAG, "Bluetooth Connected");
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_connected, Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(true);
                }
                if(message.equals(OpenFitIntent.EXTRA_IS_DISCONNCTED)) {
                    Log.d(LOG_TAG, "Bluetooth Disconnected");
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_disconnected, Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(false);
                }
                if(message.equals(OpenFitIntent.EXTRA_IS_CONNECTED_FAILED)) {
                    Log.d(LOG_TAG, "Bluetooth Connected Failed");
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_connect_failed, Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(false);
                }
                if(message.equals(OpenFitIntent.EXTRA_IS_CONNECTED_RFCOMM)) {
                    Log.d(LOG_TAG, "Bluetooth RFcomm Connected");
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_connected, Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(true);
                }
                if(message.equals(OpenFitIntent.EXTRA_IS_DISCONNECTED_RFCOMM)) {
                    Log.d(LOG_TAG, "Bluetooth Disconnected");
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_disconnected, Toast.LENGTH_SHORT).show();
                    preference_checkbox_connect.setChecked(false);
                }
                if(message.equals(OpenFitIntent.EXTRA_IS_CONNECTED_RFCOMM_FAILED)) {
                    Log.d(LOG_TAG, "Bluetooth RFcomm Failed");
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_connect_failed, Toast.LENGTH_SHORT).show();
                }
                if(message.equals(OpenFitIntent.EXTRA_SCAN_STOPPED)) {
                    Log.d(LOG_TAG, "Bluetooth scanning done");
                    preference_list_devices.setEnabled(true);
                    preference_scan.setSummary(R.string.preference_scan_summary);
                    Toast.makeText(getActivity(), R.string.toast_bluetooth_scan_complete, Toast.LENGTH_SHORT).show();
                }
                if(message.equals(OpenFitIntent.EXTRA_BLUETOOTH_DEVICE_LIST)) {
                    Log.d(LOG_TAG, "Bluetooth device list");
                    CharSequence[] entries = intent.getCharSequenceArrayExtra(OpenFitIntent.EXTRA_BLUETOOTH_ENTRIES);
                    CharSequence[] entryValues = intent.getCharSequenceArrayExtra(OpenFitIntent.EXTRA_BLUETOOTH_ENTRIES_VALUES);

                    if(entries != null && entryValues != null) {
                        preference_list_devices.setEntries(entries);
                        preference_list_devices.setEntryValues(entryValues);
                    }
                    else {
                        Toast.makeText(getActivity(), R.string.toast_bluetooth_devices_failed, Toast.LENGTH_SHORT).show();
                    }
                }
                if(message.equals(OpenFitIntent.EXTRA_DEVICE_NAME)) {
                    Log.d(LOG_TAG, "Bluetooth device name");
                    String mDeviceName = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_DATA);
                    preference_list_devices.setSummary(mDeviceName);
                }

                if(message.equals(OpenFitIntent.EXTRA_SMS)) {
                    Log.d(LOG_TAG, "SMS");
                    Boolean data = intent.getBooleanExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                    preference_checkbox_sms.setChecked(data);
                }
                if(message.equals(OpenFitIntent.EXTRA_PHONE)) {
                    Log.d(LOG_TAG, "Phone");
                    Boolean data = intent.getBooleanExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                    preference_checkbox_phone.setChecked(data);
                }
                if(message.equals(OpenFitIntent.EXTRA_TIME)) {
                    Log.d(LOG_TAG, "Time");
                    Boolean data = intent.getBooleanExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                    preference_checkbox_time.setChecked(data);
                }
                if(message.equals(OpenFitIntent.EXTRA_WEATHER)) {
                    Log.d(LOG_TAG, "Weather");
                    String entry = intent.getStringExtra(OpenFitIntent.EXTRA_WEATHER_ENTRY);
                    String value = intent.getStringExtra(OpenFitIntent.EXTRA_WEATHER_VALUE);
                    preference_list_weather.setValue(value);
                    preference_list_weather.setSummary(entry);
                }
                if(message.equals(OpenFitIntent.EXTRA_FITNESS)) {
                    Log.d(LOG_TAG, "Fitness");
                    PedometerTotal pedometerTotal = intent.getParcelableExtra(OpenFitIntent.EXTRA_PEDOMETER_TOTAL);
                    ArrayList<PedometerData> pedometerList = intent.getParcelableArrayListExtra(OpenFitIntent.EXTRA_PEDOMETER_LIST);
                    ArrayList<PedometerData> pedometerDailyList = intent.getParcelableArrayListExtra(OpenFitIntent.EXTRA_PEDOMETER_DAILY_LIST);

                    DialogFitness d = new DialogFitness(getActivity(), pedometerDailyList, pedometerList, pedometerTotal);
                    d.show(getFragmentManager(), OpenFitIntent.EXTRA_FITNESS);
                }
                if(message.equals(OpenFitIntent.EXTRA_APPLICATIONS)) {
                    Log.d(LOG_TAG, "Applications");
                    ArrayList<String> listeningListPackageNames = intent.getStringArrayListExtra(OpenFitIntent.EXTRA_APPLICATIONS_PACKAGE_NAME);
                    ArrayList<String> listeningListAppNames = intent.getStringArrayListExtra(OpenFitIntent.EXTRA_APPLICATIONS_APP_NAME);

                    PreferenceCategory category = (PreferenceCategory) findPreference("preference_category_apps");
                    if(listeningListPackageNames.size() <= 0) {
                        Preference placeholder = createPlaceHolderPreference();
                        category.addPreference(placeholder);
                    }
                    for(int i = 0; i < listeningListPackageNames.size(); i++) {
                        Preference app = createAppPreference(listeningListPackageNames.get(i), listeningListAppNames.get(i));
                        category.addPreference(app);
                        appManager.addNotificationApp(listeningListPackageNames.get(i));
                    }
                    sendNotificationApplications();
                }
            }
        }

        public Preference createAppPreference(final String packageName, final String appName) {
            Preference app = new Preference(getActivity());
            app.setTitle(appName);
            app.setKey(packageName);
            app.setIcon(appManager.loadIcon(packageName));
            return app;
        }

        public Preference createPlaceHolderPreference() {
            Preference ph = new Preference(getActivity());
            ph.setSummary(R.string.preference_applications_summary);
            ph.setKey("preference_apps_placeholder");
            return ph;
        }

        public void clearNotificationApplications() {
            Log.d(LOG_TAG, "Clearing listening apps");
            PreferenceCategory category = (PreferenceCategory) findPreference("preference_category_apps");
            category.removeAll();
            appManager.clearNotificationApplications();
        }

        public void sendNotificationApplications() {
            Log.d(LOG_TAG, "Sending Intent: " + OpenFitIntent.INTENT_SERVICE_NOTIFICATION_APPLICATIONS);
            Intent i = new Intent(OpenFitIntent.INTENT_SERVICE_NOTIFICATION_APPLICATIONS);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_SERVICE_NOTIFICATION_APPLICATIONS);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, appManager.getNotificationApplications());
            getActivity().sendBroadcast(i);
        }

        public void sendIntent(String intentName) {
            Log.d(LOG_TAG, "Sending Intent: " + intentName);
            Intent i = new Intent(intentName);
            getActivity().sendBroadcast(i);
        }

        public void sendIntent(String intentName, String intentMsg) {
            Log.d(LOG_TAG, "Sending Intent: " + intentName + ":" + intentMsg);
            Intent i = new Intent(intentName);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, intentMsg);
            getActivity().sendBroadcast(i);
        }

        public void sendIntent(String intentName, String intentMsg, String IntentData) {
            Log.d(LOG_TAG, "Sending Intent: " + intentName + ":" + intentMsg + ":" + IntentData);
            Intent i = new Intent(intentName);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, intentMsg);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, IntentData);
            getActivity().sendBroadcast(i);
        }

        @Override
        public void onDestroy() {
            Log.d(LOG_TAG, "onDestroy");
            this.getActivity().unregisterReceiver(btReceiver);
            this.getActivity().unregisterReceiver(serviceStopReceiver);
            this.getActivity().unregisterReceiver(serviceNotificationReceiver);
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(addApplicationReceiver);
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(delApplicationReceiver);
            super.onDestroy();
        }

        // broadcast receivers
        private BroadcastReceiver addApplicationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String packageName = intent.getStringExtra(OpenFitIntent.EXTRA_PACKAGE_NAME);
                final String appName = intent.getStringExtra(OpenFitIntent.EXTRA_APP_NAME);
                Log.d(LOG_TAG, "Recieved add application: "+appName+" : "+packageName);
                appManager.addNotificationApp(packageName);
                oPrefs.saveSet(packageName);
                oPrefs.saveString(packageName, appName);
                Preference app = createAppPreference(packageName, appName);
                PreferenceCategory category = (PreferenceCategory) findPreference("preference_category_apps");
                Preference placeholder = category.findPreference("preference_apps_placeholder");
                if(placeholder != null) {
                    category.removePreference(placeholder);
                }
                category.addPreference(app);
                sendNotificationApplications();
            }
        };

        private BroadcastReceiver delApplicationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                final String packageName = intent.getStringExtra(OpenFitIntent.EXTRA_PACKAGE_NAME);
                final String appName = intent.getStringExtra(OpenFitIntent.EXTRA_APP_NAME);
                Log.d(LOG_TAG, "Recieved del application: "+appName+" : "+packageName);
                appManager.delNotificationApp(packageName);
                oPrefs.removeSet(packageName);
                oPrefs.removeString(packageName);
                PreferenceCategory category = (PreferenceCategory) findPreference("preference_category_apps");
                Preference app = (Preference) findPreference(packageName);
                category.removePreference(app);
                sendNotificationApplications();
            }
        };

        private BroadcastReceiver btReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String message = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_MSG);
                Log.d(LOG_TAG, "Received Service Command: " + message);
                handleServiceMessage(message, intent);
            }
        };

        private BroadcastReceiver serviceNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "Received Notification Service");
                sendNotificationApplications();
            }
        };

        private BroadcastReceiver serviceStopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(LOG_TAG, "Stopping Activity");
                getActivity().finish();
            }
        };
    }
}
