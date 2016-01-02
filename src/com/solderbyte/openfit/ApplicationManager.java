package com.solderbyte.openfit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.solderbyte.openfit.ui.ArrayAdapterWithIcon;

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

    private CharSequence[] notificationPackageNames = new CharSequence[0];
    private CharSequence[] notificationAppNames = new CharSequence[0];
    ArrayList<Drawable> notificationPackageIcons = new ArrayList<Drawable>();

    ArrayList<String> notificationListPackageNames = new ArrayList<String>();
    List<ApplicationInfo> installedPackages = null;

    private Context context = null;

    private CharSequence[] whitelist = new CharSequence[] {
        "com.google.android",
        "com.android.deskclock",
        "com.android.email",
        "com.asus.email",
        "com.whatsapp"
    };

    public ApplicationManager() {
        Log.d(LOG_TAG, "Creating ApplicationManager");
    }

    public void setContext(Context cntxt) {
        Log.d(LOG_TAG, "Setting context");
        context = cntxt;
    }

    public ListAdapter getNotificationAdapter() {
        ArrayList<String> aName = new ArrayList<String>();
        ArrayList<String> pName = new ArrayList<String>();
        ArrayList<Drawable> iDraw = new ArrayList<Drawable>();
        Collections.sort(notificationListPackageNames);

        for(int i = 0; i < notificationListPackageNames.size(); i++) {
            //Log.d(LOG_TAG, "installed:" + listeningListPackageNames.get(i));
            PackageManager pm = context.getPackageManager();
            Drawable icon;
            try {
                icon = pm.getApplicationIcon(notificationListPackageNames.get(i));
                icon.setBounds(0, 0, 144, 144);
            }
            catch(NameNotFoundException e) {
                icon = null;
            }
            String packageName = notificationListPackageNames.get(i);
            ApplicationInfo packageInfo;
            try {
                packageInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            }
            catch (NameNotFoundException e) {
                packageInfo = null;
            }
            String appName = null;
            if(packageInfo != null) {
                appName = (String) pm.getApplicationLabel(packageInfo);
            }
            else {
                appName = packageName;
            }
            pName.add(packageName);
            aName.add(appName);
            iDraw.add(icon);
        }
        notificationPackageNames = pName.toArray(new CharSequence[pName.size()]);
        notificationAppNames = aName.toArray(new CharSequence[aName.size()]);
        notificationPackageIcons = iDraw;
        ListAdapter adapter = new ArrayAdapterWithIcon(context, aName, iDraw);
        return adapter;
    }

    public ListAdapter getInstalledAdapter() {
        Log.d(LOG_TAG, "getInstalledAdapter");
        PackageManager pm = context.getPackageManager();
        if(installedPackages == null) {
            installedPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        }
        //installedPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Log.d(LOG_TAG, "getInstalledAdapter done");
        ArrayList<String> aName = new ArrayList<String>();
        ArrayList<String> pName = new ArrayList<String>();
        ArrayList<Drawable> iDraw = new ArrayList<Drawable>();
        Collections.sort(installedPackages, new ApplicationInfo.DisplayNameComparator(pm));

        for(ApplicationInfo packageInfo : installedPackages) {
            // filter out system apps
            //if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1 || checkWhitelist(packageInfo.packageName)) {
                String appName = (String) pm.getApplicationLabel(packageInfo);
                Log.d(LOG_TAG, "Installed package :" + packageInfo.packageName);
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
            //}
        }
        installedPackageNames = pName.toArray(new CharSequence[pName.size()]);
        installedAppNames = aName.toArray(new CharSequence[aName.size()]);
        installedPackageIcons = iDraw;
        ListAdapter adapter = new ArrayAdapterWithIcon(context, aName, iDraw);

        return adapter;
    }

    public Drawable loadIcon(String packageName) {
        if(context == null) {
            return null;
        }

        PackageManager pm = context.getPackageManager();
        Drawable icon;
        try {
            icon = pm.getApplicationIcon(packageName);
            icon.setBounds(0, 0, 144, 144);
        }
        catch(NameNotFoundException e) {
            icon = null;
        }
        return icon;
    }

    public boolean checkWhitelist(String packageName) {
        boolean found = false;
        for(int i = 0; i < whitelist.length; i++) {
            if(packageName.contains(whitelist[i])) {
                found = true;
            }
        }
        return found;
    }

    public CharSequence[] getListeningPackageNames() {
        return notificationPackageNames;
    }

    public CharSequence[] getListeningAppNames() {
        return notificationAppNames;
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

    public void addNotificationApp(String packageName) {
        notificationListPackageNames.add(packageName);
    }

    public void delNotificationApp(String packageName) {
        notificationListPackageNames.remove(packageName);
    }

    public ArrayList<String> getNotificationApplications() {
        return notificationListPackageNames;
    }

    public void clearNotificationApplications() {
        notificationListPackageNames = new ArrayList<String>();
    }
}

