package com.jareddlc.openfit.util;

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
            Field localField = pTimeZone.getClass().getDeclaredField("mTransitions");
            localField.setAccessible(true);
            int[] paramTimeZone = (int[])localField.get(pTimeZone);
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
        if(oTimeZone == null) {
          return pLong;
        }
        for(;;) {
            int j = (int)(pLong / 1000L);
            int k = oTimeZone.length;
            int i = 0;
            while(i < k) {
                if(j < oTimeZone[i]) {
                    return oTimeZone[i] * 1000L;
                    }
                i += 1;
            }
        }
    }

    public static long prevTransition(String pString, long pLong) {
        return prevTransition(TimeZone.getTimeZone(pString), pLong);
    }

    public static long prevTransition(TimeZone pTimeZone, long pLong) {
        int[] oTimeZone = getTransitions(pTimeZone);
        if(oTimeZone == null) {
            return pLong;
        }
        for(;;) {
            int j = (int)(pLong / 1000L);
            int k = oTimeZone.length;
            int i = 0;
            while(i < k) {
                if(j < oTimeZone[i])  {
                    if(i == 0) {
                        return 0L;
                    }
                    return oTimeZone[(i - 1)] * 1000L;
                }
                i += 1;
            }
        }
    }
}
