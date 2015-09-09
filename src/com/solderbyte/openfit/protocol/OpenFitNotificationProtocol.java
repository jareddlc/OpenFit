package com.solderbyte.openfit.protocol;

import java.util.Iterator;
import java.util.List;

import com.solderbyte.openfit.util.OpenFitDataType;
import com.solderbyte.openfit.util.OpenFitDataTypeAndString;
import com.solderbyte.openfit.util.OpenFitVariableDataComposer;

public class OpenFitNotificationProtocol {

    public static Boolean SUPPORT_QUICK_REPLY = false;
    public static Boolean SHOW_ON_DEVICE = false;
    public static Boolean HAS_IMAGE = false;
    public static Boolean INCOMING_CALL_FLAG = true;

    public byte[] createMessageProtocol(int msgType, long msgId, List<OpenFitDataTypeAndString> msgData, long timeStamp) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();;
        oDatacomposer.writeByte((byte)msgType);
        oDatacomposer.writeLong(msgId);
        StringBuilder oStringBuilder = new StringBuilder();

        Iterator<OpenFitDataTypeAndString> oIterator = msgData.iterator();
        while(oIterator.hasNext()) {
            OpenFitDataTypeAndString localDataTypeAndString = (OpenFitDataTypeAndString)oIterator.next();
            byte[] oByte = OpenFitVariableDataComposer.convertToByteArray(localDataTypeAndString.getData());
            
            if(localDataTypeAndString.getDataType() == OpenFitDataType.BYTE) {
                oDatacomposer.writeByte((byte)oByte.length);
            }
            if(localDataTypeAndString.getDataType() == OpenFitDataType.SHORT) {
                oDatacomposer.writeShort((short)oByte.length);
            }
            oStringBuilder.append(oByte.length).append(" ");
            oDatacomposer.writeBytes(oByte);
        }
        oDatacomposer.writeBoolean(SHOW_ON_DEVICE);
        oDatacomposer.writeByte((byte)0);
        OpenFitVariableDataComposer.writeTimeInfo(oDatacomposer, timeStamp);
        return oDatacomposer.toByteArray();
    }

    public static byte[] createNotificationProtocol(int msgType, long msgId, List<OpenFitDataTypeAndString> msgData, long timeStamp) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)msgType);
        oDatacomposer.writeLong(msgId);
        StringBuilder oStringBuilder = new StringBuilder();
        Iterator<OpenFitDataTypeAndString> oIterator = msgData.iterator();

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

    public static byte[] createEmailProtocol(int msgType, long msgId, List<OpenFitDataTypeAndString> msgData, long timeStamp) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)msgType);
        oDatacomposer.writeLong(msgId);
        StringBuilder oStringBuilder = new StringBuilder();
        Iterator<OpenFitDataTypeAndString> oIterator = msgData.iterator();

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
        // number of attached files num << 1
        oDatacomposer.writeByte((byte)0);
        oDatacomposer.writeBoolean(SUPPORT_QUICK_REPLY);
        oDatacomposer.writeBoolean(HAS_IMAGE);
        OpenFitVariableDataComposer.writeTimeInfo(oDatacomposer, timeStamp);
        return oDatacomposer.toByteArray();
    }

    public static byte[] createIncomingCallProtocol(int msgType, long msgId, List<OpenFitDataTypeAndString> msgData, long timeStamp) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)msgType);
        oDatacomposer.writeLong(msgId);
        if(INCOMING_CALL_FLAG) {
            oDatacomposer.writeByte((byte)0);
        }
        else {
            oDatacomposer.writeByte((byte)1);
        }
        StringBuilder oStringBuilder = new StringBuilder();
        Iterator<OpenFitDataTypeAndString> oIterator = msgData.iterator();

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

        OpenFitVariableDataComposer.writeTimeInfo(oDatacomposer, timeStamp);
        return oDatacomposer.toByteArray();
    }

    public static byte[] createMediaTrackProtocol(int msgType, String msgData) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)msgType);
        OpenFitVariableDataComposer.writeStringWithOneByteLength(oDatacomposer, msgData);

        return oDatacomposer.toByteArray();
    }

    public static byte[] createAlarmProtocol(int msgType, long msgId, List<OpenFitDataTypeAndString> msgData, long timeStamp) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)msgType);
        oDatacomposer.writeLong(msgId);
        StringBuilder oStringBuilder = new StringBuilder();
        Iterator<OpenFitDataTypeAndString> oIterator = msgData.iterator();

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
        //OpenFitVariableDataComposer.writeTimeInfo(oDatacomposer, timeStamp);

        oDatacomposer.writeInt((int) timeStamp);
        // snooze or dissmiss
        oDatacomposer.writeInt(0);
        return oDatacomposer.toByteArray();
    }
    
    public static byte[] createWeatherProtocol(int msgType, long msgId, String msgData, int icon, long timeStamp) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)msgType);
        oDatacomposer.writeLong(msgId);

        byte[] oByte = OpenFitVariableDataComposer.convertToByteArray(msgData);

        oDatacomposer.writeByte((byte)oByte.length);
        oDatacomposer.writeBytes(oByte);
        oDatacomposer.writeInt(icon);
        OpenFitVariableDataComposer.writeTimeInfo(oDatacomposer, timeStamp);
        return oDatacomposer.toByteArray();
    }
}
