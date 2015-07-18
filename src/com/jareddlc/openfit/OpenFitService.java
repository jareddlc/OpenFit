package com.jareddlc.openfit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class OpenFitService extends Service {
    private static final String LOG_TAG = "OpenFit:OpenFitService";
    
    private int NotificationId = 1;

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand" + intent);
        registerReceiver(stopServiceReceiver, new IntentFilter("stopOpenFitService"));

        this.createNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        unregisterReceiver(stopServiceReceiver);
        super.onDestroy();
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
}
