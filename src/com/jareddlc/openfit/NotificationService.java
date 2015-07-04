package com.jareddlc.openfit;

import android.annotation.SuppressLint;
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
        String shortMsg = sbn.getNotification().tickerText.toString();
        String tag = sbn.getTag();
        // API v19
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String notificationMsg = extras.getCharSequence("android.text").toString();

        Log.d(LOG_TAG, "Captured notification message [" + notificationMsg + "] for source [" + packageName + "]");
        Log.d(LOG_TAG, "ticker: " + shortMsg);
        Log.d(LOG_TAG, "title: " + title);
        Log.d(LOG_TAG, "tag: " + tag);

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
        String shortMsg = sbn.getNotification().tickerText.toString();
        Log.d(LOG_TAG, "Removed notification message [" + shortMsg + "] for source [" + packageName + "]");
    }
}
