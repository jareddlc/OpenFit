package com.solderbyte.openfit.util;

import android.util.Log;
import java.lang.reflect.Field;
import java.util.TimeZone;

public class OpenFitTimeZoneUtil {
    private static final String LOG_TAG = "OpenFit:OpenFitTimeZoneUtil";

    private static int[] getTransitions(TimeZone pTimeZone) {
        if(!pTimeZone.useDaylightTime()) {
            return null;
        }
        try {
            Field oField = pTimeZone.getClass().getDeclaredField("mTransitions");
            oField.setAccessible(true);
            int[] paramTimeZone = (int[])oField.get(pTimeZone);
            return paramTimeZone;
        }
        catch(NoSuchFieldException eTimeZone) {
            Log.w(LOG_TAG, eTimeZone.getMessage(), eTimeZone);
            return null;
        }
        catch (IllegalArgumentException eTimeZone) {
            Log.w(LOG_TAG, eTimeZone.getMessage(), eTimeZone);
            return null;
        }
        catch (IllegalAccessException eTimeZone) {
            Log.w(LOG_TAG, eTimeZone.getMessage(), eTimeZone);
            return null;
        }
    }

    public static long nextTransition(String pString, long pLong) {
        return nextTransition(TimeZone.getTimeZone(pString), pLong);
    }

    public static long nextTransition(TimeZone pTimeZone, long pLong) {
        int[] oTimeZone = getTransitions(pTimeZone);
        if(oTimeZone != null) {
            int i = (int)(pLong / 1000L);
            int j = oTimeZone.length;
            int k = 0;
            while(k < j) {
                if(i < oTimeZone[k]) {
                    return 1000L * (long)oTimeZone[k];
                }
                k++;
            }
        }
        return pLong;
    }

    public static long prevTransition(String pString, long pLong) {
        return prevTransition(TimeZone.getTimeZone(pString), pLong);
    }

    public static long prevTransition(TimeZone pTimeZone, long pLong) {
        int[] oTimeZone = getTransitions(pTimeZone);
        if(oTimeZone != null) {
            int i = (int)(pLong / 1000L);
            int j = oTimeZone.length;
            int k = 0;
            while(k < j) {
                if(i < oTimeZone[k]) {
                    if(k == 0) {
                        return 0L;
                    }
                    else {
                        return 1000L * (long)oTimeZone[k - 1];
                    }
                }
                k++;
            }
        }
        return pLong;
    }
}
