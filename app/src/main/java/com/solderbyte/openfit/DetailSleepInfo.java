package com.solderbyte.openfit;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailSleepInfo implements Parcelable {

    private int index;
    private long timeStamp;
    private int status;

    public DetailSleepInfo(int i, long t, int s) {
        index = i;
        timeStamp = t;
        status = s;
    }

    public DetailSleepInfo(Parcel source) {
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

    public static final Creator<DetailSleepInfo> CREATOR = new Creator<DetailSleepInfo>() {
        public DetailSleepInfo createFromParcel(Parcel source) {
            return new DetailSleepInfo(source);
        }

        @Override
        public DetailSleepInfo[] newArray(int size) {
            return new DetailSleepInfo[size];
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
