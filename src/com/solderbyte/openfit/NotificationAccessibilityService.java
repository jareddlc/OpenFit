package com.solderbyte.openfit;

import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class NotificationAccessibilityService extends AccessibilityService {
    private static final String LOG_TAG = "OpenFit:NotificationAccessibilityService";

    private final String INTENT_NAME = "custom";
    private final String INTENT_PACKAGE = "package";
    private final String INTENT_MESSAGE = "message";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        if(eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            final String packageName = (String)event.getPackageName();
            Parcelable parcelable = event.getParcelableData();

            // Statusbar Notification
            if(parcelable instanceof Notification) {
                //Notification notification = (Notification) parcelable; 
                //Log.e(TAG, "Notification -> notification.tickerText :: " + notification.tickerText);
                List<CharSequence> messages = event.getText();
                if(messages.size() > 0) {
                    final String notificationMsg = (String) messages.get(0);
                    Log.d(LOG_TAG, "Captured notification message [" + notificationMsg + "] for source [" + packageName + "]");
                    try {
                        Intent mIntent = new Intent(INTENT_NAME);
                        mIntent.putExtra(INTENT_PACKAGE, packageName);
                        mIntent.putExtra(INTENT_MESSAGE, notificationMsg);
                        NotificationAccessibilityService.this.getApplicationContext().sendBroadcast(mIntent);
                    }
                    catch (Exception e) {
                        Log.e(LOG_TAG, "Failed to send intent: " + e.toString());
                    }
                }
                else {
                    Log.e(LOG_TAG, "Notification Message is empty. Can not broadcast"); 
                }
            }
            // Some other Notification
            else {
                List<CharSequence> messages = event.getText();
                if(messages.size() > 0) {
                    final String toastMsg = (String) messages.get(0);
                    Log.d(LOG_TAG, "Captured message [" + toastMsg + "] for source [" + packageName + "]");
                    try {
                        Intent mIntent = new Intent(INTENT_NAME);
                        mIntent.putExtra(INTENT_PACKAGE, packageName);
                        mIntent.putExtra(INTENT_MESSAGE, toastMsg);
                        NotificationAccessibilityService.this.getApplicationContext().sendBroadcast(mIntent);
                    }
                    catch (Exception e) {
                        Log.e(LOG_TAG, "Failed to send intent: " + e.toString());
                    }
                }
                else {
                    Log.e(LOG_TAG, "Message is empty. Can not broadcast"); 
                }
            }
        }
        else {
            Log.d(LOG_TAG, "Got un-handled Event"); 
        }
    }

    @Override
    public void onInterrupt() {

    }
    
    @Override
    protected void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        // Set the type of events that this service wants to listen to.
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;

        // Set package names to listen for or listen for all applications
        //info.packageNames = new String[] {"com.appone.totest.accessibility", "com.apptwo.totest.accessibility"};
 
        // Set the type of feedback your service will provide.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        }
        else {
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        }

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }
    
    /*
    public class ToastOrNotificationTestActivity extends Activity {
 
        private static final String TAG = "ToastOrNotificationTestActivity";
         
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
             
            final IntentFilter mIntentFilter = new IntentFilter(Constants.ACTION_CATCH_NOTIFICATION); 
            mIntentFilter.addAction(Constants.ACTION_CATCH_TOAST);
            registerReceiver(toastOrNotificationCatcherReceiver, mIntentFilter);
            Log.v(TAG, "Receiver registered.");
        }
                 
        @Override
        protected void onDestroy() {
            super.onDestroy();
            unregisterReceiver(toastOrNotificationCatcherReceiver);
        }
             
        private final BroadcastReceiver toastOrNotificationCatcherReceiver = new BroadcastReceiver() {
             
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.v(TAG, "Received message"); 
                Log.v(TAG, "intent.getAction() :: " + intent.getAction());
                Log.v(TAG, "intent.getStringExtra(Constants.EXTRA_PACKAGE) :: " + intent.getStringExtra(Constants.EXTRA_PACKAGE));
                Log.v(TAG, "intent.getStringExtra(Constants.EXTRA_MESSAGE) :: " + intent.getStringExtra(Constants.EXTRA_MESSAGE));          
            }
        };
    }
     */
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = "com.mytest.accessibility/com.mytest.accessibility.MyAccessibilityService";
        
        Log.v(LOG_TAG, "Looking for accessibility");
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(LOG_TAG, "accessibilityEnabled = " + accessibilityEnabled);
        }
        catch (SettingNotFoundException e) {
            Log.e(LOG_TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
 
        if(accessibilityEnabled == 1) {
            Log.d(LOG_TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if(settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while(splitter.hasNext()) {
                    String accessabilityService = splitter.next();
                     
                    Log.d(LOG_TAG, "-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        Log.v(LOG_TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        }
        else {
            Log.v(LOG_TAG, "***ACCESSIBILIY IS DISABLED***");
        }
                 
        return accessibilityFound;
    }
}
