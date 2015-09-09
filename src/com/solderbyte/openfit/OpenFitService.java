package com.solderbyte.openfit;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;

import com.solderbyte.openfit.R;

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
import android.util.Log;

@SuppressLint("HandlerLeak")
public class OpenFitService extends Service {
    private static final String LOG_TAG = "OpenFit:OpenFitService";

    // services
    private BluetoothLeService bluetoothLeService;
    private  Handler mHandler;
    private PackageManager pManager;
    private static ReconnectBluetoothThread reconnectThread;

    private int notificationId = 28518;
    private boolean smsEnabled = false;
    private boolean phoneEnabled = false;
    private boolean weatherEnabled = false;
    private boolean isReconnect = false;
    private boolean reconnecting = false;
    private boolean isStopping = false;
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
        Log.d(LOG_TAG, "onStartCommand: " + intent);
        // register receivers
        this.registerReceiver(stopServiceReceiver, new IntentFilter("stopOpenFitService"));
        this.registerReceiver(bluetoothReceiver, new IntentFilter("bluetooth"));
        this.registerReceiver(notificationReceiver, new IntentFilter("notification"));
        this.registerReceiver(smsReceiver, new IntentFilter("sms"));
        this.registerReceiver(mmsReceiver, new IntentFilter("mms"));
        this.registerReceiver(phoneReceiver, new IntentFilter("phone"));
        this.registerReceiver(phoneIdleReceiver, new IntentFilter("phone:idle"));
        this.registerReceiver(phoneOffhookReceiver, new IntentFilter("phone:offhook"));
        this.registerReceiver(mediaReceiver, MediaController.getIntentFilter());
        this.registerReceiver(alarmReceiver, Alarm.getIntentFilter());
        this.registerReceiver(weatherReceiver, new IntentFilter("weather"));
        this.registerReceiver(cronReceiver, new IntentFilter("cronJob"));

        pManager = this.getPackageManager();
        MediaController.init(this);
        LocationInfo.init(this);
        Weather.init(this);
        Cronjob.init(this);

        // start service
        this.createNotification(false);
        this.startBluetoothHandler();
        this.startBluetoothService();
        this.startNotificationListenerService();
        this.startForeground(notificationId, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        unregisterReceiver(stopServiceReceiver);
        super.onDestroy();
    }

    public void sendServiceStarted() {
        Log.d(LOG_TAG, "sendServiceStarted");
        Intent i = new Intent("bluetoothUI");
        i.putExtra("message", "OpenFitService");
        sendBroadcast(i);
    }

    public void startBluetoothService() {
        // initialize BluetoothLE
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        this.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(LOG_TAG, "Starting bluetooth service");
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

    protected ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(LOG_TAG, "onService Connected");
            bluetoothLeService = ((BluetoothLeService.LocalBinder)service).getService();
            if(!bluetoothLeService.initialize()) {
                Log.e(LOG_TAG, "Unable to initialize BluetoothLE");
            }
            bluetoothLeService.setHandler(mHandler);
            sendServiceStarted();
            sendBluetoothStatus();

            // Automatically connects to the device upon successful start-up initialization.
            //bluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(LOG_TAG, "Bluetooth onServiceDisconnected");
            bluetoothLeService = null;
        }
    };

    public void startBluetoothHandler() {
        // setup message handler
        Log.d(LOG_TAG, "Setting up bluetooth Service handler");
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(LOG_TAG, "handleMessage: "+msg.getData());
                String bluetoothMessage = msg.getData().getString("bluetooth");
                String bluetoothDevice = msg.getData().getString("bluetoothDevice");
                String bluetoothDevicesList = msg.getData().getString("bluetoothDevicesList");
                String bluetoothData = msg.getData().getString("bluetoothData");
                if(bluetoothMessage != null && !bluetoothMessage.isEmpty()) {
                    Intent i = new Intent("bluetoothUI");
                    i.putExtra("message", bluetoothMessage);
                    sendBroadcast(i);
                }
                if(bluetoothDevice != null && !bluetoothDevice.isEmpty()) {
                    String[] sDevice = bluetoothDevice.split(",");
                    String sDeviceName = sDevice[0];
                    String sDeviceAddress = sDevice[1];
                    Intent i = new Intent("bluetoothUI");
                    i.putExtra("message", bluetoothDevice);
                    i.putExtra("deviceName", sDeviceName);
                    i.putExtra("deviceAddress", sDeviceAddress);
                    sendBroadcast(i);
                }
                if(bluetoothDevicesList != null && !bluetoothDevicesList.isEmpty()) {
                    CharSequence[] bluetoothEntries = msg.getData().getCharSequenceArray("bluetoothEntries");
                    CharSequence[] bluetoothEntryValues = msg.getData().getCharSequenceArray("bluetoothEntryValues");
                    Intent i = new Intent("bluetoothUI");
                    i.putExtra("message", bluetoothDevicesList);
                    i.putExtra("bluetoothEntries", bluetoothEntries);
                    i.putExtra("bluetoothEntryValues", bluetoothEntryValues);
                    sendBroadcast(i);
                }
                if(bluetoothData != null && !bluetoothData.isEmpty()) {
                    byte[] byteArray = msg.getData().getByteArray("data");
                    handleBluetoothData(byteArray);
                }
                if(bluetoothMessage != null && bluetoothMessage.equals("isConnectedRfcomm")) {
                    if(reconnecting) {
                        reconnectBluetoothStop();
                    }
                    if(!isStopping) {
                        createNotification(true);
                    }
                }
                if(bluetoothMessage != null && bluetoothMessage.equals("isDisconnectedRfComm")) {
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
            if(message.equals("enable")) {
                bluetoothLeService.enableBluetooth();
            }
            if(message.equals("disable")) {
                bluetoothLeService.disableBluetooth();
            }
            if(message.equals("scan")) {
                bluetoothLeService.scanLeDevice();
            }
            if(message.equals("connect")) {
                bluetoothLeService.connectRfcomm();
                isReconnect = true;
            }
            if(message.equals("disconnect")) {
                bluetoothLeService.disconnectRfcomm();
                isReconnect = false;
            }
            if(message.equals("setEntries")) {
                bluetoothLeService.setEntries();
            }
            if(message.equals("setDevice")) {
                String deviceMac = intent.getStringExtra("data");
                bluetoothLeService.setDevice(deviceMac);
            }
            if(message.equals("time")) {
                String s = intent.getStringExtra("data");
                boolean value = Boolean.parseBoolean(s);
                sendTime(value);
            }
            if(message.equals("weather")) {
                String s = intent.getStringExtra("data");
                weatherEnabled = Boolean.parseBoolean(s);
                startWeather();
            }
            if(message.equals("phone")) {
                String s = intent.getStringExtra("data");
                phoneEnabled = Boolean.parseBoolean(s);
                startDailerListener();
            }
            if(message.equals("sms")) {
                String s = intent.getStringExtra("data");
                smsEnabled = Boolean.parseBoolean(s);
                startSmsListener();
            }
            if(message.equals("status")) {
                sendBluetoothStatus();
            }
        }
    }

    public void handleBluetoothData(byte[] data) {
        Log.d(LOG_TAG, "Service received: " + OpenFitApi.byteArrayToHexString(data));
        if(Arrays.equals(data, OpenFitApi.getReady())) {
            Log.d(LOG_TAG, "Recieved ready message");
            bluetoothLeService.write(OpenFitApi.getUpdate());
            bluetoothLeService.write(OpenFitApi.getUpdateFollowUp());
            bluetoothLeService.write(OpenFitApi.getFotaCommand());
            //bluetoothLeService.write(OpenFitApi.getCurrentTimeInfo(false));
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
        if(Arrays.equals(data, OpenFitApi.getFitnessSyncRes())) {
            sendFitnessHeartBeat();
        }
        if(OpenFitApi.byteArrayToHexString(data).contains(OpenFitApi.byteArrayToHexString(OpenFitApi.getOpenRejectCall()))) {
            endCall();
        }
    }

    public void startNotificationListenerService() {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        this.startService(notificationIntent);
        Log.d(LOG_TAG, "Starting notification service");
    }

    public void startDailerListener() {
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

    public void startSmsListener() {
        Log.d(LOG_TAG, "Starting SMS/MMS Listeners");
        // register listeners
        if(smsEnabled) {
            if(smsListener == null) {
                smsListener = new SmsListener(this);
                IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
                this.registerReceiver(smsListener, smsFilter);
                mmsListener = new MmsListener(this);
                IntentFilter mmsFilter = new IntentFilter("android.provider.Telephony.WAP_PUSH_RECEIVED");
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
        Intent stopService =  new Intent("stopOpenFitService");
        //Intent startActivity = new Intent(this, OpenFitActivity.class);
        //PendingIntent startIntent = PendingIntent.getActivity(this, 0,startActivity, PendingIntent.FLAG_NO_CREATE);
        PendingIntent stopIntent = PendingIntent.getBroadcast(this, 0, stopService, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.open_fit_notification);
        nBuilder.setContentTitle("Open Fit");
        if(connected) {
            nBuilder.setContentText("Connected to Gear Fit");
        }
        else {
            nBuilder.setContentText("Disconnected to Gear Fit");
        }
        //nBuilder.setContentIntent(startIntent);
        nBuilder.setAutoCancel(true);
        nBuilder.setOngoing(true);
        nBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Shut Down", stopIntent);
        if(connected) {
            Intent cIntent = new Intent("bluetooth");
            cIntent.putExtra("message", "disconnect");
            PendingIntent pConnect = PendingIntent.getBroadcast(this, 0, cIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nBuilder.addAction(android.R.drawable.ic_menu_send, "Disconnect", pConnect);
        }
        else {
            Intent cIntent = new Intent("bluetooth");
            cIntent.putExtra("message", "connect");
            PendingIntent pConnect = PendingIntent.getBroadcast(this, 0, cIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            nBuilder.addAction(android.R.drawable.ic_menu_send, "Connect", pConnect);
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

    public void sendBluetoothStatus() {
     // update bluetooth ui
        if(BluetoothLeService.isEnabled) {
            Intent i = new Intent("bluetoothUI");
            i.putExtra("message", "isEnabled");
            sendBroadcast(i);
        }
        else {
            Intent i = new Intent("bluetoothUI");
            i.putExtra("message", "isEnabledFailed");
            sendBroadcast(i);
        }
        if(BluetoothLeService.isConnected) {
            Intent i = new Intent("bluetoothUI");
            i.putExtra("message", "isConnected");
            sendBroadcast(i);
            createNotification(true);
        }
        else {
            Intent i = new Intent("bluetoothUI");
            i.putExtra("message", "isDisconnected");
            sendBroadcast(i);
            createNotification(false);
        }
    }

    public void sendTime(boolean is24Hour) {
        byte[] bytes = OpenFitApi.getCurrentTimeInfo(is24Hour);
        bluetoothLeService.write(bytes);
    }

    public void sendFitnessHeartBeat() {
        Log.d(LOG_TAG, "sendFitnessHeartBeat");
        byte[] bytes = OpenFitApi.getFitnessHeartBeat();
        bluetoothLeService.write(bytes);
    }

    public void sendMediaTrack() {
        byte[] bytes = OpenFitApi.getOpenMediaTrack(MediaController.getTrack());
        if(bluetoothLeService != null) {
            bluetoothLeService.write(bytes);
        }
    }

    public void sendMediaPrev() {
        Log.d(LOG_TAG, "Media Prev");
        sendOrderedBroadcast(MediaController.prevTrackDown(), null);
        sendOrderedBroadcast(MediaController.prevTrackUp(), null);
    }

    public void sendMediaNext() {
        Log.d(LOG_TAG, "Media Next");
        sendOrderedBroadcast(MediaController.nextTrackDown(), null);
        sendOrderedBroadcast(MediaController.nextTrackUp(), null);
    }

    public void sendMediaPlay() {
        Log.d(LOG_TAG, "Media Play/Pause");
        sendOrderedBroadcast(MediaController.playTrackDown(), null);
        sendOrderedBroadcast(MediaController.playTrackUp(), null);
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
        bluetoothLeService.write(bytes);
    }

    public void sendMediaRes() {
        Log.d(LOG_TAG, "Media Request");
        sendMediaTrack();
        sendMediaVolume(MediaController.getVolume(), true);
    }

    public void sendAppNotification(String packageName, String sender, String title, String message, int id) {
        byte[] bytes = OpenFitApi.getOpenNotification(packageName, sender, title, message, id);
        bluetoothLeService.write(bytes);
    }

    public void sendEmailNotification(String packageName, String sender, String title, String message, int id) {
        byte[] bytes = OpenFitApi.getOpenEmail(sender, title, message, message, id);
        bluetoothLeService.write(bytes);
    }

    public void sendDialerNotification(String number) {
        long id = (long)(System.currentTimeMillis() / 1000L);
        String sender = "OpenFit Call";
        String name = getContactName(number);
        if(name != null) {
            sender = name;
        }
        byte[] bytes = OpenFitApi.getOpenIncomingCall(sender, number, id);
        bluetoothLeService.write(bytes);
    }

    public void sendDialerEndNotification() {
        byte[] bytes = OpenFitApi.getOpenIncomingCallEnd();
        bluetoothLeService.write(bytes);
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
        String sender = "Text Message";
        String name = getContactName(number);
        if(name != null) {
            sender = name;
        }
        byte[] bytes = OpenFitApi.getOpenNotification(sender, number, title, message, id);
        bluetoothLeService.write(bytes);
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
        bluetoothLeService.write(bytes);
    }

    public void sendAlarmStop() {
        byte[] bytes = OpenFitApi.getOpenAlarmClear();
        bluetoothLeService.write(bytes);
    }

    public void sendWeatherNotifcation(String weather, String icon) {
        long id = (long)(System.currentTimeMillis() / 1000L);
        byte[] bytes = OpenFitApi.getOpenWeather(weather, icon, id);
        bluetoothLeService.write(bytes);
    }

    public void startWeather() {
        Log.d(LOG_TAG, "Starting Weather Cronjob");
        if(weatherEnabled) {
            Cronjob.start();
        }
        else {
            Cronjob.stop();
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

    private BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Stopping Service");
            reconnecting = false;
            isReconnect = false;
            isStopping = true;
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
            unregisterReceiver(bluetoothReceiver);
            unregisterReceiver(notificationReceiver);
            unregisterReceiver(smsReceiver);
            unregisterReceiver(mmsReceiver);
            unregisterReceiver(phoneReceiver);
            unregisterReceiver(phoneIdleReceiver);
            unregisterReceiver(phoneOffhookReceiver);
            unregisterReceiver(mediaReceiver);
            unregisterReceiver(alarmReceiver);
            unregisterReceiver(weatherReceiver);
            unregisterReceiver(cronReceiver);
            unbindService(mServiceConnection);
            Cronjob.stop();
            clearNotification();
            reconnectBluetoothStop();
            Log.d(LOG_TAG, "stopSelf");
            stopForeground(true);
            stopSelf();
        }
    };

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
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
            else {
                Log.d(LOG_TAG, "Received notification appName:" + appName + " title:" + title + " ticker:" + ticker + " message:" + message);
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
            String artist = MediaController.getArtist(intent);
            //String album = MediaController.getAlbum(intent);
            String track = MediaController.getTrack(intent);
            String mediaTrack = artist + " - " + track;
            Log.d(LOG_TAG, "Media sending: " + mediaTrack);
            MediaController.setTrack(mediaTrack);
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
            Log.d(LOG_TAG, "Weather updated: ");
            String name = intent.getStringExtra("name");
            String weather = intent.getStringExtra("weather");
            String description = intent.getStringExtra("description");
            String tempCur = intent.getStringExtra("tempCur");
            String tempMin = intent.getStringExtra("tempMin");
            String tempMax = intent.getStringExtra("tempMax");
            String tempUnit = intent.getStringExtra("tempUnit");
            String humidity = intent.getStringExtra("humidity");
            String pressure = intent.getStringExtra("pressure");
            String icon = intent.getStringExtra("icon");

            Log.d(LOG_TAG, "City Name: " + name);
            Log.d(LOG_TAG, "Weather: " + weather);
            Log.d(LOG_TAG, "Description: " + description);
            Log.d(LOG_TAG, "Temperature Current: " + tempCur);
            Log.d(LOG_TAG, "Temperature Min: " + tempMin);
            Log.d(LOG_TAG, "Temperature Max: " + tempMax);
            Log.d(LOG_TAG, "Temperature Unit: " + tempUnit);
            Log.d(LOG_TAG, "Humidity: " + humidity);
            Log.d(LOG_TAG, "Pressure: " + pressure);
            Log.d(LOG_TAG, "icon: " + icon);

            String weatherInfo = name + ": " + tempCur + tempUnit + "\nMin: " + tempMin + tempUnit + " Max: " + tempMax + tempUnit + "\n" + description;

            sendWeatherNotifcation(weatherInfo, icon);
        }
    };

    private BroadcastReceiver cronReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "#####CronJob#####");
            if(weatherEnabled) {
                LocationInfo.updateLastKnownLocation();
                
                if(LocationInfo.getCityName() != null && LocationInfo.getStateName() != null) {
                    String query = LocationInfo.getCityName().replace(" ", "%20") + "," + LocationInfo.getCountryCode();
                    Weather.getWeather(query);
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
                    // unexpected interruption while enabling bluetooth
                    Thread.currentThread().interrupt(); // restore interrupted flag
                    return;
                }
            }
        }

        public void close() {
            reconnecting = false;
        }
    }
}
