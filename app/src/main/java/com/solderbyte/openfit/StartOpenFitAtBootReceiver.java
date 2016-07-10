package com.solderbyte.openfit;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartOpenFitAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, OpenFitService.class);
            context.startService(serviceIntent);
        }
    }
}
