package com.jareddlc.openfit;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NotificationService extends NotificationListenerService {
    private static final String LOG_TAG = "OpenFit:NotificationService";

    Context context;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "Created NotificationService");
        super.onCreate();
        context = getApplicationContext();
    }
    
    @SuppressLint("NewApi")
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        String shortMsg = "";
        try {
            shortMsg = (String) sbn.getNotification().tickerText;
        }
        catch(Exception e) {
            
        }
        String tag = sbn.getTag();
        // API v19
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        //String category = notification.category; API v21
        String title = extras.getString("android.title");
        String notificationMsg = extras.getCharSequence("android.text").toString();

        Log.d(LOG_TAG, "Captured notification message: " + notificationMsg + " \nfrom source:" + packageName);
        Log.d(LOG_TAG, "ticker: " + shortMsg);
        Log.d(LOG_TAG, "title: " + title);
        Log.d(LOG_TAG, "tag: " + tag);
        //Log.d(LOG_TAG, "category: " + category);

        Intent msg = new Intent("Notification");
        msg.putExtra("packageName", packageName);
        msg.putExtra("ticker", shortMsg);
        msg.putExtra("title", title);
        msg.putExtra("notificationMsg", notificationMsg);

        LocalBroadcastManager.getInstance(context).sendBroadcast(msg);
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
        Log.d(LOG_TAG, "Removed notification message: " + shortMsg + " \nfrom source:" + packageName);
    }
}
