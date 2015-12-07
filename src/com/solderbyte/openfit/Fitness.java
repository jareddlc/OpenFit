package com.solderbyte.openfit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.solderbyte.openfit.util.OpenFitData;


import android.util.Log;

public class Fitness {
    private static final String LOG_TAG = "OpenFit:Fitness";

    private static ByteArrayOutputStream fitnessStream = new ByteArrayOutputStream(0);
    private static int size = 0;
    private static boolean pendingData = false;

    private static PedometerTotal pedometerTotal = null;
    private static ArrayList<PedometerData> pedometerList = new ArrayList<PedometerData>();
    private static ArrayList<PedometerData> pedometerDailyList = new ArrayList<PedometerData>();
    private static ProfileData profileData = null;

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

    public static ProfileData getProfileData() {
        return profileData;
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

        if(OpenFitApi.byteArrayToHexString(data).contains(OpenFitApi.byteArrayToHexString(OpenFitApi.getFitnessCycling()))) {
            return false;
        }
        if(OpenFitApi.byteArrayToHexString(data).contains(OpenFitApi.byteArrayToHexString(OpenFitApi.getFitnessRunning()))) {
            return false;
        }

        if(type == 2) {
            if(data.length <= 6) {
                return false;
            }
            size = buffer.getInt();

            int msgType = buffer.getInt();
            Log.d(LOG_TAG, "msgType: " + msgType);
            if(msgType == OpenFitData.FITNESS_MENU || msgType == OpenFitData.FITNESS_CANCEL || msgType == OpenFitData.FITNESS_UNKOWN) {
                return false;
            }
            pendingData = true;
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
            Log.d(LOG_TAG, "Fitness type: " + fitnessType);
            if(fitnessType ==  OpenFitData.DATA_TYPE_USER_PROFILE) {
                Log.d(LOG_TAG, "User Profile");
                parseUserProfile(buffer);
            }
            if(fitnessType ==  OpenFitData.DATA_TYPE_PEDO_INFO) {
                Log.d(LOG_TAG, "Pedemeter Info");
                parsePedoInfo(buffer);
            }
            else if(fitnessType == OpenFitData.DATA_TYPE_SLEEP_INFO) {
                Log.d(LOG_TAG, "Sleep Info");
                parseSleepInfo(buffer);
            }
            else if(fitnessType == OpenFitData.DATA_TYPE_COACHING_VARS) {
                Log.d(LOG_TAG, "Coaching Vars");
                parseCoachingVars(buffer);
            }
            else if(fitnessType == OpenFitData.DATA_TYPE_COACHING_EXERCISERESULT) {
                Log.d(LOG_TAG, "Coaching Excercise Result");
                parseCoachingExerciseResult(buffer);
            }
            else if(fitnessType == OpenFitData.DATA_TYPE_COACHING_RESULTRECORD) {
                Log.d(LOG_TAG, "Coaching Result Record");
                parseCoachingResultRecord(buffer);

            }
            else {
                Log.d(LOG_TAG, "Unsupported: " + fitnessType);
                //logBuffer(buffer);
                depleteBuffer(buffer);
            }
        }

        Log.d(LOG_TAG, "remaining buffer: " + buffer.remaining());
    }

    public static void parseUserProfile(ByteBuffer buffer) {
        int userProfileSize = buffer.getInt();
        Log.d(LOG_TAG, "User profile size: " + userProfileSize);
        for(int i = 0; i < userProfileSize; i++) {
            long timeStamp = buffer.getInt() * 1000L;
            int age = buffer.getInt();
            float height = Float.intBitsToFloat(buffer.getInt());
            float weight = Float.intBitsToFloat(buffer.getInt());
            int gender = buffer.getInt();
            int birthday = buffer.getInt();
            int heightUnit = buffer.getInt();
            int weightUnit = buffer.getInt();
            int distanceUnit = buffer.getInt();
            int activity = buffer.getInt();

            profileData = new ProfileData(timeStamp, age, height, weight, gender, birthday, heightUnit, weightUnit, distanceUnit, activity);

            Date date = new Date(timeStamp);
            Log.d(LOG_TAG, "time stamp: " + timeStamp);
            Log.d(LOG_TAG, "date: " + date);
            Log.d(LOG_TAG, "age: " + age);
            Log.d(LOG_TAG, "height: " + height + OpenFitData.getHeightUnit(heightUnit));
            Log.d(LOG_TAG, "weight: " + weight + OpenFitData.getWeightUnit(weightUnit));
            Log.d(LOG_TAG, "gender: " + OpenFitData.getGender(gender));
            Log.d(LOG_TAG, "activity: " + activity);
            Log.d(LOG_TAG, "birthday: " + birthday);
            Log.d(LOG_TAG, "distanceUnit: " + OpenFitData.getDistanceUnit(distanceUnit));
        }
    }

    public static void parsePedoInfo(ByteBuffer buffer) {
        int pedometerSize = buffer.getInt();
        Log.d(LOG_TAG, "Pedometer info size: " + pedometerSize);
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
        }

        pedometerTotal = new PedometerTotal(pedometerTotalSteps, pedometerTotalDistance, pedometerTotalCalorie);
        Log.d(LOG_TAG, "totalSteps: " + pedometerTotal.getSteps());
        Log.d(LOG_TAG, "totalDistance: " + pedometerTotal.getDistance());
        Log.d(LOG_TAG, "totalCalorie: " + pedometerTotal.getCalories());
    }

    @SuppressWarnings("unused")
    public static void parseSleepInfo(ByteBuffer buffer) {
        int sleepSize = buffer.getInt();
        Log.d(LOG_TAG, "Sleep info size: " + sleepSize);

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

    public static void parseCoachingVars(ByteBuffer buffer) {
        Log.d(LOG_TAG, "Coaching vars");

        int ac = buffer.getInt();
        byte maxHeartrate = buffer.get();
        long maxMET = buffer.getLong();
        int recovery = buffer.getInt();
        long startDate = buffer.getLong();
        int trainingLevel = buffer.getInt();
        long lastTrainingLevel = buffer.getLong();
        int previousToPrevious = buffer.getInt();
        int previousTrainingLevel = buffer.getInt();
        int lastestFeedbackPhraseNumber = buffer.get();
        long lastestExerciseTime = buffer.getLong();

        Log.d(LOG_TAG, "ac: " + ac);
        Log.d(LOG_TAG, "maxHeartrate: " + maxHeartrate);
        Log.d(LOG_TAG, "maxMET: " + maxMET);
        Log.d(LOG_TAG, "recovery: " + recovery);
        Log.d(LOG_TAG, "timeStamp: " + startDate);
        Log.d(LOG_TAG, "trainingLevel: " + trainingLevel);
        Log.d(LOG_TAG, "lastTrainingLevel: " + lastTrainingLevel);
        Log.d(LOG_TAG, "previousToPrevious: " + previousToPrevious);
        Log.d(LOG_TAG, "previousTrainingLevel: " + previousTrainingLevel);
        Log.d(LOG_TAG, "lastestFeedbackPhraseNumber: " + lastestFeedbackPhraseNumber);
        Log.d(LOG_TAG, "lastestExerciseTime: " + lastestExerciseTime);
    }

    public static void parseCoachingExerciseResult(ByteBuffer buffer) {
        int exerciseSize = buffer.getInt();
        Log.d(LOG_TAG, "Coaching exercise result size: " + exerciseSize);

        for(int i = 0; i < exerciseSize; i++) {
            long endTime = buffer.getLong();
            double distance = buffer.getDouble();
            int trainingLoadPeak = buffer.getInt();
            int maxMET = buffer.getInt();
            int recovery = buffer.getInt();

            Log.d(LOG_TAG, "endTime: " + endTime);
            Log.d(LOG_TAG, "distance: " + distance);
            Log.d(LOG_TAG, "trainingLoadPeak: " + trainingLoadPeak);
            Log.d(LOG_TAG, "maxMET: " + maxMET);
            Log.d(LOG_TAG, "recovery: " + recovery);
        }
    }

    public static void parseCoachingResultRecord(ByteBuffer buffer) {
        int exerciseSize = buffer.getInt();
        Log.d(LOG_TAG, "Coaching result record size: " + exerciseSize);

        for(int i = 0; i < exerciseSize; i++) {
            int timeStamp = buffer.getInt();
            long duration = buffer.getLong();
            double calorie = buffer.getDouble();
            double heartrate = buffer.getDouble();
            double distance = buffer.getDouble();
            byte fitnessLevel = buffer.get();
            int type = buffer.getInt();
            float avgSpeed = Float.intBitsToFloat(buffer.getInt());
            float maxSpeed = Float.intBitsToFloat(buffer.getInt());
            float maxAlt = Float.intBitsToFloat(buffer.getInt());
            float minAlt = Float.intBitsToFloat(buffer.getInt());
            byte maxHeartrate = buffer.get();

            Log.d(LOG_TAG, "timeStamp: " + timeStamp);
            Log.d(LOG_TAG, "duration: " + duration);
            Log.d(LOG_TAG, "calorie: " + calorie);
            Log.d(LOG_TAG, "heartrate: " + heartrate);
            Log.d(LOG_TAG, "distance: " + distance);
            Log.d(LOG_TAG, "fitnessLevel: " + fitnessLevel);
            Log.d(LOG_TAG, "type: " + type);
            Log.d(LOG_TAG, "avgSpeed: " + avgSpeed);
            Log.d(LOG_TAG, "maxSpeed: " + maxSpeed);
            Log.d(LOG_TAG, "maxAlt: " + maxAlt);
            Log.d(LOG_TAG, "minAlt: " + minAlt);
            Log.d(LOG_TAG, "maxHeartrate: " + maxHeartrate);
        }
    }

    @SuppressWarnings("unused")
    public static void depleteBuffer(ByteBuffer buffer) {
        while(buffer.hasRemaining()) {
            byte b = buffer.get();
        }
    }

    public static void logBuffer(ByteBuffer buffer) {
        int remaining = buffer.capacity() - buffer.position();
        Log.d(LOG_TAG, "Remaining" + remaining);
        byte[] b = new byte[remaining];
        for(int i = 0; i < remaining; i++) {
            b[i] = buffer.get();
        }
        Log.d(LOG_TAG, "logBuffer" + OpenFitApi.byteArrayToHexString(b));
    }
}
