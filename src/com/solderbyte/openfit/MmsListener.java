package com.solderbyte.openfit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MmsListener extends BroadcastReceiver {
    private static final String LOG_TAG = "TurquoiseBicuspid:MmsListener";

    private Context context;

    public MmsListener(Context cntxt) {
        Log.d(LOG_TAG, "MMS listening");
        context = cntxt;
    }

    @Override
    public void onReceive(Context cntxt, Intent intent) {
        Log.d(LOG_TAG, "MMS: Intent received");
        if(intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            try {
                if(bundle != null) {
                    String type = intent.getType();
                    if(type.trim().equalsIgnoreCase("application/vnd.wap.mms-message")) {
                        byte[] buffer = bundle.getByteArray("data");
                        String phoneNumber = new String(buffer);
                        int index = phoneNumber.indexOf("/TYPE");
                        if(index > 0 && (index - 15) > 0) {
                            int newIndx = index - 15;
                            phoneNumber = phoneNumber.substring(newIndx, index);
                            index = phoneNumber.indexOf("+");
                            if(index > 0) {
                                phoneNumber = phoneNumber.substring(index);
                                String senderNum = phoneNumber;
                                Log.d(LOG_TAG, "MMS: "+senderNum);
                                Intent msg = new Intent("mms");
                                msg.putExtra("sender", senderNum);
                                context.sendBroadcast(msg);
                            }
                        }
                    }
                }
            }
            catch(Exception e) {
                Log.e(LOG_TAG, "Error: intent.getType()", e);
            }
        }
    }

}