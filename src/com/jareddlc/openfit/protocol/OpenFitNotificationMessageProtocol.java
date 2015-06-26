package com.jareddlc.openfit.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jareddlc.openfit.OpenFitApi;
import com.jareddlc.openfit.util.OpenFitDataType;
import com.jareddlc.openfit.util.OpenFitDataTypeAndString;
import com.jareddlc.openfit.util.OpenFitVariableDataComposer;

import android.util.Log;

public class OpenFitNotificationMessageProtocol {
    private static final String LOG_TAG = "OpenFit:OpenFitNotificationMessageProtocol";
    private long mTime = 0;
    private long mMsgId = 0;
    private boolean bSupportQuickReply;
    private byte mByteArray[];
    private final List mDataList = new ArrayList();

    public static byte DATA_TYPE_INCOMING_CALL = 0;
    public static byte DATA_TYPE_MISSCALL = 1;
    public static byte DATA_TYPE_EMAIL = 3;
    public static byte DATA_TYPE_MESSAGE = 4;
    public static byte DATA_TYPE_ALARM = 5;
    public static byte DATA_TYPE_WEATHER = 7;
    public static byte DATA_TYPE_CHATON = 10;
    public static byte DATA_TYPE_GENERAL= 12;
    public static byte DATA_TYPE_REJECT_ACTION = 13;
    public static byte DATA_TYPE_ALARM_ACTION = 14;
    public static byte DATA_TYPE_SMART_RELAY_REQUEST = 17;
    public static byte DATA_TYPE_SMART_RELAY_RESPONSE = 18;
    public static byte DATA_TYPE_IMAGE = 33;
    public static byte DATA_TYPE_CMAS = 35;
    public static byte DATA_TYPE_EAS = 36;
    public static byte DATA_TYPE_RESERVED = 49;

    public OpenFitNotificationMessageProtocol(long msgId, String senderName, String senderPhone, String msgTitle, String msgData, long time) {
        mMsgId = msgId;
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, senderName));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, senderPhone));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, msgTitle));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.SHORT, msgData));
        mTime = time;
    }

    public void createCMASProtocol() {
        mByteArray = createNotificationProtocol(DATA_TYPE_CMAS, mMsgId, mDataList, mTime);
    }

    public void createEASEMailProtocol() {
        mByteArray = createNotificationProtocol(DATA_TYPE_EAS, mMsgId, mDataList, mTime);
    }
    
    public void createMessageProtocol(int paramInt) {
        Log.d(LOG_TAG, "new  createMessageProtocol");
        int i = DATA_TYPE_MESSAGE;
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();;
        oDatacomposer.writeByte((byte)i);
        oDatacomposer.writeLong(this.mMsgId);
        StringBuilder oStringBuilder = new StringBuilder();
        i = 0;
        Iterator oIterator = this.mDataList.iterator();
        while(oIterator.hasNext()) {
            OpenFitDataTypeAndString localDataTypeAndString = (OpenFitDataTypeAndString)oIterator.next();
            byte[] oByte = OpenFitVariableDataComposer.convertToByteArray(localDataTypeAndString.getData());
            if(localDataTypeAndString.getDataType() == OpenFitDataType.BYTE) {
                oDatacomposer.writeByte((byte)oByte.length);
            }

            oStringBuilder.append(oByte.length).append(" ");
            oDatacomposer.writeBytes(oByte);
            Log.d(LOG_TAG, "mDataList[" + i + "]           : " + OpenFitApi.byteArrayToHexString(oByte));
            i += 1;

            if(localDataTypeAndString.getDataType() == OpenFitDataType.SHORT) {
                oDatacomposer.writeShort((short)oByte.length);
            }
        }
        //oDataComposer.writeBoolean(this.bShowDeviceOnDevice);
        oDatacomposer.writeByte((byte)paramInt);
        OpenFitVariableDataComposer.writeTimeInfo(oDatacomposer, this.mTime);
        this.mByteArray = oDatacomposer.toByteArray();
    }
    
    public static byte[] createNotificationProtocol(int msgType, long msgId, List msgData, long timeStamp) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)msgType);
        oDatacomposer.writeLong(msgId);
        StringBuilder oStringBuilder = new StringBuilder();
        Iterator oIterator = msgData.iterator();

        while(oIterator.hasNext()) {
            OpenFitDataTypeAndString oDataString = (OpenFitDataTypeAndString)oIterator.next();
            byte[] oByte = OpenFitVariableDataComposer.convertToByteArray(oDataString.getData());

            if(oDataString.getDataType() == OpenFitDataType.BYTE) {
                oDatacomposer.writeByte((byte)oByte.length);
            }
            if(oDataString.getDataType() == OpenFitDataType.SHORT) {
                oDatacomposer.writeShort((short)oByte.length);
            }
            oStringBuilder.append(oByte.length).append(" ");
            oDatacomposer.writeBytes(oByte);
        }
        oDatacomposer.writeByte((byte)0);
        OpenFitVariableDataComposer.writeTimeInfo(oDatacomposer, timeStamp);
        return oDatacomposer.toByteArray();
    }

    public byte[] getByteArray() {
        if(mByteArray != null) {
            return mByteArray;
        }
        else {
            byte oByte[] = new byte[1];
            oByte[0] = (byte)DATA_TYPE_RESERVED;
            return oByte;
        }
    }

    public String getSenderNumber() {
        if(mDataList != null && mDataList.size() > 2) {
            mDataList.get(1);
        }
        return null;
    }
}
