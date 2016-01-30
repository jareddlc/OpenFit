package com.solderbyte.openfit;

import android.os.Parcel;
import android.os.Parcelable;

public class PedometerTotal implements Parcelable {

    private int steps;
    private float distance;
    private float calories;

    public PedometerTotal(int s, float d, float c) {
        steps = s;
        distance = d;
        calories = c;
    }

    public PedometerTotal(Parcel source) {
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
        source.writeInt(steps);
        source.writeFloat(distance);
        source.writeFloat(calories);
    }

    public static final Parcelable.Creator<PedometerTotal> CREATOR = new Parcelable.Creator<PedometerTotal>() {
        public PedometerTotal createFromParcel(Parcel source) {
            return new PedometerTotal(source);
        }

        @Override
        public PedometerTotal[] newArray(int size) {
            return new PedometerTotal[size];
        }
    };

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
