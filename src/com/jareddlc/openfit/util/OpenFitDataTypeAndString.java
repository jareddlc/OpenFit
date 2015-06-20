package com.jareddlc.openfit.util;

public class OpenFitDataTypeAndString {
    String mData;
    OpenFitDataType mDataType;

    public OpenFitDataTypeAndString(OpenFitDataType pDataType, String pString) {
        this.mDataType = pDataType;
        this.mData = pString;
        if(pString == null) {
            return;
        }
        /*while((!this.mDataType.equals(OpenFitDataType.BYTE)) || (this.mData.length() <= 50)) {
            if((this.mDataType.equals(OpenFitDataType.SHORT)) && (this.mData.length() > 250)) {
                this.mData = this.mData.substring(0, 250);
                return;
            }
        }*/
        //this.mData = this.mData.substring(0, 50);
    }

    public String getData() {
      return this.mData;
    }

    public OpenFitDataType getDataType()  {
      return this.mDataType;
    }

    public int getLength() {
      return this.mData.length();
    }
}
