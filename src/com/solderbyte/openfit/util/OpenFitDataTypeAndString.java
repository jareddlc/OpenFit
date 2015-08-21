package com.solderbyte.openfit.util;

public class OpenFitDataTypeAndString {
    String mData;
    OpenFitDataType mDataType;

    public OpenFitDataTypeAndString(OpenFitDataType pType, String s) {
        mDataType = pType;
        if(s != null) {
            mData = s;
            if(mDataType.equals(OpenFitDataType.SHORT) && mData.length() > 250) {
                mData = mData.substring(0, 250);
                return;
            }
            if(mDataType.equals(OpenFitDataType.BYTE) && mData.length() > 50) {
                mData = mData.substring(0, 50);
                return;
            }
        }
    }

    public String getData() {
        return mData;
    }

    public String getDataString() {
        return mDataType.STRING;
    }

    public OpenFitDataType getDataType() {
        return mDataType;
    }

    public int getDataIndex() {
        return mDataType.INTEGER;
    }

    public int getLength() {
        return mData.length();
    }
}
