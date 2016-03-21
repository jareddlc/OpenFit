package com.solderbyte.openfit;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.Date;

public class PedometerData implements Parcelable {

    private long timeStamp;
    private int steps;
    private float distance;
    private float calories;
    private long timeStampEnd;

    public PedometerData(long t, int s, float d, float c) {
        timeStamp = t;
        steps = s;
        distance = d;
        calories = c;
        Calendar cal = Calendar.getInstance();
        Date startDate = new Date(timeStamp);
        cal.setTime(startDate);
        cal.add(Calendar.MINUTE, 10);
        Date endDate = cal.getTime();
        timeStampEnd = endDate.getTime();
    }

    public PedometerData(Parcel source) {
        timeStamp = source.readLong();
        steps = source.readInt();
        distance = source.readFloat();
        calories = source.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel source, int flags) {
        source.writeLong(timeStamp);
        source.writeInt(steps);
        source.writeFloat(distance);
        source.writeFloat(calories);
    }

    public static final Parcelable.Creator<PedometerData> CREATOR = new Parcelable.Creator<PedometerData>() {
        public PedometerData createFromParcel(Parcel source) {
            return new PedometerData(source);
        }

        @Override
        public PedometerData[] newArray(int size) {
            return new PedometerData[size];
        }
    };

    public void setTimeStampEnd(long ms) {
        timeStampEnd = ms;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getTimeStampEnd() {
        return timeStampEnd;
    }

    public int getSteps() {
        return steps;
    }

    public float getDistance() {
        return distance;
    }

    public float getCalories() {
        return calories;
    }
}
