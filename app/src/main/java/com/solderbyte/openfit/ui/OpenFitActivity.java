package com.solderbyte.openfit.ui;

import java.util.ArrayList;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.solderbyte.openfit.ApplicationManager;
import com.solderbyte.openfit.Billing;
import com.solderbyte.openfit.BuildConfig;
import com.solderbyte.openfit.ExerciseData;
import com.solderbyte.openfit.GoogleFit;
import com.solderbyte.openfit.HeartRateData;
import com.solderbyte.openfit.SleepInfo;
import com.solderbyte.openfit.OpenFitSavedPreferences;
import com.solderbyte.openfit.OpenFitService;
import com.solderbyte.openfit.PedometerData;
import com.solderbyte.openfit.PedometerTotal;
import com.solderbyte.openfit.ProfileData;
import com.solderbyte.openfit.R;
import com.solderbyte.openfit.SleepData;
import com.solderbyte.openfit.StartOpenFitAtBootReceiver;
import com.solderbyte.openfit.util.OpenFitIntent;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.TextView;
import android.widget.Toast;

public class OpenFitActivity extends Activity {
    private static final String LOG_TAG = "OpenFit:OpenFitActivity";

    static ApplicationManager appManager;
    private static GoogleFit gFit = null;
    private static Billing billing = null;

    private static IInAppBillingService billingService;
    private static GoogleApiClient mClient = null;
    private static final int REQUEST_OAUTH = 1;
    private static final int BILLING_REQ = 1001;
    private static boolean GFIT_CONNECTED = false;

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

        // load applications
        appManager = new ApplicationManager();
        appManager.setContext(getBaseContext());

        // load billing
        billing = new Billing();
        billing.setContext(getBaseContext());
        billing.setActivity(this);
        connectBillingService();

        // load google fit
        buildFitnessClient();

        // load the PreferenceFragment
        Log.d(LOG_TAG, "Loading PreferenceFragment");

        this.getFragmentManager().beginTransaction().replace(android.R.id.content, new OpenFitFragment()).commitAllowingStateLoss();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy Activity");
        super.onDestroy();
        if(billingService != null) {
            unbindService(billingServiceConnection);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult");
        if(requestCode == REQUEST_OAUTH) {
            if(resultCode == RESULT_OK) {
                if(!mClient.isConnecting() && !mClient.isConnected()) {
                    this.connectGoogleFit();
                }
            }
        }

        if(requestCode == BILLING_REQ) {
            Log.d(LOG_TAG, "BILLING onActivityResult");
            if(resultCode == RESULT_OK) {
                Log.d(LOG_TAG, "Purchased");
                Toast.makeText(this, R.string.toast_premium_purchased, Toast.LENGTH_SHORT).show();
                DialogPurchase d = new DialogPurchase();
                d.show(getFragmentManager(), getString(R.string.dialog_title_purchase));
                billing.verifyPremium();
            }
        }
    }

    public void connectGoogleFit() {
        if(!mClient.isConnecting() && !mClient.isConnected()) {
            Log.d(LOG_TAG, "Connecting to GoogleFit");
            mClient.connect();
        }
        else {
            Log.d(LOG_TAG, "GoogleFit already connected: " + GFIT_CONNECTED);
        }
    }

    public void connectBillingService() {
        Intent billingServiceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        billingServiceIntent.setPackage("com.android.vending");
        bindService(billingServiceIntent, billingServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public ServiceConnection billingServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "Billing service disconnected");
            billingService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "Billing service connected");
            billingService = IInAppBillingService.Stub.asInterface(service);
            billing.setService(billingService);
            billing.getSkuDetails();
            billing.verifyPremium();
        }
    };

    public static class OpenFitFragment extends PreferenceFragment {
        private static final String LOG_TAG = "OpenFit:OpenFitFragment";
        private static final String PREFERENCE_LAST_VERSION_KEY = "com.solderbyte.openfit.PREFERENCE_LAST_VERSION_KEY";
        public static final String PREFERENCE_SKIP_CHANGELOG_KEY = "com.solderbyte.openfit.PREFERENCE_SKIP_CHANGELOG";

        private OpenFitSavedPreferences oPrefs;
        private ProgressDialog progressDailog = null;

        // UI preferences
        private static SwitchPreference preference_switch_bluetooth;
        private static CheckBoxPreference preference_checkbox_exercise_gps;
        private static CheckBoxPreference preference_checkbox_connect;
        private static CheckBoxPreference preference_checkbox_phone;
        private static CheckBoxPreference preference_checkbox_sms;
        private static CheckBoxPreference preference_checkbox_time;
        private static CheckBoxPreference preference_checkbox_googlefit;
        private static CheckBoxPreference preference_autostart_at_boot;
        private static CheckBoxPreference preference_checkbox_mediacontroller;
        private static ListPreference preference_list_weather;
        private static ListPreference preference_list_devices;
        private static Preference preference_scan;
        private static Preference preference_fitness;
        private static Preference preference_edit_reject_messages;
        private static Preference preference_apps_placeholder;
        //private static Preference preference_donate;
        private static Preference preference_purchase;

        private static boolean fitnessRequeted = false;

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

            // Show the changelog dialog
            this.showChangelog();

            // check notification access
            this.checkNotificationAccess();

            // start google fit
            this.restoreGoogleFit();

            // start service
            Intent serviceIntent = new Intent(this.getActivity(), OpenFitService.class);
            this.getActivity().startService(serviceIntent);

            // App Listener
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(addApplicationReceiver, new IntentFilter(OpenFitIntent.INTENT_UI_ADDAPPLICATION));
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(delApplicationReceiver, new IntentFilter(OpenFitIntent.INTENT_UI_DELAPPLICATION));
            LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(rejectMessagesReceiver, new IntentFilter(OpenFitIntent.INTENT_UI_REJECTMESSAGES));
            this.getActivity().registerReceiver(btReceiver, new IntentFilter(OpenFitIntent.INTENT_UI_BT));
            this.getActivity().registerReceiver(serviceStopReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_STOP));
            this.getActivity().registerReceiver(serviceNotificationReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_NOTIFICATION));
            this.getActivity().registerReceiver(googleFitReceiver, new IntentFilter(OpenFitIntent.INTENT_GOOGLE_FIT));
            this.getActivity().registerReceiver(billingReceiver, new IntentFilter(OpenFitIntent.INTENT_BILLING));
        }

        private void showChangelog(){
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);


            // Check if the changelog has to be skipped (ie "Don't show again" has been checked)
            boolean skipChangelog = sharedPref.getBoolean(PREFERENCE_SKIP_CHANGELOG_KEY, false);

            // Get versionCode numbers of the app (current and last)
            int lastVersion = sharedPref.getInt(PREFERENCE_LAST_VERSION_KEY, 0);
            int thisVersion = BuildConfig.VERSION_CODE;

            // Reinit skipChangelog if the app has been updated since last start (or first start)
            if(thisVersion != lastVersion){
                SharedPreferences.Editor sharedPrefEditor = sharedPref.edit();

                // Reinitialize the skipChangelog preference
                skipChangelog = false;
                sharedPrefEditor.putBoolean(PREFERENCE_SKIP_CHANGELOG_KEY, false);

                // Set last version
                sharedPrefEditor.putInt(PREFERENCE_LAST_VERSION_KEY, thisVersion);

                sharedPrefEditor.apply();
            }

            if(!skipChangelog) {
                // load news
                DialogNews d = new DialogNews();
                d.show(getFragmentManager(), getString(R.string.dialog_title_news));
            }
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
                    if ((Boolean) newValue) {
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
                    if ((Boolean) newValue) {
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

            preference_edit_reject_messages = (Preference) getPreferenceManager().findPreference("preference_edit_reject_messages");
            preference_edit_reject_messages.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    OpenFitSavedPreferences oPrefs = new OpenFitSavedPreferences(getActivity());
                    ArrayList<String> msgList = new ArrayList<String>();
                    for(int i = 0; i < oPrefs.getInt("reject_messages_size"); ++i) {
                        msgList.add(oPrefs.getString("reject_message_" + i));
                    }
                    DialogRejectMessages d = new DialogRejectMessages(msgList);
                    d.show(getFragmentManager(), getString(R.string.dialog_edit_rejectcall_messages));
                    return true;
                }
            });

            preference_checkbox_sms = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_sms");
            preference_checkbox_sms.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue) {
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
                    fitnessRequeted = true;
                    return true;
                }
            });

            preference_checkbox_googlefit = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_googlefit");
            preference_checkbox_googlefit.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean) newValue) {
                        Toast.makeText(getActivity(), R.string.toast_google_fit_connect, Toast.LENGTH_SHORT).show();
                        connectGoogleFit();
                        return false;
                    }
                    else {
                        Toast.makeText(getActivity(), R.string.toast_google_fit_disconnect, Toast.LENGTH_SHORT).show();
                        disconnectGoogleFit();
                        return false;
                    }
                }
            });

            preference_checkbox_exercise_gps = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_exercise_gps");
            preference_checkbox_exercise_gps.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_GPS, OpenFitIntent.ACTION_TRUE);
                        oPrefs.saveBoolean("preference_checkbox_exercise_gps", true);
                        return true;
                    }
                    else {
                        sendIntent(OpenFitIntent.INTENT_SERVICE_BT, OpenFitIntent.ACTION_GPS, OpenFitIntent.ACTION_FALSE);
                        oPrefs.saveBoolean("preference_checkbox_exercise_gps", false);
                        return true;
                    }
                }
            });

            preference_checkbox_mediacontroller = (CheckBoxPreference) getPreferenceManager().findPreference("preference_checkbox_mediacontroller");
            preference_checkbox_mediacontroller.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if((Boolean)newValue) {
                        sendIntent(OpenFitIntent.INTENT_MEDIACONTROLLER_METHOD, OpenFitIntent.ACTION_TRUE);

                        oPrefs.saveBoolean("preference_checkbox_mediacontroller", true);
                        return true;
                    }
                    else {
                        sendIntent(OpenFitIntent.INTENT_MEDIACONTROLLER_METHOD, OpenFitIntent.ACTION_FALSE);
                        oPrefs.saveBoolean("preference_checkbox_mediacontroller", false);
                        return true;
                    }
                }
            });

            preference_apps_placeholder = getPreferenceManager().findPreference("preference_apps_placeholder");

            /*
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
            */

            preference_autostart_at_boot = (CheckBoxPreference) getPreferenceManager().findPreference("preference_autostart_at_boot");
            preference_autostart_at_boot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ComponentName receiver = new ComponentName(getActivity(), StartOpenFitAtBootReceiver.class);
                    PackageManager pm = getActivity().getPackageManager();

                    if((Boolean)newValue) {
                        pm.setComponentEnabledSetting(receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP);
                    }
                    else {
                        pm.setComponentEnabledSetting(receiver,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP);
                    }
                    return true;
                }
            });

            preference_purchase = (Preference) getPreferenceManager().findPreference("preference_purchase");
            preference_purchase.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(billing != null) {
                        //startIntentSenderForResult(pendingIntent.getIntentSender(), 1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                        try {
                            billing.purchasePremium();
                        }
                        catch(Exception e) {
                            Log.d(LOG_TAG, "Error: " + e.getMessage());
                        }
                    }
                    return true;
                }
            });
        }

        public void handleServiceMessage(String message, Intent intent) {
            // setup message handler
            if(message != null && !message.isEmpty()) {
                if(message.equals(OpenFitIntent.INTENT_SERVICE_START)) {
                    Log.d(LOG_TAG, "Service started");

                    if(mClient != null) {
                        Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                        msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT);
                        msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
                        getActivity().sendBroadcast(msg);
                    }
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
                if(message.equals(OpenFitIntent.EXTRA_GPS)) {
                    Log.d(LOG_TAG, "GPS");
                    Boolean data = intent.getBooleanExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                    preference_checkbox_exercise_gps.setChecked(data);
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
                    ArrayList<ExerciseData> exerciseDataList = intent.getParcelableArrayListExtra(OpenFitIntent.EXTRA_EXERCISE_LIST);
                    ArrayList<SleepData> sleepList = intent.getParcelableArrayListExtra(OpenFitIntent.EXTRA_SLEEP_LIST);
                    ArrayList<SleepInfo> sleepInfoList = intent.getParcelableArrayListExtra(OpenFitIntent.EXTRA_SLEEP_INFO_LIST);
                    ArrayList<HeartRateData> heartRateList = intent.getParcelableArrayListExtra(OpenFitIntent.EXTRA_HEARTRATE_LIST);
                    ProfileData profileData = intent.getParcelableExtra(OpenFitIntent.EXTRA_PROFILE_DATA);
                    if(gFit != null) {
                        gFit.setData(pedometerList, exerciseDataList, sleepList, sleepInfoList, heartRateList, profileData);
                    }

                    if(fitnessRequeted) {
                        DialogFitness d = new DialogFitness(getActivity(), pedometerDailyList, pedometerList, pedometerTotal, exerciseDataList, sleepList, heartRateList, profileData);
                        d.show(getFragmentManager(), OpenFitIntent.EXTRA_FITNESS);
                        fitnessRequeted = false;
                    }
                }
                if(message.equals(OpenFitIntent.EXTRA_APPLICATIONS)) {
                    Log.d(LOG_TAG, "Applications");
                    ArrayList<String> listeningListPackageNames = intent.getStringArrayListExtra(OpenFitIntent.EXTRA_APPLICATIONS_PACKAGE_NAME);
                    ArrayList<String> listeningListAppNames = intent.getStringArrayListExtra(OpenFitIntent.EXTRA_APPLICATIONS_APP_NAME);

                    PreferenceCategory category = (PreferenceCategory) findPreference("preference_category_apps");
                    if(listeningListPackageNames.size() <= 0) {
                        category.addPreference(preference_apps_placeholder);
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

        public void restoreGoogleFit() {
            boolean preference_checkbox_googlefit = oPrefs.getBoolean("preference_checkbox_googlefit");
            if(preference_checkbox_googlefit) {
                connectGoogleFit();
            }
        }

        public void connectGoogleFit() {
            if(mClient == null) {
                Log.d(LOG_TAG, "GoogleFit is null");
                return;
            }
            if(!mClient.isConnecting() && !mClient.isConnected()) {
                Log.d(LOG_TAG, "Connecting to GoogleFit");
                mClient.connect();
            }
            else {
                Log.d(LOG_TAG, "GoogleFit already connected: " + GFIT_CONNECTED);
            }
        }

        public void disconnectGoogleFit() {
            if(mClient.isConnected()) {
                Log.d(LOG_TAG, "Disconnecting to GoogleFit");
                PendingResult<Status> pendingResult = Fitness.ConfigApi.disableFit(mClient);

                pendingResult.setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if(status.isSuccess()) {
                            GFIT_CONNECTED = false;
                            Log.d(LOG_TAG, "Google Fit disabled");
                            Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                            msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT);
                            msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                            getActivity().sendBroadcast(msg);

                            mClient.disconnect();
                        }
                        else {
                            Log.e(LOG_TAG, "Google Fit wasn't disabled " + status);
                        }
                    }
                });
            }
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
            Log.d(LOG_TAG, "onDestroy prefrenceFragement");
            this.getActivity().unregisterReceiver(btReceiver);
            this.getActivity().unregisterReceiver(serviceStopReceiver);
            this.getActivity().unregisterReceiver(serviceNotificationReceiver);
            this.getActivity().unregisterReceiver(googleFitReceiver);
            this.getActivity().unregisterReceiver(billingReceiver);
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(addApplicationReceiver);
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(delApplicationReceiver);
            LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(rejectMessagesReceiver);

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
                category.removePreference(preference_apps_placeholder);
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
                // If no more preference in the category, restore the placeholder
                if(category.getPreferenceCount() <= 0) {
                    category.addPreference(preference_apps_placeholder);
                }
                sendNotificationApplications();
            }
        };

        private BroadcastReceiver rejectMessagesReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final ArrayList<String> msgList = intent.getStringArrayListExtra(OpenFitIntent.EXTRA_REJECT_MESSAGES);
                Log.d(LOG_TAG, "REJECT MESSAGES");
                int msgSize = oPrefs.getInt("reject_messages_size");
                for(int i = 0; i < msgSize; ++i) {
                    oPrefs.removeString("reject_message_" + i);
                }
                for(int i = 0; i < msgList.size(); ++i) {
                    oPrefs.saveString("reject_message_" + i, msgList.get(i));
                    Log.d(LOG_TAG, i + ". reject message: " + msgList.get(i));
                }
                oPrefs.saveInt("reject_messages_size", msgList.size());

                Log.d(LOG_TAG, "Reject messages size: " + oPrefs.getInt("reject_messages_size"));
                Intent msg = new Intent(OpenFitIntent.INTENT_REJECTMESSAGES_SAVE);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
                getActivity().sendBroadcast(msg);
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

        private BroadcastReceiver googleFitReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String message = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_MSG);
                Log.d(LOG_TAG, "Received Google Fit: " + message);
                if(message.equals(OpenFitIntent.INTENT_GOOGLE_FIT)) {
                    Boolean enabled = intent.getBooleanExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                    if(enabled) {
                        Log.d(LOG_TAG, "Google Fit Enabled");
                        preference_checkbox_googlefit.setChecked(true);
                        oPrefs.saveBoolean("preference_checkbox_googlefit", true);
                    }
                    else {
                        Log.d(LOG_TAG, "Google Fit Disabled");
                        preference_checkbox_googlefit.setChecked(false);
                        oPrefs.saveBoolean("preference_checkbox_googlefit", false);
                    }
                }
                if(message.equals(OpenFitIntent.INTENT_GOOGLE_FIT_SYNC)) {
                    Log.d(LOG_TAG, "Google Fit Sync requested");
                    if(mClient.isConnected()) {
                        Toast.makeText(getActivity(), R.string.toast_google_fit_sync, Toast.LENGTH_SHORT).show();
                        progressDailog = new ProgressDialog(getActivity());
                        progressDailog.setMessage(getString(R.string.progress_dialog_syncing));
                        progressDailog.show();
                    }
                    else {
                        Log.d(LOG_TAG, "Google Fit Sync not connected");
                        Toast.makeText(getActivity(), R.string.toast_google_fit_sync_failure, Toast.LENGTH_SHORT).show();
                    }
                }
                if(message.equals(OpenFitIntent.INTENT_GOOGLE_FIT_SYNC_STATUS)) {
                    Boolean status = intent.getBooleanExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                    String info = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_INFO);
                    if(progressDailog != null) {
                        progressDailog.dismiss();
                    }
                    if(status) {
                        Log.d(LOG_TAG, "Google Fit Sync completed");
                        Toast.makeText(getActivity(), R.string.toast_google_fit_sync_success, Toast.LENGTH_SHORT).show();
                    }
                    else if(info != null && info.equals(OpenFitIntent.INTENT_BILLING_NO_PURCHASE)) {
                        Log.d(LOG_TAG, "Google Fit Sync failed, no premium");
                        Toast.makeText(getActivity(), R.string.toast_google_fit_sync_no_purchase, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.d(LOG_TAG, "Google Fit Sync failed");
                        Toast.makeText(getActivity(), R.string.toast_google_fit_sync_failure, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        private BroadcastReceiver billingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String message = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_MSG);
                Log.d(LOG_TAG, "Received Billing: " + message);

                if(message.equals(OpenFitIntent.INTENT_BILLING_VERIFIED)) {
                    Boolean verified = intent.getBooleanExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                    if(verified) {
                        Log.d(LOG_TAG, "Premium purchased");
                        preference_purchase.setEnabled(false);
                        preference_purchase.setSummary(R.string.preference_purchased_summary);
                        oPrefs.saveBoolean("preference_purchase", true);
                    }
                    else {
                        Log.d(LOG_TAG, "Premium not purchased");
                        preference_purchase.setEnabled(true);
                        oPrefs.saveBoolean("preference_purchase", false);
                    }
                }
            }
        };
    }

    // Google Fit
    private void buildFitnessClient() {
        mClient = new GoogleApiClient.Builder(this)
        .addApi(Fitness.CONFIG_API)
        .addApi(Fitness.HISTORY_API)
        .addApi(Fitness.SESSIONS_API)
        .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
        .addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
        .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
        .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Log.d(LOG_TAG, "Google Fit connected");
                GFIT_CONNECTED = true;

                Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, true);
                sendBroadcast(msg);
                gFit = new GoogleFit(getBaseContext(), mClient);
            }

            @Override
            public void onConnectionSuspended(int i) {
                GFIT_CONNECTED = false;
                Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                sendBroadcast(msg);

                if(i == ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                    Log.d(LOG_TAG, "Google Fit connection lost. Network Lost");
                }
                else if(i == ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                    Log.d(LOG_TAG, "Google Fit connection lost. Service Disconnected");
                }
            }
        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult result) {
                Log.d(LOG_TAG, "Google Fit connection failed. Cause: " + result.toString());
                GFIT_CONNECTED = false;
                Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT);
                msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                sendBroadcast(msg);

                if(!result.hasResolution()) {
                    Log.d(LOG_TAG, "Google Fit error has no resolution");
                    GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), OpenFitActivity.this, 0).show();
                    return;
                }

                try {
                    Log.d(LOG_TAG, "Google Fit attempting to resolve failed connection");
                    result.startResolutionForResult(OpenFitActivity.this, REQUEST_OAUTH);
                }
                catch(IntentSender.SendIntentException e) {
                    Log.e(LOG_TAG, "Google Fit exception while starting resolution activity", e);
                }
            }
        }).build();
    }
}
