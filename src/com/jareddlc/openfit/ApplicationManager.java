package com.jareddlc.openfit;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ListAdapter;

public class ApplicationManager {
    private static final String LOG_TAG = "OpenFit:ApplicationManager";
    
    private CharSequence[] installedPackageNames = new CharSequence[0];
    private CharSequence[] installedAppNames = new CharSequence[0];
    ArrayList<Drawable> installedPackageIcons = new ArrayList<Drawable>();
    
    private CharSequence[] listeningPackageNames = new CharSequence[0];
    private CharSequence[] listeningAppNames = new CharSequence[0];
    ArrayList<Drawable> listeningPackageIcons = new ArrayList<Drawable>();

    private Drawable dailerIcon;
    private Drawable smsIcon;
    private Drawable clockIcon;

    ArrayList<String> listeningListPackageNames = new ArrayList<String>();

    public ApplicationManager() {
    }

    public ListAdapter getListeningAdapter(Context context) {
        ArrayList<String> aName = new ArrayList<String>();
        ArrayList<String> pName = new ArrayList<String>();
        ArrayList<Drawable> iDraw = new ArrayList<Drawable>();

        for(int i = 0; i < listeningListPackageNames.size(); i++) {
            //Log.d(LOG_TAG, "installed:" + listeningListPackageNames.get(i));
            PackageManager pm = context.getPackageManager();
            Drawable icon;
            try {
                icon = pm.getApplicationIcon(listeningListPackageNames.get(i));
                icon.setBounds(0, 0, 144, 144);
            }
            catch(NameNotFoundException e) {
                icon = null;
            }
            String packageName = listeningListPackageNames.get(i);
            ApplicationInfo packageInfo;
            try {
                packageInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            }
            catch (NameNotFoundException e) {
                packageInfo = null;
            }
            String appName = (String) pm.getApplicationLabel(packageInfo);
            pName.add(packageName);
            aName.add(appName);
            iDraw.add(icon);
        }
        listeningPackageNames = pName.toArray(new CharSequence[pName.size()]);
        listeningAppNames = aName.toArray(new CharSequence[aName.size()]);
        listeningPackageIcons = iDraw;
        ListAdapter adapter = new ArrayAdapterWithIcon(context, aName, iDraw);;
        return adapter;
    }

    public ListAdapter getInstalledAdapter(Context context) {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<String> aName = new ArrayList<String>();
        ArrayList<String> pName = new ArrayList<String>();
        ArrayList<Drawable> iDraw = new ArrayList<Drawable>();

        for(ApplicationInfo packageInfo : packages) {
            // filter out system apps
            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1 || packageInfo.packageName.contains("com.google.android")) {
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
        installedPackageNames = pName.toArray(new CharSequence[pName.size()]);
        installedAppNames = aName.toArray(new CharSequence[aName.size()]);
        installedPackageIcons = iDraw;
        ListAdapter adapter = new ArrayAdapterWithIcon(context, aName, iDraw);

        try {
            Log.d(LOG_TAG, "Grabbed dailer/sms icon");
            dailerIcon = pm.getApplicationIcon("com.android.dialer");
            smsIcon = pm.getApplicationIcon("com.android.mms");
            clockIcon = pm.getApplicationIcon("com.android.deskclock");
        } 
        catch (NameNotFoundException e) {
            Log.d(LOG_TAG, "Cannot find dailer/sms icon");
        }

        return adapter;
    }
    
    public CharSequence[] getListeningPackageNames() {
        return listeningPackageNames;
    }

    public CharSequence[] getListeningAppNames() {
        return listeningAppNames;
    }

    public CharSequence[] getInstalledPackageNames() {
        return installedPackageNames;
    }

    public CharSequence[] getInstalledAppNames() {
        return installedAppNames;
    }

    public Drawable getIcon(String packageName) {
        Drawable icon = null;
        for(int i = 0; i < installedPackageNames.length; i++) {
            //Log.d(LOG_TAG, "pckg :" + packageNames[i]);
            if(installedPackageNames[i].equals(packageName)) {
                icon = installedPackageIcons.get(i);
                break;
            }
        }
        return icon;
    }

    public void addInstalledApp(String packageName) {
        listeningListPackageNames.add(packageName);
    }

    public void delInstalledApp(String packageName) {
        listeningListPackageNames.remove(packageName);
    }
    
    public ArrayList<String> getInstalledApp() {
        return listeningListPackageNames;
    }

    public Drawable getDailerIcon() {
        return dailerIcon;
    }

    public Drawable getSmsIcon() {
        return smsIcon;
    }

    public Drawable getClockIcon() {
        return clockIcon;
    }
}

