package com.solderbyte.openfit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Set;

import com.android.vending.billing.IInAppBillingService;
import com.solderbyte.openfit.util.OpenFitIntent;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.util.Log;

@SuppressLint("HandlerLeak")
public class OpenFitService extends Service {
    private static final String LOG_TAG = "OpenFit:OpenFitService";

    private OpenFitSavedPreferences oPrefs;
    private ApplicationManager appManager;
    private BluetoothLeService bluetoothLeService;
    private GoogleFit gFit = null;
    private Billing billing = null;
    private IInAppBillingService billingService;
    private Handler mHandler;
    private PackageManager pManager;
    private static ReconnectBluetoothThread reconnectThread;
    private static FindSoundThread findSoundThread;

    private int notificationId = 28518;
    private boolean smsEnabled = false;
    private boolean phoneEnabled = false;
    private boolean weatherClockEnabled = false;
    private boolean weatherNotificationEnabled = false;
    private boolean weatherClockReq = false;
    private boolean googleFitEnabled = false;
    private boolean googleFitSyncing = false;
    private boolean isReconnect = false;
    private boolean reconnecting = false;
    private boolean isStopping = false;
    private boolean isFinding = false;
    private boolean isPremium = false;
    private SmsListener smsListener;
    private MmsListener mmsListener;
    private TelephonyManager telephony;
    private DialerListener dailerListener;
    private Notification notification;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "OpenFit Service onStartCommand: " + flags);
        // register receivers
        this.registerReceiver(serviceStopReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_STOP));
        this.registerReceiver(btReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_BT));
        this.registerReceiver(notificationReceiver, new IntentFilter(OpenFitIntent.INTENT_NOTIFICATION));
        this.registerReceiver(smsReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_SMS));
        this.registerReceiver(mmsReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_MMS));
        this.registerReceiver(phoneReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_PHONE));
        this.registerReceiver(phoneIdleReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_PHONE_IDLE));
        this.registerReceiver(phoneOffhookReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_PHONE_OFFHOOK));
        this.registerReceiver(mediaReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_MEDIA));
        this.registerReceiver(alarmReceiver, Alarm.getIntentFilter());
        this.registerReceiver(weatherReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_WEATHER));
        this.registerReceiver(locationReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_LOCATION));
        this.registerReceiver(cronReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_CRONJOB));
        this.registerReceiver(googleFitReceiver, new IntentFilter(OpenFitIntent.INTENT_GOOGLE_FIT));
        this.registerReceiver(billingReceiver, new IntentFilter(OpenFitIntent.INTENT_BILLING));

        // start modules
        pManager = this.getPackageManager();
        MediaController.init(this);
        LocationInfo.init(this);
        Weather.init(this);
        Cronjob.init(this);
        startCronJob();
        startBillingServices();

        // start service
        this.createNotification(false);
        this.startBluetoothHandler();
        this.startBluetoothService();
        this.startNotificationListenerService();
        this.startGoogleApiClient();
        this.startForeground(notificationId, notification);

        // load saved preferences
        oPrefs = new OpenFitSavedPreferences(this);
        appManager = new ApplicationManager();
        this.sendNotificationApplications();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        unregisterReceiver(serviceStopReceiver);
        super.onDestroy();
    }

    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(LOG_TAG, "Bluetooth service connected");
            bluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
            if(!bluetoothLeService.initialize()) {
                Log.e(LOG_TAG, "Unable to initialize BluetoothLE");
            }
            bluetoothLeService.setHandler(mHandler);
            sendServiceStarted();
            sendUIPreferences();

            // auto connect
            bluetoothLeService.connectRfcomm();
            isReconnect = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "Bluetooth onServiceDisconnected");
            bluetoothLeService = null;
        }
    };

    public void sendUIPreferences() {
        // update ui
        if(BluetoothLeService.isEnabled) {
            Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_IS_ENABLED);
            sendBroadcast(i);
        }
        else {
            Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_IS_ENABLED_FAILED);
            sendBroadcast(i);
        }
        if(BluetoothLeService.isConnected) {
            Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_IS_CONNECTED);
            sendBroadcast(i);
            createNotification(true);
        }
        else {
            Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_IS_DISCONNCTED);
            sendBroadcast(i);
            createNotification(false);
        }

        if(oPrefs.preference_list_devices_value != OpenFitIntent.DEFAULT) {
            Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_DEVICE_NAME);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, oPrefs.getString("preference_list_devices_entry"));
            sendBroadcast(i);

            bluetoothLeService.setEntries();
            bluetoothLeService.setDevice(oPrefs.preference_list_devices_value);
            Log.d(LOG_TAG, "Service restored device: " + oPrefs.getString("preference_list_weather_entry") + ":" + oPrefs.getString("preference_list_weather_value"));
        }

        if(oPrefs.preference_list_weather_value != OpenFitIntent.DEFAULT) {
            Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_WEATHER);
            i.putExtra(OpenFitIntent.EXTRA_WEATHER_ENTRY, oPrefs.getString("preference_list_weather_entry"));
            i.putExtra(OpenFitIntent.EXTRA_WEATHER_VALUE, oPrefs.getString("preference_list_weather_value"));
            sendBroadcast(i);
            startWeather(oPrefs.getString("preference_list_weather_value"));
        }

        Intent s = new Intent(OpenFitIntent.INTENT_UI_BT);
        s.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_SMS);
        s.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, oPrefs.getBoolean("preference_checkbox_sms"));
        sendBroadcast(s);
        startSmsListener(oPrefs.getBoolean("preference_checkbox_sms"));

        Intent p = new Intent(OpenFitIntent.INTENT_UI_BT);
        p.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_PHONE);
        p.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, oPrefs.getBoolean("preference_checkbox_phone"));
        sendBroadcast(p);
        startDailerListener(oPrefs.getBoolean("preference_checkbox_phone"));

        Intent t = new Intent(OpenFitIntent.INTENT_UI_BT);
        t.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_TIME);
        t.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, oPrefs.getBoolean("preference_checkbox_time"));
        sendBroadcast(t);
    }

    public void sendNotificationApplications() {
        ArrayList<String> listeningListAppNames = new ArrayList<String>();
        ArrayList<String> listeningListPackageNames = new ArrayList<String>();
        Set<String> setPackageNames = oPrefs.getSet();
        for(String packageName : setPackageNames) {
            listeningListAppNames.add(oPrefs.getString(packageName));
            listeningListPackageNames.add(packageName);
            Log.d(LOG_TAG, "Listening Package: " + packageName);
            appManager.addNotificationApp(packageName);
        }

        Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
        i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_APPLICATIONS);
        i.putStringArrayListExtra(OpenFitIntent.EXTRA_APPLICATIONS_PACKAGE_NAME, listeningListPackageNames);
        i.putStringArrayListExtra(OpenFitIntent.EXTRA_APPLICATIONS_APP_NAME, listeningListAppNames);
        sendBroadcast(i);

        Intent a = new Intent(OpenFitIntent.INTENT_SERVICE_NOTIFICATION_APPLICATIONS);
        a.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_SERVICE_NOTIFICATION_APPLICATIONS);
        a.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, appManager.getNotificationApplications());
        sendBroadcast(a);
    }

    public void sendServiceStarted() {
        Log.d(LOG_TAG, "sendServiceStarted");
        Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
        i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_SERVICE_START);
        sendBroadcast(i);
    }

    public void startBluetoothService() {
        Log.d(LOG_TAG, "Starting bluetooth service");
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        this.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void reconnectBluetoothService() {
        Log.d(LOG_TAG, "starting reconnect thread");
        if(isReconnect) {
            reconnectThread = new ReconnectBluetoothThread();
            reconnectThread.start();
            reconnecting = true;
        }
    }

    public void reconnectBluetoothStop() {
        Log.d(LOG_TAG, "stopping reconnect thread");
        reconnecting = false;
        if(reconnectThread != null) {
            reconnectThread.close();
            reconnectThread = null;
            Log.d(LOG_TAG, "stopped reconnect thread");
        }
    }

    public void startBluetoothHandler() {
        // setup message handler
        Log.d(LOG_TAG, "Starting up bluetooth handler");
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(LOG_TAG, "handleMessage: "+msg.getData());
                String bluetoothMessage = msg.getData().getString(OpenFitIntent.EXTRA_BLUETOOTH);
                String bluetoothDevice = msg.getData().getString(OpenFitIntent.EXTRA_BLUETOOTH_DEVICE);
                String bluetoothDevicesList = msg.getData().getString(OpenFitIntent.EXTRA_BLUETOOTH_DEVICE_LIST);
                String bluetoothData = msg.getData().getString(OpenFitIntent.EXTRA_BLUETOOTH_DATA);

                if(bluetoothMessage != null && !bluetoothMessage.isEmpty()) {
                    Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
                    i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, bluetoothMessage);
                    sendBroadcast(i);
                }
                if(bluetoothDevice != null && !bluetoothDevice.isEmpty()) {
                    String[] sDevice = bluetoothDevice.split(",");
                    String sDeviceName = sDevice[0];
                    String sDeviceAddress = sDevice[1];
                    Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
                    i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, bluetoothDevice);
                    i.putExtra(OpenFitIntent.EXTRA_DEVICE_NAME, sDeviceName);
                    i.putExtra(OpenFitIntent.EXTRA_DEVICE_ADDRESS, sDeviceAddress);
                    sendBroadcast(i);
                }
                if(bluetoothDevicesList != null && !bluetoothDevicesList.isEmpty()) {
                    CharSequence[] bluetoothEntries = msg.getData().getCharSequenceArray(OpenFitIntent.EXTRA_BLUETOOTH_ENTRIES);
                    CharSequence[] bluetoothEntryValues = msg.getData().getCharSequenceArray(OpenFitIntent.EXTRA_BLUETOOTH_ENTRIES_VALUES);
                    Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
                    i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, bluetoothDevicesList);
                    i.putExtra(OpenFitIntent.EXTRA_BLUETOOTH_ENTRIES, bluetoothEntries);
                    i.putExtra(OpenFitIntent.EXTRA_BLUETOOTH_ENTRIES_VALUES, bluetoothEntryValues);
                    sendBroadcast(i);
                }
                if(bluetoothData != null && !bluetoothData.isEmpty()) {
                    byte[] byteArray = msg.getData().getByteArray(OpenFitIntent.INTENT_EXTRA_DATA);
                    handleBluetoothData(byteArray);
                }
                if(bluetoothMessage != null && bluetoothMessage.equals(OpenFitIntent.EXTRA_IS_CONNECTED_RFCOMM)) {
                    if(reconnecting) {
                        reconnectBluetoothStop();
                    }
                    if(!isStopping) {
                        createNotification(true);
                    }
                }
                if(bluetoothMessage != null && bluetoothMessage.equals(OpenFitIntent.EXTRA_IS_DISCONNECTED_RFCOMM)) {
                    if(isReconnect) {
                        reconnectBluetoothService();
                    }
                    if(!isStopping) {
                        createNotification(false);
                    }
                }
            }
        };
    }

    public void handleUIMessage(String message, Intent intent) {
        if(message != null && !message.isEmpty() && bluetoothLeService != null) {
            if(message.equals(OpenFitIntent.ACTION_ENABLE)) {
                bluetoothLeService.enableBluetooth();
            }
            if(message.equals(OpenFitIntent.ACTION_DISABLE)) {
                bluetoothLeService.disableBluetooth();
            }
            if(message.equals(OpenFitIntent.ACTION_SCAN)) {
                bluetoothLeService.scanLeDevice();
            }
            if(message.equals(OpenFitIntent.ACTION_CONNECT)) {
                bluetoothLeService.connectRfcomm();
                isReconnect = true;
            }
            if(message.equals(OpenFitIntent.ACTION_DISCONNECT)) {
                bluetoothLeService.disconnectRfcomm();
                isReconnect = false;
            }
            if(message.equals(OpenFitIntent.ACTION_SET_ENTRIES)) {
                bluetoothLeService.setEntries();
            }
            if(message.equals(OpenFitIntent.ACTION_SET_DEVICE)) {
                String deviceMac = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_DATA);
                bluetoothLeService.setDevice(deviceMac);
            }
            if(message.equals(OpenFitIntent.ACTION_TIME)) {
                String s = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_DATA);
                boolean value = Boolean.parseBoolean(s);
                sendTime(value);
            }
            if(message.equals(OpenFitIntent.ACTION_WEATHER)) {
                String weatherValue = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_DATA);
                startWeather(weatherValue);
            }
            if(message.equals(OpenFitIntent.ACTION_FITNESS)) {
                sendFitnessRequest();
            }
            if(message.equals(OpenFitIntent.ACTION_PHONE)) {
                String data = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_DATA);
                boolean enabled = Boolean.parseBoolean(data);
                startDailerListener(enabled);
            }
            if(message.equals(OpenFitIntent.ACTION_SMS)) {
                String data = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_DATA);
                Boolean enabled = Boolean.parseBoolean(data);
                startSmsListener(enabled);
            }
            if(message.equals(OpenFitIntent.ACTION_UI_STATUS)) {
                sendUIPreferences();
            }
        }
    }

    public void handleBluetoothData(byte[] data) {
        Log.d(LOG_TAG, "Service received: " + OpenFitApi.byteArrayToHexString(data));
        if(Arrays.equals(data, OpenFitApi.getReady())) {
            Log.d(LOG_TAG, "Recieved ready message");
            sendBluetoothBytes(OpenFitApi.getUpdate());
            sendBluetoothBytes(OpenFitApi.getUpdateFollowUp());
            sendBluetoothBytes(OpenFitApi.getFotaCommand());
            sendTime(oPrefs.getBoolean("preference_checkbox_time"));
        }

        if(Arrays.equals(data, OpenFitApi.getFindStart())) {
            sendFindStart();
        }
        if(Arrays.equals(data, OpenFitApi.getFindStop())) {
            sendFindStop();
        }
        if(Arrays.equals(data, OpenFitApi.getMediaPrev())) {
            sendMediaPrev();
        }
        if(Arrays.equals(data, OpenFitApi.getMediaNext())) {
            sendMediaNext();
        }
        if(Arrays.equals(data, OpenFitApi.getMediaPlay())) {
            sendMediaPlay();
        }
        if(OpenFitApi.byteArrayToHexString(data).contains(OpenFitApi.byteArrayToHexString(OpenFitApi.getMediaVolume()))) {
            byte vol = data[data.length - 1];
            sendMediaVolume(vol, false);
        }
        if(Arrays.equals(data, OpenFitApi.getMediaReqStart())) {
            sendMediaRes();
        }
        if(Arrays.equals(data, OpenFitApi.getOpenAlarmCleared())) {
            //DismissAlarm();
        }
        if(Arrays.equals(data, OpenFitApi.getOpenAlarmSnoozed())) {
            //snoozeAlarm();
        }
        if(OpenFitApi.byteArrayToHexString(data).contains(OpenFitApi.byteArrayToHexString(OpenFitApi.getOpenRejectCall()))) {
            endCall();
        }
        if(Arrays.equals(data, OpenFitApi.getOpenWeatherReq())) {
            Log.d(LOG_TAG, "Requesting weather");
            weatherClockReq = true;
            getWeather();
        }
        if(Arrays.equals(data, OpenFitApi.getFitnessMenu())) {
            sendFitnessMenuResponse();
        }
        if(OpenFitApi.byteArrayToHexString(data).contains(OpenFitApi.byteArrayToHexString(OpenFitApi.getFitnessCycling()))) {
            sendFitnessCycling();
        }
        if(OpenFitApi.byteArrayToHexString(data).contains(OpenFitApi.byteArrayToHexString(OpenFitApi.getFitnessRunning()))) {
            sendFitnessRunning();
        }

        if(Fitness.isPendingData()) {
            handleFitnessData(data);
        }
        if(OpenFitApi.byteArrayToHexString(data).startsWith(OpenFitApi.byteArrayToHexString(OpenFitApi.getFitness()))) {
            if(Fitness.isFitnessData(data)) {
                Log.d(LOG_TAG, "Fitness data found setting listener");
                handleFitnessData(data);
            }
            else {
                Log.d(LOG_TAG, "Fitness data false");
            }
        }
    }

    public void handleFitnessData(byte[] data) {
        Fitness.addData(data);
        if(!Fitness.isPendingData()) {
            Log.d(LOG_TAG, "Fitness data complete");
            Fitness.parseData();
            Intent i = new Intent(OpenFitIntent.INTENT_UI_BT);
            i.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.EXTRA_FITNESS);
            i.putExtra(OpenFitIntent.EXTRA_PEDOMETER_TOTAL, Fitness.getPedometerTotal());
            i.putParcelableArrayListExtra(OpenFitIntent.EXTRA_PEDOMETER_LIST, Fitness.getPedometerList());
            i.putParcelableArrayListExtra(OpenFitIntent.EXTRA_PEDOMETER_DAILY_LIST, Fitness.getPedometerDailyList());
            i.putParcelableArrayListExtra(OpenFitIntent.EXTRA_SLEEP_INFO_LIST, Fitness.getSleepInfoList());
            i.putParcelableArrayListExtra(OpenFitIntent.EXTRA_EXERCISE_LIST, Fitness.getExerciseDataList());
            i.putParcelableArrayListExtra(OpenFitIntent.EXTRA_SLEEP_LIST, Fitness.getSleepList());
            i.putParcelableArrayListExtra(OpenFitIntent.EXTRA_HEARTRATE_LIST, Fitness.getHeartRateList());
            i.putExtra(OpenFitIntent.EXTRA_PROFILE_DATA, Fitness.getProfileData());
            sendBroadcast(i);
            if(googleFitSyncing) {
                startFitnessSync(Fitness.getPedometerList(), Fitness.getExerciseDataList(), Fitness.getSleepList(), Fitness.getSleepInfoList(), Fitness.getHeartRateList(), Fitness.getProfileData());
            }
        }
    }

    public void startNotificationListenerService() {
        Log.d(LOG_TAG, "Starting notification service");
        Intent notificationIntent = new Intent(this, NotificationService.class);
        this.startService(notificationIntent);
    }

    public void startDailerListener(Boolean enabled) {
        phoneEnabled = enabled;
        Log.d(LOG_TAG, "Starting Phone Listeners");
        if(phoneEnabled) {
            dailerListener = new DialerListener(this);
            telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            telephony.listen(dailerListener, PhoneStateListener.LISTEN_CALL_STATE);
            Log.d(LOG_TAG, "phone listening");
        }
        else {
            if(telephony != null) {
                telephony.listen(dailerListener, PhoneStateListener.LISTEN_NONE);
                dailerListener.destroy();
            }
        }
    }

    public void startSmsListener(Boolean enabled) {
        smsEnabled = enabled;
        Log.d(LOG_TAG, "Starting SMS/MMS Listeners");
        if(smsEnabled) {
            if(smsListener == null) {
                smsListener = new SmsListener(this);
                IntentFilter smsFilter = new IntentFilter(OpenFitIntent.INTENT_ANDROID_SMS);
                this.registerReceiver(smsListener, smsFilter);
                mmsListener = new MmsListener(this);
                IntentFilter mmsFilter = new IntentFilter(OpenFitIntent.INTENT_ANDROID_SMS);
                this.registerReceiver(mmsListener, mmsFilter);
            }
        }
        else {
            if(smsListener != null) {
                this.unregisterReceiver(smsListener);
                smsListener = null;
                this.unregisterReceiver(mmsListener);
                mmsListener = null;
            }
        }
    }

    public void createNotification(boolean connected) {
        Log.d(LOG_TAG, "Creating Notification: " + connected);
        Intent stopService =  new Intent(OpenFitIntent.INTENT_SERVICE_STOP);
        //Intent startActivity = new Intent(this, OpenFitActivity.class);
        //PendingIntent startIntent = PendingIntent.getActivity(this, 0, startActivity, PendingIntent.FLAG_NO_CREATE);
        PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0, stopService, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.open_fit_notification);
        nBuilder.setContentTitle(getString(R.string.notification_title));
        if(connected) {
            nBuilder.setContentText(getString(R.string.notification_connected));
        }
        else {
            nBuilder.setContentText(getString(R.string.notification_disconnected));
        }
        //nBuilder.setContentIntent(startIntent);
        nBuilder.setAutoCancel(true);
        nBuilder.setOngoing(true);
        nBuilder.addAction(R.drawable.open_off_noti, getString(R.string.notification_button_close), stopIntent);
        if(connected) {
            Intent cIntent = new Intent(OpenFitIntent.INTENT_SERVICE_BT);
            cIntent.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.ACTION_DISCONNECT);
            PendingIntent pConnect = PendingIntent.getBroadcast(this, 0, cIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nBuilder.addAction(R.drawable.open_btd, getString(R.string.notification_button_disconnect), pConnect);
        }
        else {
            Intent cIntent = new Intent(OpenFitIntent.INTENT_SERVICE_BT);
            cIntent.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.ACTION_CONNECT);
            PendingIntent pConnect = PendingIntent.getBroadcast(this, 0, cIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nBuilder.addAction(R.drawable.open_btc, getString(R.string.notification_button_connect), pConnect);
        }

        // Sets an ID for the notification
        NotificationManager nManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        notification = nBuilder.build();
        nManager.notify(notificationId, notification);
    }

    public void clearNotification() {
        NotificationManager nManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        nManager.cancel(notificationId);
    }

    public void startBillingServices() {
        billing = new Billing();
        billing.setContext(this);

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

    public void sendBluetoothBytes(byte[] bytes) {
        if(bluetoothLeService != null) {
            bluetoothLeService.write(bytes);
        }
        else {
            Log.w(LOG_TAG, "bluetoothLeService is null");
        }
    }

    public void startWeather(String weatherValue) {
        if(weatherValue.equals(OpenFitIntent.NONE)) {
            weatherNotificationEnabled = false;
            weatherClockEnabled = false;
        }
        else {
            String[] values = weatherValue.split(",");
            if(values.length > 1) {
                String type = values[0];
                String unit = values[1];
                Weather.setUnits(unit);
                if(type.equals("combo")) {
                    weatherNotificationEnabled = true;
                    weatherClockEnabled = true;
                }
                else if(type.equals("notification")) {
                    weatherNotificationEnabled = true;
                    weatherClockEnabled = false;
                }
                else if(type.equals("clock")) {
                    weatherClockEnabled = true;
                    weatherNotificationEnabled = false;
                }
            }
            else {
                weatherNotificationEnabled = true;
                weatherClockEnabled = true;
            }
        }
    }

    public void startGoogleApiClient() {
        Log.d(LOG_TAG, "startGoogleApiClient");
        gFit = new GoogleFit(this);
    }

    public void startFitnessSync(ArrayList<PedometerData> pedometerList, ArrayList<ExerciseData> exerciseList, ArrayList<SleepData> sleepList, ArrayList<SleepInfo> sleepInfoList, ArrayList<HeartRateData> heartRateList, ProfileData pData) {
        Log.d(LOG_TAG, "startFitnessSync");
        if(gFit != null) {
            Log.d(LOG_TAG, "gFit.setData");
            gFit.setData(pedometerList, exerciseList, sleepList, sleepInfoList, heartRateList, pData);
            Log.d(LOG_TAG, "gFit.syncData");
            gFit.syncData();
        }
        else {
            Log.d(LOG_TAG, "Google Api Client not initialized");
        }
    }

    public void sendTime(boolean is24Hour) {
        byte[] bytes = OpenFitApi.getCurrentTimeInfo(is24Hour);
        sendBluetoothBytes(bytes);
    }

    public void sendFitnessRequest() {
        Log.d(LOG_TAG, "sendFitnessRequest");
        byte[] bytes = OpenFitApi.getFitnessRequest();
        sendBluetoothBytes(bytes);
    }

    public void sendFitnessMenuResponse() {
        Log.d(LOG_TAG, "Fitness Menu");
        byte[] bytes = OpenFitApi.getFitnessMenuResponse();
        sendBluetoothBytes(bytes);
    }

    public void sendFitnessCycling() {
        Log.d(LOG_TAG, "Cycling");
        /*if(LocationInfo.getLat() != 0 && LocationInfo.getLon() != 0) {
            int lat = Float.floatToIntBits((float) LocationInfo.getLat());
            int lon = Float.floatToIntBits((float) LocationInfo.getLon());
            String query = "lat=" + lat + "&lon=" + lon;
            Log.d(LOG_TAG, query);
            //byte[] bytes = OpenFitApi.getFitnessCyclingResponse(lat, lon);
            //sendBluetoothBytes(bytes);
        }*/

    }

    public void sendFitnessRunning() {
        Log.d(LOG_TAG, "Running");
    }

    public void sendMediaTrack() {
        byte[] bytes = OpenFitApi.getOpenMediaTrack(MediaController.getTrack());
        sendBluetoothBytes(bytes);
    }

    public void sendMediaPrev() {
        Log.d(LOG_TAG, "Media Prev");
        sendBroadcast(MediaController.prevTrack(), null);
    }

    public void sendMediaNext() {
        Log.d(LOG_TAG, "Media Next");
        sendBroadcast(MediaController.nextTrack(), null);
    }

    public void sendMediaPlay() {
        Log.d(LOG_TAG, "Media Play/Pause");
        sendBroadcast(MediaController.playTrack(), null);
    }

    public void sendMediaVolume(byte vol, boolean req) {
        Log.d(LOG_TAG, "Media Volume: " + vol);
        byte offset = (byte) (MediaController.getActualVolume() - MediaController.getVolume());
        if(offset != 0) {
            vol = (byte) (vol + offset);
            if(!req) {
                if(offset > 0) {
                    vol += 1;
                }
                else {
                    vol -= 1;
                }
            }
        }
        MediaController.setVolume(vol);
        byte[] bytes = OpenFitApi.getMediaSetVolume(vol);
        sendBluetoothBytes(bytes);
    }

    public void sendMediaRes() {
        Log.d(LOG_TAG, "Media Request");
        sendMediaTrack();
        sendMediaVolume(MediaController.getVolume(), true);
    }

    public void sendFindStart() {
        Log.d(LOG_TAG, "Find Start");
        if(isFinding == false) {
            findSoundThread = new FindSoundThread();
            findSoundThread.start();
            isFinding = true;
        }
    }

    public void sendFindStop() {
        Log.d(LOG_TAG, "Find Stop");
        if(findSoundThread != null) {
            findSoundThread = null;
            Log.d(LOG_TAG, "stopped find thread");
        }
        isFinding = false;
    }

    public void sendAppNotification(String packageName, String sender, String title, String message, int id) {
        byte[] bytes = OpenFitApi.getOpenNotification(packageName, sender, title, message, id);
        sendBluetoothBytes(bytes);
    }

    public void sendEmailNotification(String packageName, String sender, String title, String message, int id) {
        byte[] bytes = OpenFitApi.getOpenEmail(sender, title, message, message, id);
        sendBluetoothBytes(bytes);
    }

    public void sendDialerNotification(String number) {
        long id = (long)(System.currentTimeMillis() / 1000L);
        String sender = number;
        String name = getContactName(number);
        if(name != null) {
            sender = name;
        }
        byte[] bytes = OpenFitApi.getOpenIncomingCall(sender, number, id);
        sendBluetoothBytes(bytes);
    }

    public void sendDialerEndNotification() {
        byte[] bytes = OpenFitApi.getOpenIncomingCallEnd();
        sendBluetoothBytes(bytes);
    }

    public void endCall() {
        Log.d(LOG_TAG, "Ending call");
        Class<?> telephonyClass = null;
        Method method = null;
        Method endCall = null;
        try {
            telephonyClass = Class.forName(telephony.getClass().getName());
            method = telephonyClass.getDeclaredMethod("getITelephony");
            method.setAccessible(true);
            Object iTelephony = null;
            iTelephony = method.invoke(telephony);
            endCall = iTelephony.getClass().getDeclaredMethod("endCall");
            endCall.invoke(iTelephony);
        }
        catch(Exception e) {
            Log.d(LOG_TAG, "Failed ending call");
            e.printStackTrace();
        }
    }

    public void sendSmsNotification(String number, String message) {
        long id = (long)(System.currentTimeMillis() / 1000L);
        String title = "Text Message";
        String sender = number;
        String name = getContactName(number);
        if(name != null) {
            sender = name;
        }
        byte[] bytes = OpenFitApi.getOpenNotification(sender, number, title, message, id);
        sendBluetoothBytes(bytes);
    }

    public String getAppName(String packageName) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = pManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        }
        catch (NameNotFoundException e) {
            Log.d(LOG_TAG, "Cannot get application info");
        }
        String appName = (String) pManager.getApplicationLabel(appInfo);
        return appName;
    }

    public String getContactName(String phoneNumber) {
        ContentResolver cr = this.getContentResolver();
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[] {PhoneLookup.DISPLAY_NAME}, null, null, null);
        if(cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
        }
        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    public void sendAlarmStart() {
        long id = (long)(System.currentTimeMillis() / 1000L);
        byte[] bytes = OpenFitApi.getOpenAlarm(id);
        sendBluetoothBytes(bytes);
    }

    public void sendAlarmStop() {
        byte[] bytes = OpenFitApi.getOpenAlarmClear();
        sendBluetoothBytes(bytes);
    }

    public void sendWeatherNotifcation(String weather, String icon) {
        long id = (long)(System.currentTimeMillis() / 1000L);
        byte[] bytes = OpenFitApi.getOpenWeather(weather, icon, id);
        sendBluetoothBytes(bytes);
    }

    public void sendWeatherClock(String location, String tempCur, String tempUnit, String icon) {
        byte[] bytes = OpenFitApi.getOpenWeatherClock(location, tempCur, tempUnit, icon);
        sendBluetoothBytes(bytes);
    }

    public void startCronJob() {
        Log.d(LOG_TAG, "Starting Cronjob");
        Cronjob.start();
    }

    public void stopCronJob() {
        Log.d(LOG_TAG, "Stopping Cronjob");
        Cronjob.stop();
    }

    public void getWeather() {
        if(LocationInfo.getLat() != 0 && LocationInfo.getLon() != 0) {
            String query = "lat=" + LocationInfo.getLat() + "&lon=" + LocationInfo.getLon();
            String country = null;
            String location = null;
            if(LocationInfo.getCountryCode() != null) {
                country = LocationInfo.getCountryCode();
            }
            else if(LocationInfo.getCountryName() != null) {
                country = LocationInfo.getCountryName();
            }
            if(country != null) {
                location = LocationInfo.getCityName() + ", " + country;
            }
            else {
                location = LocationInfo.getCityName();
            }

            Weather.getWeather(query, location);
        }
    }

    // Does not work
    /*public void snoozeAlarm() {
        Log.d(LOG_TAG, "Snoozing alarm");
        Intent intent = Alarm.snoozeAlarm();
        sendBroadcast(intent);
    }

    public void DismissAlarm() {
        Log.d(LOG_TAG, "Dismissing alarm");
        Intent intent = Alarm.dismissAlarm();
        sendBroadcast(intent);
    }*/

    private BroadcastReceiver serviceStopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Stopping Service");
            reconnecting = false;
            isReconnect = false;
            isStopping = true;
            isFinding = false;
            mHandler = null;
            Log.d(LOG_TAG, "Stopping" + smsEnabled +" : " + phoneEnabled);
            if(smsEnabled) {
                unregisterReceiver(smsListener);
                unregisterReceiver(mmsListener);
            }
            if(phoneEnabled) {
                telephony.listen(dailerListener, PhoneStateListener.LISTEN_NONE);
                dailerListener.destroy();
            }
            unregisterReceiver(btReceiver);
            unregisterReceiver(notificationReceiver);
            unregisterReceiver(smsReceiver);
            unregisterReceiver(mmsReceiver);
            unregisterReceiver(phoneReceiver);
            unregisterReceiver(phoneIdleReceiver);
            unregisterReceiver(phoneOffhookReceiver);
            unregisterReceiver(mediaReceiver);
            unregisterReceiver(alarmReceiver);
            unregisterReceiver(weatherReceiver);
            unregisterReceiver(locationReceiver);
            unregisterReceiver(cronReceiver);
            unregisterReceiver(googleFitReceiver);
            unregisterReceiver(billingReceiver);
            unbindService(mServiceConnection);
            Cronjob.stop();
            clearNotification();
            reconnectBluetoothStop();
            Log.d(LOG_TAG, "stopSelf");
            stopForeground(true);
            stopSelf();
        }
    };

    private BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String message = intent.getStringExtra("message");
            handleUIMessage(message, intent);
            Log.d(LOG_TAG, "Received UI Command: " + message);
        }
    };

    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String packageName = intent.getStringExtra("packageName");
            final String ticker = intent.getStringExtra("ticker");
            final String title = intent.getStringExtra("title");
            final String message = intent.getStringExtra("message");
            //long time = intent.getLongExtra("time", 0);
            final int id = intent.getIntExtra("id", 0);
            final String appName = getAppName(packageName);

            if(packageName.equals("com.google.android.gm")) {
                Log.d(LOG_TAG, "Received email:" + appName + " title:" + title + " ticker:" + ticker + " message:" + message);
                sendEmailNotification(appName, title, ticker, message, id);
            }
            else if(packageName.equals("com.android.calendar")) {
                Log.d(LOG_TAG, "Received calendar: "  + appName + " Title:" + title + " Alarm time:"+message);
                final String caltitle = getString(R.string.calendar_event) + ": " + title + "\n" + getString(R.string.calendar_when) + ": " + message;
                sendAppNotification(appName, message, ticker, caltitle, id);
            }
            else {
                Log.d(LOG_TAG, "Received notification appName: " + appName + " title:" + title + " ticker:" + ticker + " message:" + message);
                sendAppNotification(appName, title, ticker, message, id);
            }
        }
    };

    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            String sender = intent.getStringExtra("sender");
            Log.d(LOG_TAG, "Recieved SMS message: "+sender+" - "+message);
            sendSmsNotification(sender, message);
        }
    };

    private BroadcastReceiver mmsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = "MMS received";
            String sender = intent.getStringExtra("sender");
            Log.d(LOG_TAG, "Recieved MMS message: "+sender+" - "+message);
            sendSmsNotification(sender, message);
        }
    };

    private BroadcastReceiver phoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String sender = intent.getStringExtra("sender");
            Log.d(LOG_TAG, "Recieved PHONE: "+sender);
            sendDialerNotification(sender);
        }
    };

    private BroadcastReceiver phoneIdleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String sender = intent.getStringExtra("sender");
            Log.d(LOG_TAG, "Recieved Idle: "+sender);
            sendDialerEndNotification();
        }
    };

    private BroadcastReceiver phoneOffhookReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String sender = intent.getStringExtra("sender");
            Log.d(LOG_TAG, "Recieved Offhook: "+sender);
            sendDialerEndNotification();
        }
    };

    private BroadcastReceiver mediaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mediaTrack = intent.getStringExtra("mediaTrack");
            Log.d(LOG_TAG, "Media sending: " + mediaTrack);
            sendMediaTrack();
        }
    };

    private BroadcastReceiver alarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = Alarm.getAction(intent);
            Log.d(LOG_TAG, "Alarm Action: " + action);
            if(action.equals("START")) {
                sendAlarmStart();
            }
            else if(action.equals("SNOOZE")) {
                sendAlarmStop();
            }
            else if(action.equals("STOP")) {
                sendAlarmStop();
            }
        }
    };

    private BroadcastReceiver weatherReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "weatherReceiver updated: ");
            //String name = intent.getStringExtra("name");
            //String weather = intent.getStringExtra("weather");
            String description = intent.getStringExtra("description");
            String tempCur = intent.getStringExtra("tempCur");
            //String tempMin = intent.getStringExtra("tempMin");
            //String tempMax = intent.getStringExtra("tempMax");
            String tempUnit = intent.getStringExtra("tempUnit");
            //String humidity = intent.getStringExtra("humidity");
            //String pressure = intent.getStringExtra("pressure");
            String icon = intent.getStringExtra("icon");
            String location = intent.getStringExtra("location");

            String weatherInfo = location + ": " + tempCur + tempUnit + "\nWeather: " + description;
            Log.d(LOG_TAG, weatherInfo);

            if(weatherClockEnabled || weatherClockReq) {
                sendWeatherClock(location, tempCur, tempUnit, icon);
            }
            if(weatherNotificationEnabled) {
                sendWeatherNotifcation(weatherInfo, icon);
            }
        }
    };

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "locationReceiver updated");
            if(weatherClockEnabled || weatherNotificationEnabled) {
                getWeather();
            }
        }
    };

    private BroadcastReceiver cronReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "#####CronJob#####");
            if(weatherClockEnabled || weatherNotificationEnabled) {
                LocationInfo.listenForLocation();
            }

            if(isPremium) {
                Log.d(LOG_TAG, "Premium Features");
                if(googleFitEnabled) {
                    googleFitSyncing = true;
                    sendFitnessRequest();
                }
            }
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
                    startGoogleApiClient();
                    googleFitEnabled = true;
                }
                else {
                    Log.d(LOG_TAG, "Google Fit Disabled");
                    googleFitEnabled = false;
                }
            }
            if(message.equals(OpenFitIntent.INTENT_GOOGLE_FIT_SYNC)) {
                Log.d(LOG_TAG, "Google Fit Sync requested");
                if(isPremium) {
                    Log.d(LOG_TAG, "Premium Features");
                    if(googleFitEnabled) {
                        googleFitSyncing = true;
                        sendFitnessRequest();
                    }
                }
                else {
                    Intent msg = new Intent(OpenFitIntent.INTENT_GOOGLE_FIT);
                    msg.putExtra(OpenFitIntent.INTENT_EXTRA_MSG, OpenFitIntent.INTENT_GOOGLE_FIT_SYNC_STATUS);
                    msg.putExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                    sendBroadcast(msg);
                }
            }
            if(message.equals(OpenFitIntent.INTENT_GOOGLE_FIT_SYNC_STATUS)) {
                Boolean status = intent.getBooleanExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                googleFitSyncing = false;
                if(status) {
                    Log.d(LOG_TAG, "Google Fit Sync completed");
                }
                else {
                    Log.d(LOG_TAG, "Google Fit Sync failed");
                }
            }
        }
    };

    private BroadcastReceiver billingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String message = intent.getStringExtra(OpenFitIntent.INTENT_EXTRA_MSG);
            Log.d(LOG_TAG, "Received Billing: ");

            if(message.equals(OpenFitIntent.INTENT_BILLING_VERIFIED)) {
                Boolean verified = intent.getBooleanExtra(OpenFitIntent.INTENT_EXTRA_DATA, false);
                if(verified) {
                    Log.d(LOG_TAG, "Received Billing: ");
                    isPremium = true;
                }
                else {
                    isPremium = false;
                }
            }
        }
    };

    private class ReconnectBluetoothThread extends Thread {
        public void run() {
            long timeStart = Calendar.getInstance().getTimeInMillis();
            Log.d(LOG_TAG, "Reconnecting Bluetooth: "+timeStart);

            while(reconnecting) {
                try {
                    long timeDiff =  Calendar.getInstance().getTimeInMillis() - timeStart;
                    Log.d(LOG_TAG, "Reconnecting Elapsed time: " + timeDiff/1000);
                    bluetoothLeService.connectRfcomm();
                    Thread.sleep(10000L);
                }
                catch(InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        public void close() {
            reconnecting = false;
        }
    }

    private class FindSoundThread extends Thread {
        public void run() {
            long timeStart = Calendar.getInstance().getTimeInMillis();
            Log.d(LOG_TAG, "FindSound Start: "+timeStart);
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, ToneGenerator.MAX_VOLUME);

            while(isFinding) {
                try {
                    long timeDiff =  Calendar.getInstance().getTimeInMillis() - timeStart;
                    Log.d(LOG_TAG, "Sound time: " + timeDiff/1000);

                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200); // 200 ms tone
                    Thread.sleep(600L);
                }
                catch(InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    };
}
