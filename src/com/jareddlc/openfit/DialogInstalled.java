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
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;

public class DialogInstalled extends DialogFragment {
    private static final String LOG_TAG = "OpenFit:DialogInstalled";
    
    private CharSequence[] packageNames;
    private CharSequence[] appNames;
    private Integer[] icons;
    private ListAdapter adapter;
    
    public DialogInstalled(Context context) {
        this.getInstalledApps(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Application");
        /*builder.setItems(appNames, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                Log.d(LOG_TAG, "Clicked: " + appNames[index] + " : " + packageNames[index]);
            }
        });*/
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                Log.d(LOG_TAG, "Clicked: " + appNames[index] + " : " + packageNames[index]);
            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void getInstalledApps(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<String> aName = new ArrayList<String>();
        ArrayList<String> pName = new ArrayList<String>();
        ArrayList<Drawable> iDraw = new ArrayList<Drawable>();
        for(ApplicationInfo packageInfo : packages) {
            // filter out system apps
            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1) {
                String appName = (String) pm.getApplicationLabel(packageInfo);
                //Log.d(LOG_TAG, "Installed package :" + packageInfo.packageName);
                //Log.d(LOG_TAG, "Installed package :" + appName);
                Log.d(LOG_TAG, "Installed icon :" + packageInfo.icon);
                Drawable icon;
                try {
                    icon = pm.getApplicationIcon(packageInfo.packageName);
                    icon.setBounds(0, 0, 144, 144);
                }
                catch(NameNotFoundException e) {
                    icon = null;
                }
                pName.add(packageInfo.packageName);
                aName.add(appName);
                iDraw.add(icon);
            }
        }
        packageNames = pName.toArray(new CharSequence[pName.size()]);
        appNames = aName.toArray(new CharSequence[aName.size()]);
        adapter = new ArrayAdapterWithIcon(context, aName, iDraw);
    }

}

