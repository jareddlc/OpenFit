package com.solderbyte.openfit;

import android.os.Parcel;
import android.os.Parcelable;

public class HeartRateData implements Parcelable {

    private long timeStamp;
    private int heartRate;

    public HeartRateData(long t, int h) {
        timeStamp = t;
        heartRate = h;
    }

    public HeartRateData(Parcel source) {
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

    public static final Creator<HeartRateData> CREATOR = new Creator<HeartRateData>() {
        public HeartRateData createFromParcel(Parcel source) {
            return new HeartRateData(source);
        }

        @Override
        public HeartRateData[] newArray(int size) {
            return new HeartRateData[size];
        }
    };

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getHeartRate() {
        return heartRate;
    }
}
