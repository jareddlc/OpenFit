package com.jareddlc.openfit;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

public class DialogInstalled extends DialogFragment {
    private static final String LOG_TAG = "OpenFit:DialogInstalled";
    
    private CharSequence[] items;
    
    public DialogInstalled(Context context) {
        this.getInstalledApps(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Application");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                Log.d(LOG_TAG, "Clicked: " + index);
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void getInstalledApps(Context context) {
        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<String> list = new ArrayList<String>();
        for(ApplicationInfo packageInfo : packages) {
            // filter out system apps
            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                String appName = (String) pm.getApplicationLabel(packageInfo);
                //Log.d(LOG_TAG, "Installed package :" + packageInfo.packageName);
                //Log.d(LOG_TAG, "Installed package :" + appName);
                list.add(appName);
            }
        }
        items = list.toArray(new CharSequence[list.size()]);
    }

}
