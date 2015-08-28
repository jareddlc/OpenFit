package com.solderbyte.openfit;

import android.content.Intent;
import android.content.IntentFilter;

public class Alarm {

    public static IntentFilter getIntentFilter() {
        IntentFilter alarm = new IntentFilter();
        alarm.addAction("com.android.deskclock.ALARM_ALERT");
        alarm.addAction("com.android.deskclock.ALARM_SNOOZE");
        alarm.addAction("com.android.deskclock.ALARM_DISMISS");
        alarm.addAction("com.android.deskclock.ALARM_DONE");
        return alarm;
    }
    
    public static String getAction(Intent intent) {
        String action = intent.getAction();

        if(action.equals("com.android.deskclock.ALARM_ALERT")) {
            action = "START";
        }
        else if(action.equals("com.android.deskclock.ALARM_SNOOZE")) {
            action = "STOP";
        }
        else if(action.equals("com.android.deskclock.ALARM_DISMISS")) {
            action = "STOP";
        }
        else if(action.equals("com.android.deskclock.ALARM_DONE")) {
            action = "STOP";
        }
        else {
            action = "STOP";
        }

        return action;
    }
}
