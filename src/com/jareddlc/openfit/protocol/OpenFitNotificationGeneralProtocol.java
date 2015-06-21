package com.jareddlc.openfit.protocol;

import android.util.Log;
import com.jareddlc.openfit.util.OpenFitDataTypeOld;
import com.jareddlc.openfit.util.OpenFitDataTypeAndString;
import com.jareddlc.openfit.util.OpenFitVariableDataComposer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OpenFitNotificationGeneralProtocol {
    private static final String LOG_TAG = "OpenFit:OpenFitNotificationGeneralProtocol";
    private final boolean bShowDeviceOnDevice;
    private byte[] dataByteArray;
    private final List<OpenFitDataTypeAndString> mDataList;
    private final long mTime;
    private long mIndex;

    public OpenFitNotificationGeneralProtocol(long msgID, String appName, String AppLabel, String uStr1, String eString1, String eString2, String pString6, boolean pShowDeviceOnDevice, long msgTime) {
        this.mIndex = msgID;
        this.mDataList = new ArrayList();
        Log.d(LOG_TAG, "about to add data: " + msgID);
        this.mDataList.add(new OpenFitDataTypeAndString(OpenFitDataTypeOld.BYTE, appName));
        this.mDataList.add(new OpenFitDataTypeAndString(OpenFitDataTypeOld.BYTE, AppLabel));
        this.mDataList.add(new OpenFitDataTypeAndString(OpenFitDataTypeOld.BYTE, uStr1));
        this.mDataList.add(new OpenFitDataTypeAndString(OpenFitDataTypeOld.BYTE, eString1));
        this.mDataList.add(new OpenFitDataTypeAndString(OpenFitDataTypeOld.BYTE, eString2));
        this.mDataList.add(new OpenFitDataTypeAndString(OpenFitDataTypeOld.SHORT, pString6));
        this.bShowDeviceOnDevice = pShowDeviceOnDevice;
        this.mTime = msgTime;
    }

    public void createGeneralProtocol() {
        int i = OpenFitDataTypeOld.DATA_TYPE_GENERAL.i;
        Log.d(LOG_TAG, "OpenFitDataType.DATA_TYPE_GENERAL.i: " + i);
        OpenFitVariableDataComposer oDataComposer = new OpenFitVariableDataComposer();
        oDataComposer.writeByte((byte)i);
        oDataComposer.writeLong(this.mIndex);
        StringBuilder localStringBuilder = new StringBuilder();
        Iterator localIterator = this.mDataList.iterator();
        if(localIterator.hasNext()) {
            OpenFitDataTypeAndString oDataTypeAndString = (OpenFitDataTypeAndString)localIterator.next();
            byte[] arrayOfByte = OpenFitVariableDataComposer.convertToByteArray(oDataTypeAndString.getData());
            if(oDataTypeAndString.getDataType() == OpenFitDataTypeOld.BYTE) {
                oDataComposer.writeByte((byte)arrayOfByte.length);
            }
            for(;;) {
                localStringBuilder.append(arrayOfByte.length).append(" ");
                oDataComposer.writeBytes(arrayOfByte);
                if(oDataTypeAndString.getDataType() == OpenFitDataTypeOld.SHORT) {
                    oDataComposer.writeShort((short)arrayOfByte.length);
                    break;
                } 
            }
        }
        Log.i(LOG_TAG, " isShowOn : " + this.bShowDeviceOnDevice);
        oDataComposer.writeBoolean(this.bShowDeviceOnDevice);
        OpenFitVariableDataComposer.writeTimeInfo(oDataComposer, this.mTime);
        this.dataByteArray = oDataComposer.toByteArray();
    }

    public byte[] getByteArray() {
        if(this.dataByteArray != null) {
            return this.dataByteArray;
        }
        return new byte[] {(byte)OpenFitDataTypeOld.DATA_TYPE_RESERVED.i};
    }
}
