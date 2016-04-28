package com.solderbyte.openfit;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class ExerciseDatabase extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "exerciseInfo";

    public static final String EXERCISE_TABLE = "exercise";
    public static final String EXERCISE_COLS = "( id INTEGER PRIMARY KEY AUTOINCREMENT, timestampStart INTEGER, timestampEnd INTEGER, type INTEGER )";
    public static final String DATA_TABLE = "gpsData";
    public static final String DATA_COLS = "( id INTEGER PRIMARY KEY AUTOINCREMENT, exerciseId INTEGER, lon FLOAT, lat FLOAT, altitude FLOAT, totalDistance FLOAT, speed FLOAT, timestamp INTEGER, FOREIGN KEY (exerciseId) REFERENCES exercise(id) )";
    public static final String PROFILE_TABLE = "profile";
    public static final String PROFILE_COLS = "( id INTEGER, height FLOAT, weight FLOAT )";

    public ExerciseDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + EXERCISE_TABLE + " " + EXERCISE_COLS);
        db.execSQL("CREATE TABLE " + DATA_TABLE + " " + DATA_COLS);
        db.execSQL("CREATE TABLE " + PROFILE_TABLE + " " + PROFILE_COLS);
        db.execSQL("INSERT INTO " + PROFILE_TABLE + " (id, height, weight) values (1, 0, 0)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATA_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + EXERCISE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PROFILE_TABLE);
        onCreate(db);
    }
}

