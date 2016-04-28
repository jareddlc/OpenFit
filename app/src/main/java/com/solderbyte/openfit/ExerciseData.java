package com.solderbyte.openfit;

import android.os.Parcel;
import android.os.Parcelable;

public class ExerciseData implements Parcelable {

    private long timeStamp;
    private long duration;
    private float calories;
    private int avgHeartRate;
    private float distance;
    private byte fitnessLevel;
    private int exerciseType;
    private float avgSpeed;
    private float maxSpeed;
    private int maxHeartRate;
    private float maxAlt;
    private float minAlt;
    private float inclinedDistance;
    private float declinedDistance;

    public ExerciseData(long t, long d, float c, int aHR, float dist, byte f, int e, float aS, float mS, int mHR, float maxA, float minA, float iD, float dD) {
        timeStamp = t;
        duration = d;
        calories = c;
        avgHeartRate = aHR;
        distance = dist;
        fitnessLevel = f;
        exerciseType = e;
        avgSpeed = aS;
        maxSpeed = mS;
        maxHeartRate = mHR;
        maxAlt = maxA;
        minAlt = minA;
        inclinedDistance = iD;
        declinedDistance = dD;
    }

    public ExerciseData(Parcel source) {
        timeStamp = source.readLong();
        duration = source.readLong();
        calories = source.readFloat();
        avgHeartRate = source.readInt();
        distance = source.readFloat();
        fitnessLevel = source.readByte();
        exerciseType = source.readInt();
        avgSpeed = source.readFloat();
        maxSpeed = source.readFloat();
        maxHeartRate = source.readInt();
        maxAlt = source.readFloat();
        minAlt = source.readFloat();
        inclinedDistance = source.readFloat();
        declinedDistance = source.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel source, int flags) {
        source.writeLong(timeStamp);
        source.writeLong(duration);
        source.writeFloat(calories);
        source.writeInt(avgHeartRate);
        source.writeFloat(distance);
        source.writeByte(fitnessLevel);
        source.writeInt(exerciseType);
        source.writeFloat(avgSpeed);
        source.writeFloat(maxSpeed);
        source.writeInt(maxHeartRate);
        source.writeFloat(maxAlt);
        source.writeFloat(minAlt);
        source.writeFloat(inclinedDistance);
        source.writeFloat(declinedDistance);
    }

    public static final Parcelable.Creator<ExerciseData> CREATOR = new Parcelable.Creator<ExerciseData>() {
        public ExerciseData createFromParcel(Parcel source) {
            return new ExerciseData(source);
        }

        @Override
        public ExerciseData[] newArray(int size) {
            return new ExerciseData[size];
        }
    };

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getDuration() {
        return duration;
    }

    public float getCalories() {
        return calories;
    }

    public int getAvgHeartRate() {
        return avgHeartRate;
    }

    public float getDistance() {
        return distance;
    }

    public byte getFitnessLevel() {
        return fitnessLevel;
    }

    public int getExerciseType() {
        return exerciseType;
    }

    public float getAvgSpeed() {
        return avgSpeed;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public int getMaxHeartRate() {
        return maxHeartRate;
    }

    public long getTimeStampEnd() {
        return timeStamp + duration * 1000L;
    }

    public float getMaxAltitude() {
        return maxAlt;
    }

    public float getMinAltitude() {
        return minAlt;
    }

    public float getInclinedDistance() {
        return inclinedDistance;
    }

    public float getDeclinedDistance() {
        return declinedDistance;
    }
}
