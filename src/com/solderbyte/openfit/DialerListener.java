package com.solderbyte.openfit;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DialerListener extends PhoneStateListener {
    private static final String LOG_TAG = "TurquoiseBicuspid:DialerListener";

    private Context context;

    public DialerListener(Context cntxt) {
        Log.d(LOG_TAG, "Dailer listening");
        context = cntxt;
    }

    public void destroy() {
        context = null;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        switch(state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.d(LOG_TAG, "Phone: Idle - "+incomingNumber);
                Intent msgi = new Intent("phone:idle");
                msgi.putExtra("sender", incomingNumber);
                context.sendBroadcast(msgi);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.d(LOG_TAG, "Phone: Offhook - "+incomingNumber);
                Intent msgo = new Intent("phone:offhook");
                msgo.putExtra("sender", incomingNumber);
                context.sendBroadcast(msgo);
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.d(LOG_TAG, "Phone: Ringing - "+incomingNumber);
                Intent msg = new Intent("phone");
                msg.putExtra("sender", incomingNumber);
                context.sendBroadcast(msg);
                break;
        }
    }
}
