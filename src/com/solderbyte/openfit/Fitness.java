package com.solderbyte.openfit;

import android.util.Log;

public class Fitness {
    private static final String LOG_TAG = "OpenFit:Fitness";

    public static boolean isFitnessData(byte[] data) {
        Log.d(LOG_TAG, "isFitnessData");

        byte type = data[0];
        if(type == 2) {
            return true;
        }
        else {
            return false;
        }
    }

    public static void parseData(byte[] data) {

    }
}
