package com.solderbyte.openfit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import android.util.Log;

public class Fitness {
    private static final String LOG_TAG = "OpenFit:Fitness";

    private static ByteArrayOutputStream fitnessStream = new ByteArrayOutputStream(0);
    private static int size = 0;
    private static boolean pendingData = false;

    private static PedometerTotal pedometerTotal;
    private static ArrayList<PedometerData> pedometerList = new ArrayList<PedometerData>();
    private static ArrayList<PedometerData> pedometerDailyList = new ArrayList<PedometerData>();

    public static int getSize() {
        return size;
    }

    public static PedometerTotal getPedometerTotal() {
        return pedometerTotal;
    }

    public static ArrayList<PedometerData> getPedometerList() {
        return pedometerList;
    }

    public static ArrayList<PedometerData> getPedometerDailyList() {
        return pedometerDailyList;
    }

    public static PedometerData[] getPedometerArray() {
        PedometerData[] p = new PedometerData[pedometerList.size()];
        for(int i = 0; i < pedometerList.size(); i++) {
            p[i] = pedometerList.get(i);
        }
        return p;
    }

    public static void clearFitnessData() {
        fitnessStream = null;
        fitnessStream = new ByteArrayOutputStream(0);
        pedometerTotal = null;
        pedometerList = new ArrayList<PedometerData>();
        pedometerDailyList = new ArrayList<PedometerData>();
    }

    public static boolean isPendingData() {
        return pendingData;
    }

    public static boolean isFitnessData(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte type = buffer.get();

        if(type == 2) {
            if(data.length <= 6) {
                return false;
            }
            pendingData = true;
            size = buffer.getInt();
            Log.d(LOG_TAG, "Fitness data size: " + size);
            clearFitnessData();
            return true;
        }
        else {
            return false;
        }
    }

    public static void addData(byte[] data) {
        try {
            fitnessStream.write(data);
            //Log.d(LOG_TAG, "Adding data to buffer: " + data.length);
        } 
        catch (IOException e) {
            Log.e(LOG_TAG, "Error writting fitness data to buffer");
            e.printStackTrace();
        }

        if(fitnessStream.size() > size && fitnessStream.size() < (size + 6)) {
            Log.d(LOG_TAG, "Received all message data");
            pendingData = false;
        }
    }

    @SuppressWarnings("unused")
    public static void parseData() {
        Log.d(LOG_TAG, "Parsing data");
        ByteBuffer buffer = ByteBuffer.wrap(fitnessStream.toByteArray());
        buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);

        if(buffer.capacity() < 14) {
            return;
        }
        byte msgType = buffer.get();
        int msgSize = buffer.getInt();
        int byte4 = buffer.getInt();
        int byte1 = buffer.getInt();
        byte byteFF = buffer.get();
        Log.d(LOG_TAG, "type: " + msgType);
        Log.d(LOG_TAG, "msgSize: " + msgSize);

        while(buffer.hasRemaining()) {
            byte fitnessType = buffer.get();
            Log.d(LOG_TAG, "fitnessType: " + fitnessType);
            if(fitnessType ==  4) {
                Log.d(LOG_TAG, "Pedemeter Data");
                parsePedometer(buffer);
            }
            else if(fitnessType == 6) {
                Log.d(LOG_TAG, "Sleep Data");
                parseSleep(buffer);
            }
            else {
                Log.d(LOG_TAG, "unsupported");
                depleteBuffer(buffer);
            }
        }

        Log.d(LOG_TAG, "remaining buffer: " + buffer.remaining());
    }


    @SuppressWarnings("unused")
    public static void parseSleep(ByteBuffer buffer) {
        int sleepSize = buffer.getInt();
        Log.d(LOG_TAG, "Sleep size: " + sleepSize);

        for(int i = 0; i < sleepSize; i++) {
            int index = buffer.getInt();
            long timeStamp = buffer.getInt() * 1000L;
            int status = buffer.getInt();
            Date date = new Date(timeStamp);

            //Log.d(LOG_TAG, "idex: " + index);
            //Log.d(LOG_TAG, "date: " + date.toString());
            //Log.d(LOG_TAG, "status: " + status);
        }
    }

    public static void parsePedometer(ByteBuffer buffer) {
        int pedometerSize = buffer.getInt();
        Log.d(LOG_TAG, "Pedometer size: " + pedometerSize);
        Calendar cal = Calendar.getInstance();
        int pedometerTotalSteps = 0;
        float pedometerTotalDistance = 0;
        float pedometerTotalCalorie = 0;
        int currentDay = 0;
        int dailySteps = 0;
        float dailyDistance = 0;
        float dailyCalorie = 0;

        for(int i = 0; i < pedometerSize; i++) {
            long timeStamp = buffer.getInt() * 1000L;
            int step = buffer.getInt();
            float distance = Float.intBitsToFloat(buffer.getInt());
            float calorie = Float.intBitsToFloat(buffer.getInt());
            Date date = new Date(timeStamp);
            cal.setTime(date);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            if(day != currentDay) {
                currentDay = day;

                if(i == 0) {
                    dailySteps += step;
                    dailyDistance += distance;
                    dailyCalorie += calorie;
                }
                else if(i == (pedometerSize - 1)) {
                    pedometerDailyList.add(new PedometerData(timeStamp, dailySteps, dailyDistance, dailyCalorie));
                }
                else {
                    pedometerDailyList.add(new PedometerData(timeStamp, dailySteps, dailyDistance, dailyCalorie));
                    dailySteps = 0;
                    dailyDistance = 0;
                    dailyCalorie = 0;
                    dailySteps += step;
                    dailyDistance += distance;
                    dailyCalorie += calorie;
                }
            }
            else {
                dailySteps += step;
                dailyDistance += distance;
                dailyCalorie += calorie;
            }

            pedometerTotalSteps += step;
            pedometerTotalDistance += distance;
            pedometerTotalCalorie += calorie;

            pedometerList.add(new PedometerData(timeStamp, step, distance, calorie));
            //Log.d(LOG_TAG, "day: " + day);
            //Log.d(LOG_TAG, "i: " + i);
            //Log.d(LOG_TAG, "date: " + date.toString());
            //Log.d(LOG_TAG, "step: " + step);
            //Log.d(LOG_TAG, "distance: " + distance);
            //Log.d(LOG_TAG, "calorie: " + calorie);
        }
        /*Log.d(LOG_TAG, "pedometerDailyDates: " + pedometerDailyDates.size());
        for(int j = 0; j < pedometerDailyDates.size(); j++) {
            cal.setTime(pedometerDailyDates.get(j));
            int day = cal.get(Calendar.DAY_OF_MONTH);
            Log.d(LOG_TAG, day + " : " + pedometerDailyList.get(j).getTimeStamp() + " : " + pedometerDailyList.get(j).getSteps() + " : " + pedometerDailyList.get(j).getDistance() + " : " + pedometerDailyList.get(j).getCalories());
        }*/
        
        pedometerTotal = new PedometerTotal(pedometerTotalSteps, pedometerTotalDistance, pedometerTotalCalorie);
        Log.d(LOG_TAG, "totalSteps: " + pedometerTotal.getSteps());
        Log.d(LOG_TAG, "totalDistance: " + pedometerTotal.getDistance());
        Log.d(LOG_TAG, "totalCalorie: " + pedometerTotal.getCalories());
    }

    public static void parseExercise(ByteBuffer buffer) {
        
        //0801000000320000000000000000040000002861462F50010000010000000000000000000000FFFFFFFFFFFFFFFF012861462F5001000009010000002861462F500100001F85EB51B85E30408B36000000000000040000000C01000000C93210560F0500000000803F52000000C3F582410153460000E41B4F3C52B8863F00000000000000008200000000000000000000000501000000C3EB0C56814A0D560A509E4201000000150000000001000000AD3210561C0000007A142F4359DAC74235E6020080FB911FF2490200D2FB01001398020021BF02000101000000EDAC105610270000EBAC105603050000005D150A56102700000D370A56102700008B880B561027000003DA0C5610270000EE2B0E5610270000
        /*int exerciseSize = buffer.getInt();
        Log.d(LOG_TAG, "Excercise size: " + exerciseSize);

        for(int i = 0; i < exerciseSize; i++) {
        }*/
    }

    @SuppressWarnings("unused")
    public static void depleteBuffer(ByteBuffer buffer) {
        while(buffer.hasRemaining()) {
            byte b = buffer.get();
        }
    }
}
