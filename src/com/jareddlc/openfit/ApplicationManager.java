package com.jareddlc.openfit;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListAdapter;

public class ApplicationManager extends DialogFragment {
    private static final String LOG_TAG = "OpenFit:ApplicationManager";
    
    private CharSequence[] packageNames = new CharSequence[0];
    private CharSequence[] appNames = new CharSequence[0];
    ArrayList<Drawable> packageIcons = new ArrayList<Drawable>();
    ArrayList<String> listeningNames = new ArrayList<String>();
    private ListAdapter adapter;

    private AlertDialog.Builder installedApps;
    private AlertDialog.Builder listeningApps;
    
    public ApplicationManager(Context context) {
        this.getInstalledApps(context);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select Application");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                Intent msg = new Intent("appListener");
                msg.putExtra("packageName", packageNames[index]);
                msg.putExtra("appName", appNames[index]);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(msg);
                Log.d(LOG_TAG, "Clicked: " + appNames[index] + " : " + packageNames[index]);
            }
        });

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
        packageIcons = iDraw;
        adapter = new ArrayAdapterWithIcon(context, aName, iDraw);
        
        /*installedApps = new AlertDialog.Builder(context);
        installedApps.setTitle("Select Application");
        installedApps.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int index) {
                Intent msg = new Intent("appListener");
                msg.putExtra("packageName", packageNames[index]);
                msg.putExtra("appName", appNames[index]);
                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(msg);
                Log.d(LOG_TAG, "Clicked: " + appNames[index] + " : " + packageNames[index]);
            }
        });
        installedApps.create();*/
    }
    
    public void showInstalledApps() {
        installedApps.show();
    }
    
    public Drawable getIcon(String packageName) {
        Log.d(LOG_TAG, "Getting icon for package :" + packageName);
        Drawable icon = null;
        for(int i = 0; i < packageNames.length; i++) {
            //Log.d(LOG_TAG, "pckg :" + packageNames[i]);
            if(packageNames[i].equals(packageName)) {
                icon = packageIcons.get(i);
                break;
            }
        }
        return icon;
    }

    public void addInstalledApp(String packageName) {
        Log.d(LOG_TAG, "Adding package to listeningApps: " + packageName);
        listeningNames.add(packageName);
    }

}

