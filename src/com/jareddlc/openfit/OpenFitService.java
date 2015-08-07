package com.jareddlc.openfit;

import java.util.Arrays;
import java.util.Calendar;



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

public class OpenFitService extends Service {
    private static final String LOG_TAG = "OpenFit:OpenFitService";

    // services
    private BluetoothLeService bluetoothLeService;
    private  Handler mHandler;
    private PackageManager pManager;
    private static ReconnectBluetoothThread reconnectThread;

    private int NotificationId = 1;
    private boolean smsEnabled = false;
    private boolean phoneEnabled = false;
    private boolean isConnected = false;
    private boolean isReconnect = false;
    private boolean reconnecting = false;
    private SmsListener smsListener;
    private MmsListener mmsListener;
    private TelephonyManager telephony;
    private DialerListener dailerListener;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand" + intent);
        // register receivers
        this.registerReceiver(stopServiceReceiver, new IntentFilter("stopOpenFitService"));
        this.registerReceiver(bluetoothReceiver, new IntentFilter("bluetooth"));
        this.registerReceiver(notificationReceiver, new IntentFilter("notification"));
        this.registerReceiver(smsReceiver, new IntentFilter("sms"));
        this.registerReceiver(mmsReceiver, new IntentFilter("mms"));
        this.registerReceiver(phoneReceiver, new IntentFilter("phone"));
        pManager = this.getPackageManager();

        // start service
        this.createNotification();
        this.startBluetoothHandler();
        this.startBluetoothService();
        this.startNotificationListenerService();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        unregisterReceiver(stopServiceReceiver);
        super.onDestroy();
    }
    
    public void sendServiceStarted() {
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
        reconnecting = true;
        reconnectThread = new ReconnectBluetoothThread();
        reconnectThread.start();
    }

    public void reconnectBluetoothStop() {
        Log.d(LOG_TAG, "stopping reconnect thread");
        reconnecting = false;
        if(reconnectThread != null) {
            reconnectThread.close();
            reconnectThread = null;
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
            }
            else {
                Intent i = new Intent("bluetoothUI");
                i.putExtra("message", "isDisconnected");
                sendBroadcast(i);
            }
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
        Log.d(LOG_TAG, "Setting up message handler");
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
                    isConnected = true;
                    if(reconnecting) {
                        reconnectBluetoothStop();
                    }
                }
                if(bluetoothMessage != null && bluetoothMessage.equals("isDisconnectedRfComm")) {
                    isConnected = false;
                    if(isReconnect) {
                        reconnectBluetoothService();
                    }
                }
            }
        };
    }

    public void handleBluetoothMessage(String message, Intent intent) {
        if(message != null && !message.isEmpty()) {
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
                setTime(value);
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
        }
    }

    public void handleBluetoothData(byte[] data) {
        if(Arrays.equals(data, OpenFitApi.getReady())) {
            Log.d(LOG_TAG, "Recieved ready message");
            bluetoothLeService.write(OpenFitApi.getUpdate());
            bluetoothLeService.write(OpenFitApi.getUpdateFollowUp());
            bluetoothLeService.write(OpenFitApi.getFotaCommand());
            //bluetoothLeService.write(OpenFitApi.getCurrentTimeInfo(false));
        }
    }

    public void startNotificationListenerService() {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        this.startService(notificationIntent);
        Log.d(LOG_TAG, "Starting notification service");
    }

    public void startDailerListener() {
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
        // register listeners
        if(smsEnabled) {
            smsListener = new SmsListener(this);
            IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            this.registerReceiver(smsListener, smsFilter);
            mmsListener = new MmsListener(this);
            IntentFilter mmsFilter = new IntentFilter("android.provider.Telephony.WAP_PUSH_RECEIVED");
            this.registerReceiver(mmsListener, mmsFilter);
            Log.d(LOG_TAG, "sms listening");
            Log.d(LOG_TAG, "mms listening");
        }
        else {
            if(smsListener != null) {
                this.unregisterReceiver(smsListener);
                this.unregisterReceiver(mmsListener);
            }
        }
    }

    public void createNotification() {
        Intent stopService =  new Intent("stopOpenFitService");
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, stopService, PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.open_fit_notification);
        nBuilder.setContentTitle("Open Fit");
        nBuilder.setContentText("Listening for notifications");
        //nBuilder.setContentIntent(pIntent);
        nBuilder.setAutoCancel(true);
        nBuilder.setOngoing(true);
        nBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pIntent);

        // Sets an ID for the notification
        NotificationManager nManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        nManager.notify(NotificationId, nBuilder.build());
    }

    public void clearNotification() {
        NotificationManager nManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        nManager.cancel(NotificationId);
    }

    public void setTime(boolean is24Hour) {
        bluetoothLeService.write(OpenFitApi.getCurrentTimeInfo(is24Hour));
        //bluetoothLeService.write(OpenFitApi.getOpenFitWelcomeNotification());
        //long idTimestamp = (long)(System.currentTimeMillis() / 1000L);
        //byte[] bytes = OpenFitApi.getOpenNotification("Gmail", "number", "Hello OpenFit", "Far far away,", idTimestamp);
        //bluetoothLeService.write(bytes);
    }

    public void sendAppNotification(String packageName, String sender, String title, String message, int id) {
        long idTimestamp = (long)(System.currentTimeMillis() / 1000L);
        byte[] bytes = OpenFitApi.getOpenNotification(packageName, sender, title, message, idTimestamp);
        bluetoothLeService.write(bytes);
    }

    public void sendEmailNotification(String packageName, String sender, String title, String message, int id) {
        long idTimestamp = (long)(System.currentTimeMillis() / 1000L);
        byte[] bytes = OpenFitApi.getOpenEmail(sender, title, message, message, idTimestamp);
        bluetoothLeService.write(bytes);
    }

    public void sendDialerNotification(String number) {
        long id = (long)(System.currentTimeMillis() / 1000L);
        String title = "Phone Call";
        String sender = "Phone Call";
        String message = "Receiving phone call from: " + number;
        String name = getContactName(number);
        if(name != null) {
            sender = name;
            message = "Receiving phone call from: " + name;
        }
        byte[] bytes = OpenFitApi.getOpenNotification(sender, number, title, message, id);
        bluetoothLeService.write(bytes);
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

    private BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Stopping Service");
            clearNotification();
            unregisterReceiver(bluetoothReceiver);
            unregisterReceiver(notificationReceiver);
            unregisterReceiver(smsReceiver);
            unregisterReceiver(mmsReceiver);
            unregisterReceiver(phoneReceiver);
            unbindService(mServiceConnection);
            if(smsEnabled) {
                unregisterReceiver(smsListener);
                unregisterReceiver(mmsListener);
            }
            if(phoneEnabled) {
                telephony.listen(dailerListener, PhoneStateListener.LISTEN_NONE);
                dailerListener.destroy();
            }
            reconnectBluetoothStop();
            stopSelf();
        }
    };

    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String message = intent.getStringExtra("message");
            handleBluetoothMessage(message, intent);
            Log.d(LOG_TAG, "Received bluetooth command: " + message);
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
