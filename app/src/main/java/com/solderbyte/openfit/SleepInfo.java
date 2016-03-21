package com.solderbyte.openfit;

import android.os.Parcel;
import android.os.Parcelable;

public class SleepInfo implements Parcelable {

    private int index;
    private long timeStamp;
    private int status;

    public SleepInfo(int i, long t, int s) {
        index = i;
        timeStamp = t;
        status = s;
    }

    public SleepInfo(Parcel source) {
        index = source.readInt();
        timeStamp = source.readLong();
        status = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel source, int flags) {
        source.writeInt(index);
        source.writeLong(timeStamp);
        source.writeInt(status);
    }

    public static final Creator<SleepInfo> CREATOR = new Creator<SleepInfo>() {
        public SleepInfo createFromParcel(Parcel source) {
            return new SleepInfo(source);
        }

        @Override
        public SleepInfo[] newArray(int size) {
            return new SleepInfo[size];
        }
    };

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getIndex() {
        return index;
    }

    public int getStatus() {
        return status;
    }
}
