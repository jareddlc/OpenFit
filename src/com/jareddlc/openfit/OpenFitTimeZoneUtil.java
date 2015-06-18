package com.jareddlc.openfit;

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
        catch(NoSuchFieldException paramTimeZone) {
            Log.w(LOG_TAG, paramTimeZone.getMessage(), paramTimeZone);
            return null;
        }
        catch (IllegalArgumentException paramTimeZone) {
            Log.w(LOG_TAG, paramTimeZone.getMessage(), paramTimeZone);
            return null;
        }
        catch (IllegalAccessException paramTimeZone) {
            Log.w(LOG_TAG, paramTimeZone.getMessage(), paramTimeZone);
            return null;
        }
    }

    public static long nextTransition(String paramString, long paramLong) {
        return nextTransition(TimeZone.getTimeZone(paramString), paramLong);
    }

    public static long nextTransition(TimeZone pTimeZone, long paramLong) {
        int[] paramTimeZone = getTransitions(pTimeZone);
        if(paramTimeZone == null) {}
        for(;;) {
            //return paramLong;
            int j = (int)(paramLong / 1000L);
            int k = paramTimeZone.length;
            int i = 0;
            while(i < k) {
                if(j < paramTimeZone[i]) {
                    return paramTimeZone[i] * 1000L;
                    }
                i += 1;
            }
        }
    }

    public static long prevTransition(String paramString, long paramLong) {
        return prevTransition(TimeZone.getTimeZone(paramString), paramLong);
    }

    public static long prevTransition(TimeZone pTimeZone, long paramLong) {
        int[] paramTimeZone = getTransitions(pTimeZone);
        if(paramTimeZone == null) {}
        for(;;) {
            //return paramLong;
            int j = (int)(paramLong / 1000L);
            int k = paramTimeZone.length;
            int i = 0;
            while(i < k) {
                if(j < paramTimeZone[i])  {
                    if(i == 0) {
                        return 0L;
                    }
                    return paramTimeZone[(i - 1)] * 1000L;
                }
                i += 1;
            }
        }
    }
}
