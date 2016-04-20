package com.solderbyte.openfit;


import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;

import java.sql.Date;
import java.util.ArrayList;

public class ExerciseGPSStorage {

    private static final String LOG_TAG = "OpenFit:ExerciseGPSStorage";

    private static ExerciseDatabase db = null;

    private static float consumedCalorie = 0.0f;
    private static float declineDistance = 0.0f;
    private static float inclineDistance = 0.0f;
    private static float maxAltitude = 0.0f;
    private static float maxSpeed = 0.0f;
    private static float minAltitude = 0.0f;
    private static float totalDistance = 0.0f;
    private static float averageSpeed = 0.0f;

    private static final float INCLINATION_THRESHOLD = (float)Math.toRadians(2.28);

    public ExerciseGPSStorage(Context ctx) {

        db = new ExerciseDatabase(ctx);
    }

    public int createExercise(int exType) {
        Log.d(LOG_TAG, "INSERT INTO " + db.EXERCISE_TABLE + ": type=" + exType);

        ContentValues values = new ContentValues();
        values.put("type", exType);
        SQLiteDatabase wdb = db.getWritableDatabase();
        int id = (int)wdb.insert(db.EXERCISE_TABLE, null, values);
        wdb.close();

        SQLiteDatabase rdb = db.getReadableDatabase();
        long cnt  = DatabaseUtils.queryNumEntries(rdb, db.EXERCISE_TABLE);
        if (cnt > 30) {
            String[] select = {
                    "id"
            };
            Cursor c = rdb.query(db.EXERCISE_TABLE, select, null, null, null, null, "id LIMIT 1");
            if (c.getCount() == 1) {
                c.moveToFirst();
                deleteExercise(Integer.parseInt(c.getString(0)));
            }
            c.close();
        }
        rdb.close();

        return id;
    }

    public void insertExerciseData(int exerciseId, double lon, double lat, float altitude, float totalDistance, float speed, long timestamp) {
        Log.d(LOG_TAG, "INSERT INTO " + db.DATA_TABLE + ": lon=" + lon + ": lat=" + lat + ": alt=" + altitude
                + ": totalDistance=" + totalDistance + ": speed=" + speed + ": timestamp" + new Date(timestamp));

        ContentValues values = new ContentValues();
        values.put("exerciseId", exerciseId);
        values.put("lon", lon);
        values.put("lat", lat);
        values.put("altitude", altitude);
        values.put("totalDistance", totalDistance);
        values.put("speed", speed);
        values.put("timestamp", timestamp);
        SQLiteDatabase wdb = db.getWritableDatabase();
        wdb.insert(db.DATA_TABLE, null, values);
        wdb.close();
    }

    public void updateExerciseTimestamp(int id, long start, long end) {
        Log.d(LOG_TAG, "UPDATE " + db.EXERCISE_TABLE + ": start=" + new Date(start) + ": end=" + new Date(end) + ": id=" + id);

        ContentValues values = new ContentValues();
        values.put("timestampStart", start);
        values.put("timestampEnd", end);
        SQLiteDatabase wdb = db.getWritableDatabase();
        wdb.update(db.EXERCISE_TABLE, values, "id=" + id, null);
        wdb.close();
    }

    public void updateProfile(float height, float weight) {
        Log.d(LOG_TAG, "UPDATE " + db.PROFILE_TABLE + ": height=" + height + ": weight=" + weight);

        ContentValues values = new ContentValues();
        values.put("height", height);
        values.put("weight", weight);
        SQLiteDatabase wdb = db.getWritableDatabase();
        wdb.update(db.PROFILE_TABLE, values, "id=1", null);
        wdb.close();
    }

    public void deleteExercise(int id) {
        Log.d(LOG_TAG, "Delete exercise data with id: " + id);

        SQLiteDatabase wdb = db.getWritableDatabase();
        wdb.delete(db.EXERCISE_TABLE, "id=" + id, null);
        wdb.close();
    }

    public void computeExerciseResults(int exerciseId) {
        Log.d(LOG_TAG, "READING DATA FROM - " + db.DATA_TABLE + " exerciseId: " + exerciseId);

        consumedCalorie = 0.0f;
        declineDistance = 0.0f;
        inclineDistance = 0.0f;
        maxAltitude = 0.0f;
        maxSpeed = 0.0f;
        minAltitude = 0.0f;
        totalDistance = 0.0f;
        averageSpeed = 0.0f;

        String[] selectGPS = {
            "id",
            "lon",
            "lat",
            "altitude",
            "totalDistance",
            "speed",
            "timestamp"
        };

        String[] selectProfile = {
                "height",
                "weight"
        };

        SQLiteDatabase rdb = db.getReadableDatabase();
        Cursor c = rdb.query(db.DATA_TABLE, selectGPS, "exerciseId=" + exerciseId, null, null, null, "id");
        Cursor cP = rdb.query(db.PROFILE_TABLE, selectProfile, "id=1", null, null, null, null);

        float firstAlt = 0;
        float firstDist = 0;
        long timestamp = 0;
        long prevTimestamp = 0;

        int cnt = c.getCount();
        if (cnt > 0) {
            Log.d(LOG_TAG, "GPS DATA LEN: " + cnt);
            c.moveToFirst();

            maxAltitude += Float.parseFloat(c.getString(c.getColumnIndex("altitude")));
            minAltitude += Float.parseFloat(c.getString(c.getColumnIndex("altitude")));
            maxSpeed += Float.parseFloat(c.getString(c.getColumnIndex("speed")));
            averageSpeed += Float.parseFloat(c.getString(c.getColumnIndex("speed")));
            totalDistance = Float.parseFloat(c.getString(c.getColumnIndex("totalDistance")));
            prevTimestamp = Long.parseLong(c.getString(c.getColumnIndex("timestamp")));

            cP.moveToFirst();
            float userWeight = Float.parseFloat(cP.getString(cP.getColumnIndex("weight")));

            firstAlt = maxAltitude;
            firstDist = totalDistance;

            declineDistance = 0;
            inclineDistance = 0;

            while (c.moveToNext()) {
                timestamp = Long.parseLong(c.getString(c.getColumnIndex("timestamp")));

                if (timestamp == prevTimestamp) {
                    continue;
                }
                prevTimestamp = timestamp;

                float altitude = Float.parseFloat(c.getString(c.getColumnIndex("altitude")));
                if (altitude > maxAltitude) {
                    maxAltitude = altitude;
                }
                if (altitude < minAltitude) {
                    minAltitude = altitude;
                }
                float speed = Float.parseFloat(c.getString(c.getColumnIndex("speed")));
                if (speed > maxSpeed) {
                    maxSpeed = speed;
                }
                averageSpeed += Float.parseFloat(c.getString(c.getColumnIndex("speed")));
                totalDistance = Float.parseFloat(c.getString(c.getColumnIndex("totalDistance")));

                float distDiff = Math.abs(totalDistance-firstDist);
                if (distDiff > 100) {
                    double tg = Math.atan((altitude-firstAlt)/distDiff);
                    if (tg >= INCLINATION_THRESHOLD) {
                        inclineDistance += distDiff;
                    }
                    if (tg <= -INCLINATION_THRESHOLD) {
                        declineDistance += distDiff;
                    }
                    firstAlt = altitude;
                    firstDist = totalDistance;
                }
            }

            averageSpeed /= cnt;

            float flatDistance = totalDistance - inclineDistance - declineDistance;
            consumedCalorie = (6.82E-4f * inclineDistance + 3.41E-4f * flatDistance + 1.705E-4f * declineDistance) * userWeight;

            Log.d(LOG_TAG, "INCLINE: " + inclineDistance + " DECLINE: " + declineDistance + " FLAT: " + flatDistance);
            Log.d(LOG_TAG, "Total distance: " + totalDistance + " maxAltitude: " + maxAltitude + " minAltitude: " + minAltitude +
                    "maxSpeed: " + maxSpeed + " avgSpeed: " + averageSpeed + " calories: " + consumedCalorie);
        }
        c.close();
        cP.close();
        rdb.close();
    }

    public static ArrayList<GPSData> getGpsDataListForExercise (long timestampStart, long timestampEnd) {
        ArrayList<GPSData> gpsDataList = new ArrayList<GPSData>();

        SQLiteDatabase rdb = db.getReadableDatabase();
        String[] select = {
                "id"
        };
        Cursor c = rdb.query(db.EXERCISE_TABLE, select, "timestampStart=" + timestampStart + " AND timestampEnd=" + timestampEnd, null, null, null, null);
        if (c.getCount() == 1) {
            c.moveToFirst();
            int id = Integer.parseInt(c.getString(0));
            String[] gpsSelect = {
                    "lon",
                    "lat",
                    "altitude",
                    "totalDistance",
                    "speed",
                    "timestamp"
            };
            c.close();
            c = rdb.query(db.DATA_TABLE, gpsSelect, "exerciseId=" + id, null, null, null, "id");

            float tD;
            float s;
            float lo;
            float la;
            float alt;
            long tS;

            long prevTimestamp = 0;
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                tD = Float.parseFloat(c.getString(c.getColumnIndex("totalDistance")));
                s = Float.parseFloat(c.getString(c.getColumnIndex("speed")));
                lo = Float.parseFloat(c.getString(c.getColumnIndex("lon")));
                la = Float.parseFloat(c.getString(c.getColumnIndex("lat")));
                alt = Float.parseFloat(c.getString(c.getColumnIndex("altitude")));
                tS = Long.parseLong(c.getString(c.getColumnIndex("timestamp")));

                if (tS < timestampStart) {
                    tS = timestampStart;
                }
                if (tS > timestampEnd) {
                    tS = timestampEnd;
                }
                if (prevTimestamp != tS) {
                    gpsDataList.add(new GPSData(tD, s, lo, la, alt, tS));
                    prevTimestamp = tS;
                }
            }
        }
        c.close();
        rdb.close();

        return gpsDataList;
    }

    public static float getConsumedCalorie() {
        return consumedCalorie;
    }

    public static float getDeclineDistance() {
        return declineDistance;
    }

    public static float getInclineDistance() {
        return inclineDistance;
    }

    public static float getMaxAltitude() {
        return maxAltitude;
    }

    public static float getMaxSpeed() {
        return maxSpeed;
    }

    public static float getMinAltitude() {
        return minAltitude;
    }

    public static float getTotalDistance() {
        return totalDistance;
    }

    public static float getAverageSpeed() {
        return averageSpeed;
    }
}
