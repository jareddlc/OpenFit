package com.jareddlc.openfit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class OpenFitService extends Service {
    private static final String LOG_TAG = "OpenFit:OpenFitService";

    // services
    private static BluetoothLeService bluetoothLeService;
    private  Handler mHandler;

    private int NotificationId = 1;

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

        // start service
        this.createNotification();
        this.startBluetoothHandler();
        this.startBluetoothService();
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
        final ServiceConnection mServiceConnection = new ServiceConnection() {
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
                // Automatically connects to the device upon successful start-up initialization.
                //bluetoothLeService.connect(mDeviceAddress);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                bluetoothLeService = null;
            }
        };
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        this.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

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
            }
            if(message.equals("disconnect")) {
                bluetoothLeService.disconnectRfcomm();
            }
            if(message.equals("setEntries")) {
                bluetoothLeService.setEntries();
            }
            if(message.equals("setDevice")) {
                String deviceMac = intent.getStringExtra("data");
                bluetoothLeService.setDevice(deviceMac);
            }
        }
    }

    public void startNotificationListenerService() {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        this.startService(notificationIntent);
    }

    public void createNotification() {
        Intent stopService =  new Intent("stopOpenFitService");
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, stopService, PendingIntent.FLAG_UPDATE_CURRENT);
        
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this);
        nBuilder.setSmallIcon(R.drawable.open_fit_notification);
        nBuilder.setContentTitle("Open Fit");
        nBuilder.setContentText("Listening for notifications");
        nBuilder.setContentIntent(pIntent);
        nBuilder.setAutoCancel(true);
        nBuilder.setOngoing(true);
        nBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", pIntent);

        // Sets an ID for the notification
        int mNotificationId = 1;
        NotificationManager nManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        nManager.notify(mNotificationId, nBuilder.build());
    }

    public void clearNotification() {
        NotificationManager nManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        nManager.cancel(NotificationId);
    }

    protected BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Stopping Service");
            clearNotification();
            stopSelf();
        }
    };

    protected BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String message = intent.getStringExtra("message");
            handleBluetoothMessage(message, intent);
            Log.d(LOG_TAG, "Received bluetooth command: " + message);
        }
    };
}
