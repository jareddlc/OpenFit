package com.solderbyte.openfit;

import android.os.Parcel;
import android.os.Parcelable;

public class HeartRateResultRecord implements Parcelable {

    private long timeStamp;
    private int heartRate;

    public HeartRateResultRecord(long t, int h) {
        timeStamp = t;
        heartRate = h;
    }

    public HeartRateResultRecord(Parcel source) {
        timeStamp = source.readLong();
        heartRate = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel source, int flags) {
        source.writeLong(timeStamp);
        source.writeInt(heartRate);
    }

    public static final Creator<HeartRateResultRecord> CREATOR = new Creator<HeartRateResultRecord>() {
        public HeartRateResultRecord createFromParcel(Parcel source) {
            return new HeartRateResultRecord(source);
        }

        @Override
        public HeartRateResultRecord[] newArray(int size) {
            return new HeartRateResultRecord[size];
        }
    };

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getHeartRate() {
        return heartRate;
    }
}
