package com.solderbyte.openfit;

import java.util.ArrayList;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationService extends NotificationListenerService {
    private static final String LOG_TAG = "OpenFit:NotificationService";

    private ArrayList<String> listeningListPackageNames = new ArrayList<String>();
    private Context context;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "Created NotificationService");
        this.registerReceiver(stopServiceReceiver, new IntentFilter("stopOpenFitService"));
        this.registerReceiver(appsReceiver, new IntentFilter("listeningApps"));
        context = getApplicationContext();

        Intent msg = new Intent("NotificationService");
        context.sendBroadcast(msg);
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String ticker = null;
        String message = null;
        String submessage = null;
        String summary = null;
        String info = null;
        String title = null;
        try {
            ticker = (String) sbn.getNotification().tickerText;
        }
        catch(Exception e) {
            // nothing
        }
        String tag = sbn.getTag();
        long time = sbn.getPostTime();
        int id = sbn.getId();

        // API v19
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        //String category = notification.category; API v21

        if(extras.getCharSequence("android.title") != null) {
            title = extras.getString("android.title");
        }
        if(extras.getCharSequence("android.text") != null) {
            message = extras.getCharSequence("android.text").toString();
        }
        if(extras.getCharSequence("android.subText") != null) {
            submessage = extras.getCharSequence("android.subText").toString();
        }
        if(extras.getCharSequence("android.summaryText") != null) {
            summary = extras.getCharSequence("android.summaryText").toString();
        }
        if(extras.getCharSequence("android.infoText") != null) {
            info = extras.getCharSequence("android.infoText").toString();
        }

        Log.d(LOG_TAG, "Captured notification message: " + message + " from source:" + packageName);

        if(listeningListPackageNames.contains(packageName)) {
            Log.d(LOG_TAG, "ticker: " + ticker);
            Log.d(LOG_TAG, "title: " + title);
            Log.d(LOG_TAG, "message: " + message);
            Log.d(LOG_TAG, "tag: " + tag);
            Log.d(LOG_TAG, "time: " + time);
            Log.d(LOG_TAG, "id: " + id);
            Log.d(LOG_TAG, "submessage: " + submessage);
            Log.d(LOG_TAG, "summary: " + summary);
            Log.d(LOG_TAG, "info: " + info);
            //Log.d(LOG_TAG, "category: " + category);

            Intent msg = new Intent("notification");
            msg.putExtra("packageName", packageName);
            msg.putExtra("ticker", ticker);
            msg.putExtra("title", title);
            msg.putExtra("message", message);
            msg.putExtra("time", time);
            msg.putExtra("id", id);
            if(submessage != null) {
                msg.putExtra("submessage", submessage);
            }

            //LocalBroadcastManager.getInstance(context).sendBroadcast(msg);
            context.sendBroadcast(msg);
            Log.d(LOG_TAG, "Sending notification message: " + message + " from source:" + packageName);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String shortMsg = "";
        try {
            shortMsg = (String) sbn.getNotification().tickerText;
        }
        catch(Exception e) {
            
        }
        Log.d(LOG_TAG, "Removed notification message: " + shortMsg + " from source:" + packageName);
    }

    public void setListeningPackageNames(ArrayList<String> packageNames) {
        listeningListPackageNames = packageNames;
    }

    private BroadcastReceiver stopServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Stopping Service");
            unregisterReceiver(appsReceiver);
            unregisterReceiver(stopServiceReceiver);
            stopSelf();
        }
    };

    private BroadcastReceiver appsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> listeningApps = intent.getStringArrayListExtra("data");
            setListeningPackageNames(listeningApps);
            Log.d(LOG_TAG, "Recieved listeningApps");
        }
    };
}
