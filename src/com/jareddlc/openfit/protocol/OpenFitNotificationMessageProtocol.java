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
    private long mIndex = 0;
    private boolean bSupportQuickReply;
    private byte mByteArray[];
    private final List mDataList = new ArrayList();

    private byte DATA_TYPE_RESERVED = 49;
    private byte DATA_TYPE_CMAS = 35;
    private byte DATA_TYPE_EAS = 36;
    private byte DATA_TYPE_EMAIL = 3;
    private byte DATA_TYPE_MESSAGE = 4;

    public OpenFitNotificationMessageProtocol(long index, String senderName, String senderPhone, String msgTitle, String msgData, long time) {
        mIndex = index;
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, senderName));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, senderPhone));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, msgTitle));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.SHORT, msgData));
        mTime = time;
    }

    public void createCMASProtocol() {
        mByteArray = createNotificationProtocol(DATA_TYPE_CMAS, mIndex, mDataList, mTime);
    }

    public void createEASEMailProtocol() {
        mByteArray = createNotificationProtocol(DATA_TYPE_EAS, mIndex, mDataList, mTime);
    }
    
    public void createMessageProtocol(int paramInt) {
        Log.d(LOG_TAG, "new  createMessageProtocol");
        int i = DATA_TYPE_MESSAGE;
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();;
        oDatacomposer.writeByte((byte)i);
        Log.d(LOG_TAG, "Noti. Type               : " + i);
        oDatacomposer.writeLong(this.mIndex);
        Log.d(LOG_TAG, "mIndex               : " + this.mIndex);
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
        oDatacomposer.writeByte((byte)paramInt);
        OpenFitVariableDataComposer.writeTimeInfo(oDatacomposer, this.mTime);
        Log.d(LOG_TAG, "Time                     : " + this.mTime);
        this.mByteArray = oDatacomposer.toByteArray();
    }
    
    public static byte[] createNotificationProtocol(int msgType, long msgSize, List msgData, long timeStamp) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)msgType);       // i should be 4
        oDatacomposer.writeLong(msgSize);             // size of packet
        StringBuilder oStringBuilder = new StringBuilder();
        Iterator oIterator = msgData.iterator();

        while(oIterator.hasNext()) {
            OpenFitDataTypeAndString oDataString = (OpenFitDataTypeAndString)oIterator.next();
            //OpenFitDataTypeAndString oDataString = new OpenFitDataTypeAndString(OpenFitDataType.BYTE, oIterator.next().toString()); //;
            byte[] oByte = OpenFitVariableDataComposer.convertToByteArray(oDataString.getData());
            Log.d(LOG_TAG, "Iterating over:"+ oDataString.getData()+" type: "+oDataString.getDataString());
            
            if(oDataString.getDataType() == OpenFitDataType.BYTE) {
                Log.d(LOG_TAG, "writting byte "+(byte)oByte.length);
                oDatacomposer.writeByte((byte)oByte.length);
            }
            if(oDataString.getDataType() == OpenFitDataType.SHORT) {
                Log.d(LOG_TAG, "writting short "+(short)oByte.length);
                oDatacomposer.writeShort((short)oByte.length);
            }
            oStringBuilder.append(oByte.length).append(" ");
            oDatacomposer.writeBytes(oByte);
            Log.d(LOG_TAG, "wrote bytes  "+oByte);
            
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
