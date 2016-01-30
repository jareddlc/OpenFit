package com.solderbyte.openfit;

import android.os.Parcel;
import android.os.Parcelable;

public class ProfileData implements Parcelable {

    private long timeStamp;
    private int age;
    private float height;
    private float weight;
    private int gender;
    private int birthday;
    private int heightUnit;
    private int weightUnit;
    private int distanceUnit;
    private int activity;
    
    public ProfileData(long t, int a, float h, float w, int g, int b, int hu, int wu, int du, int ac) {
        timeStamp = t;
        age = a;
        height = h;
        weight = w;
        gender = g;
        birthday = b;
        heightUnit = hu;
        weightUnit = wu;
        distanceUnit = du;
        activity = ac;
    }

    public ProfileData(Parcel source) {
        timeStamp = source.readLong();
        age = source.readInt();
        height = source.readFloat();
        weight = source.readFloat();
        gender = source.readInt();
        birthday = source.readInt();
        heightUnit = source.readInt();
        weightUnit = source.readInt();
        distanceUnit = source.readInt();
        activity = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel source, int flags) {
        source.writeLong(timeStamp);
        source.writeInt(age);
        source.writeFloat(height);
        source.writeFloat(weight);
        source.writeInt(gender);
        source.writeInt(birthday);
        source.writeInt(heightUnit);
        source.writeInt(weightUnit);
        source.writeInt(distanceUnit);
        source.writeInt(activity);
    }

    public static final Parcelable.Creator<ProfileData> CREATOR = new Parcelable.Creator<ProfileData>() {
        public ProfileData createFromParcel(Parcel source) {
            return new ProfileData(source);
        }

        @Override
        public ProfileData[] newArray(int size) {
            return new ProfileData[size];
        }
    };

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getAge() {
        return age;
    }

    public float getHeight() {
        return height;
    }

    public float getWeight() {
        return weight;
    }

    public int getGender() {
        return gender;
    }

    public int getBirthday() {
        return birthday;
    }

    public int getHeightUnit() {
        return heightUnit;
    }

    public int getWeightUnit() {
        return weightUnit;
    }

    public int getDistanceUnit() {
        return distanceUnit;
    }

    public int getActivity() {
        return activity;
    }
}
