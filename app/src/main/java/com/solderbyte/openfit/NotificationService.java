package com.solderbyte.openfit;

import java.util.ArrayList;
import java.util.List;

import com.solderbyte.openfit.util.OpenFitIntent;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Process;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

public class NotificationService extends NotificationListenerService {
    private static final String LOG_TAG = "OpenFit:NotificationService";

    private ArrayList<String> ListPackageNames = new ArrayList<String>();
    private PackageManager packageManager = null;
    private Context context;

    // view data
    private String NOTIFICATION_TITLE = null;
    private String NOTIFICATION_TEXT = null;
    private String NOTIFICATION_BIG_TEXT = null;

    // applications
    private String APP_FB_MESSENGER = "com.facebook.orca";
    private String APP_WHATSAPP = "com.whatsapp";
    private String APP_G_HANGOUTS = "com.google.android.talk";


    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "Created NotificationService");
        this.registerReceiver(serviceStopReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_STOP));
        this.registerReceiver(applicationsReceiver, new IntentFilter(OpenFitIntent.INTENT_SERVICE_NOTIFICATION_APPLICATIONS));
        context = getApplicationContext();
        packageManager = this.getPackageManager();
        this.checkNotificationListenerService();

        Intent msg = new Intent(OpenFitIntent.INTENT_SERVICE_NOTIFICATION);
        context.sendBroadcast(msg);
        super.onCreate();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(LOG_TAG, "onNotificationPosted");
        String packageName = sbn.getPackageName();
        String appName = this.getAppName(packageName);

        if(!ListPackageNames.contains(packageName)) {
            Log.d(LOG_TAG, "filtered by list");
            return;
        }

        // API v19
        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;
        this.getViewNotification(notification, packageName);
        //String category = notification.category; API v21
        if((notification.flags & Notification.FLAG_ONGOING_EVENT) != 0) {
            Log.d(LOG_TAG, "filtered by flags");
            return;
        }

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
            Log.d(LOG_TAG, "Notification does not have tickerText");
        }
        String tag = sbn.getTag();
        long time = sbn.getPostTime();
        int id = sbn.getId();

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
        if(extras.getCharSequence("android.infoText") != null) {
            info = extras.getCharSequence("android.infoText").toString();
        }

        Log.d(LOG_TAG, "Captured notification message: " + message + " from source:" + packageName);
        Log.d(LOG_TAG, "ticker: " + ticker);
        Log.d(LOG_TAG, "title: " + title);
        Log.d(LOG_TAG, "message: " + message);
        Log.d(LOG_TAG, "tag: " + tag);
        Log.d(LOG_TAG, "time: " + time);
        Log.d(LOG_TAG, "id: " + id);
        Log.d(LOG_TAG, "submessage: " + submessage);
        Log.d(LOG_TAG, "summary: " + summary);
        Log.d(LOG_TAG, "info: " + info);
        Log.d(LOG_TAG, "view title: " + NOTIFICATION_TITLE);
        Log.d(LOG_TAG, "view big text: " + NOTIFICATION_BIG_TEXT);
        Log.d(LOG_TAG, "view text: " + NOTIFICATION_TEXT);


        String APP_NAME = appName;
        String PACKAGE_NAME = packageName;
        String NAME = null;
        String CONTACT = null;
        String GROUP = null;
        String MESSAGE = null;
        int ID = id;
        long CREATED = time;

        if(packageName.equals(APP_FB_MESSENGER)) {
            // facebook messenger
            // appName: Messenger
            // packageName: com.facebook.orca
            // name: title or view title
            // contact: n/a
            // message: message or view big text

            if(title != null) {
                NAME = title;
            }
            else {
                NAME = NOTIFICATION_TITLE;
            }
            if(message != null) {
                MESSAGE = message;
            }
            else if(NOTIFICATION_BIG_TEXT != null) {
                MESSAGE = NOTIFICATION_BIG_TEXT;
            }
            else {
                MESSAGE = NOTIFICATION_TEXT;
            }
        }
        else if(packageName.equals(APP_G_HANGOUTS)) {
            // google hangouts
            // appName: Hangouts
            // packageName: com.google.android.talk
            // name: title or view title (name)
            // contact: summary or view text (email)
            // message: message or view big text

            if(title != null) {
                NAME = title;
            }
            else {
                NAME = NOTIFICATION_TITLE;
            }
            if(summary != null) {
                CONTACT = summary;
            }
            else {
                CONTACT = NOTIFICATION_TEXT;
            }
            if(message != null) {
                MESSAGE = message;
            }
            else {
                MESSAGE = NOTIFICATION_BIG_TEXT;
            }
        }
        else if(packageName.equals(APP_WHATSAPP)) {
            // whatsapp
            // appName: WhatsApp
            // packageName: com.whatsapp
            // name: title or view title (group: name @ group)
            // contact: n/a
            // message: message or view text big text

            try {
                if(message.matches(".*(\\d+).new messages.*") || NOTIFICATION_TEXT.matches(".*(\\d+).new messages.*")) {
                    Log.d(LOG_TAG, "ignoring message");
                    return;
                }
            }
            catch(Exception e) {
                Log.w(LOG_TAG, "regex error: " + e.getMessage());
            }

            String[] split;
            if(title != null) {
                // group message
                if(title.contains("@")) {
                    split = title.split("(.+)@(.+)");
                    NAME = split[0];
                    GROUP = split[1];
                }
                else {
                    NAME = title;
                }
            }
            else if(NOTIFICATION_TITLE != null) {
                if(title.contains("@")) {
                    split = NOTIFICATION_TITLE.split("(.+)@(.+)");
                    NAME = split[0];
                    GROUP = split[1];
                }
                else {
                    NAME = NOTIFICATION_TITLE;
                }
            }
            else {
                NAME = title;
            }
            if(NOTIFICATION_BIG_TEXT != null) {
                MESSAGE = NOTIFICATION_BIG_TEXT;
            }
            else if(NOTIFICATION_TEXT != null) {
                MESSAGE = NOTIFICATION_TEXT;
            }
            else {
                MESSAGE = message;
            }
        }
        else {
            if(title != null) {
                NAME = title;
            }
            else {
                NAME = NOTIFICATION_TITLE;
            }
            if(NOTIFICATION_BIG_TEXT != null) {
                MESSAGE = NOTIFICATION_BIG_TEXT;
            }
            else if(message != null) {
                MESSAGE = message;
            }
            else {
                MESSAGE = NOTIFICATION_TEXT;
            }
        }

        Intent msg = new Intent(OpenFitIntent.INTENT_NOTIFICATION);
        msg.putExtra("packageName", packageName);
        msg.putExtra("ticker", ticker);
        msg.putExtra("title", NAME);
        msg.putExtra("message", MESSAGE);
        //msg.putExtra("title", title);
        //msg.putExtra("message", message);
        msg.putExtra("time", time);
        msg.putExtra("id", id);
        if(submessage != null) {
            msg.putExtra("submessage", submessage);
        }

        context.sendBroadcast(msg);
        Log.d(LOG_TAG, "Sending notification message: " + message + " from source:" + packageName);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(LOG_TAG, "onNotificationRemoved");
        String packageName = sbn.getPackageName();
        String shortMsg = "";
        try {
            shortMsg = (String) sbn.getNotification().tickerText;
        }
        catch(Exception e) {

        }
        Log.d(LOG_TAG, "Removed notification message: " + shortMsg + " from source:" + packageName);
    }

    public void checkNotificationListenerService() {
        Log.d(LOG_TAG, "checkNotificationListenerService");
        boolean isNotificationListenerRunning = false;
        ComponentName thisComponent = new ComponentName(this, NotificationService.class);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(Integer.MAX_VALUE);
        if(runningServices == null) {
            Log.d(LOG_TAG, "running services is null");
            return;
        }
        for(ActivityManager.RunningServiceInfo service : runningServices) {
            if(service.service.equals(thisComponent)) {
                Log.d(LOG_TAG, "checkNotificationListenerService service - pid: " + service.pid + ", currentPID: " + Process.myPid() + ", clientPackage: " + service.clientPackage + ", clientCount: " + service.clientCount + ", clientLabel: " + ((service.clientLabel == 0) ? "0" : "(" + getResources().getString(service.clientLabel) + ")"));
                if(service.pid == Process.myPid() /*&& service.clientCount > 0 && !TextUtils.isEmpty(service.clientPackage)*/) {
                    isNotificationListenerRunning = true;
                }
            }
        }
        if(isNotificationListenerRunning) {
            Log.d(LOG_TAG, "NotificationListenerService is running");
            return;
        }
        Log.d(LOG_TAG, "NotificationListenerService is not running, trying to start");
        this.toggleNotificationListenerService();
    }

    public void toggleNotificationListenerService() {
        Log.d(LOG_TAG, "toggleNotificationListenerService");
        // adb shell dumpsys notification
        // force start of notification service
        ComponentName thisComponent = new ComponentName(this, NotificationService.class);
        packageManager.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        packageManager.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    public void setPackageNames(ArrayList<String> packageNames) {
        ListPackageNames = packageNames;
    }

    public String getAppName(String packageName) {
        if(packageName == null) {
            return null;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(LOG_TAG, "Cannot get application info");
        }
        String appName = (String) packageManager.getApplicationLabel(appInfo);
        return appName;
    }

    public boolean getViewNotification(Notification n, String packageName) {
        Log.d(LOG_TAG, "getViewNotification");
        if(packageName == null) {
            return false;
        }
        Resources resources = null;
        try {
            resources = packageManager.getResourcesForApplication(packageName);
        }
        catch(Exception e){
            Log.e(LOG_TAG, "Failed to get PackageManager: " + e.getMessage());
        }
        if(resources == null) {
            Log.e(LOG_TAG, "No PackageManager resources");
            return false;
        }

        int TITLE = resources.getIdentifier("android:id/title", null, null);
        int BIG_TEXT = resources.getIdentifier("android:id/big_text", null, null);
        int TEXT = resources.getIdentifier("android:id/text", null, null);

        RemoteViews views = n.bigContentView;
        if(views == null) {
            views = n.contentView;
        }
        if(views == null) {
            Log.d(LOG_TAG, "No RemoteViews");
            return false;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup localView = null;
        try {
            localView = (ViewGroup) inflater.inflate(views.getLayoutId(), null);
            views.reapply(context, localView);
        }
        catch(Exception e) {
            Log.e(LOG_TAG, "Error with local view: " + e.getMessage());
            return false;
        }

        Log.d(LOG_TAG, "about to get views");
        TextView title = (TextView) localView.findViewById(TITLE);
        if(title != null) {
            NOTIFICATION_TITLE = title.getText().toString();
        }
        else {
            NOTIFICATION_TITLE = null;
        }
        TextView big = (TextView) localView.findViewById(BIG_TEXT);
        if(big != null) {
            NOTIFICATION_BIG_TEXT = big.getText().toString();
        }
        else {
            NOTIFICATION_BIG_TEXT = null;
        }
        TextView text = (TextView) localView.findViewById(TEXT);
        if(text != null) {
            NOTIFICATION_TEXT = text.getText().toString();
        }
        else {
            NOTIFICATION_TEXT = null;
        }

        return true;
    }

    private BroadcastReceiver serviceStopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "Stopping Service");
            unregisterReceiver(applicationsReceiver);
            unregisterReceiver(serviceStopReceiver);
            stopSelf();
        }
    };

    private BroadcastReceiver applicationsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> applications = intent.getStringArrayListExtra(OpenFitIntent.INTENT_EXTRA_DATA);
            setPackageNames(applications);
            Log.d(LOG_TAG, "Recieved listeningApps: " + applications.size());
        }
    };
}
