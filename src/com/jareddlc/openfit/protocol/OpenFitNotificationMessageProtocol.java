package com.jareddlc.openfit.protocol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    public OpenFitNotificationMessageProtocol(long l, String s, String s1, String pTitle, String s3, long pLong) {
        mIndex = l;
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, s));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, s1));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, pTitle));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.SHORT, s3));
        mTime = pLong;
    }

    public void createCMASProtocol() {
        mByteArray = createNotificationProtocol(DATA_TYPE_CMAS, mIndex, mDataList, mTime);
    }

    public void createEASEMailProtocol() {
        mByteArray = createNotificationProtocol(DATA_TYPE_EAS, mIndex, mDataList, mTime);
    }
    
    public byte[] createNotificationProtocol(int i, long l, List list, long l1) {
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        StringBuilder stringbuilder;
        Iterator iterator;
        oDatacomposer.writeByte((byte)i);
        oDatacomposer.writeLong(l);
        stringbuilder = new StringBuilder();
        iterator = list.iterator();
        OpenFitDataTypeAndString oDataString;
        byte oByte[];
        
        //_L2:
        if(!iterator.hasNext()) {
            OpenFitVariableDataComposer.writeTimeInfo(oDatacomposer, l1);
            return oDatacomposer.toByteArray();
        }
        oDataString = (OpenFitDataTypeAndString)iterator.next();
        oByte = OpenFitVariableDataComposer.convertToByteArray(oDataString.getData());
        
        if(oDataString.getDataType() != OpenFitDataType.BYTE) {
           
        }
        oDatacomposer.writeByte((byte)oByte.length);
        //_L4:
        stringbuilder.append(oByte.length).append(" ");
        oDatacomposer.writeBytes(oByte);
        //_L1:
        if(oDataString.getDataType() != OpenFitDataType.SHORT) {
            // got L4
        }
        else {
            oDatacomposer.writeShort((short)oByte.length);
        }
        return oDatacomposer.toByteArray();
    }
    
    /*
     * public void createMessageProtocol(int i)
    {
        DataComposer datacomposer;
        StringBuilder stringbuilder;
        int k;
        Iterator iterator;
        Logger.i(TAG, (new StringBuilder("createMessageProtocol(mmsAttachedFile : ")).append(i >> 1).append(")").toString());
        int j = AbstractNotificationProtocol.EDataType.DATA_TYPE_MESSAGE.ordinal();
        datacomposer = DataComposer.newVariableDataComposer();
        datacomposer.writeByte((byte)j);
        Logger.v(TAG, (new StringBuilder("Noti. Type               : ")).append(j).toString());
        datacomposer.writeLong(mIndex);
        Logger.v(TAG, (new StringBuilder("mIndex                   : ")).append(mIndex).toString());
        stringbuilder = new StringBuilder();
        k = 0;
        iterator = mDataList.iterator();
_L2:
        DataTypeAndString datatypeandstring;
        byte abyte0[];
        if (!iterator.hasNext())
        {
            datacomposer.writeByte((byte)i);
            Logger.v(TAG, (new StringBuilder("Num or Attached files    : ")).append(i).toString());
            DataComposer.writeTimeInfo(datacomposer, mTime);
            Logger.v(TAG, (new StringBuilder("Time                     : ")).append(mTime).toString());
            dataByteArray = datacomposer.toByteArray();
            Logger.v(TAG, (new StringBuilder("logBuilder.toString() : ")).append(stringbuilder.toString()).toString());
            Logger.v(TAG, (new StringBuilder("logBuilder.toByteArray() : ")).append(ByteUnitConverter.bytesToHexString(dataByteArray, dataByteArray.length)).toString());
            return;
        }
        datatypeandstring = (DataTypeAndString)iterator.next();
        abyte0 = DataComposer.convertToByteArray(datatypeandstring.getData());
        if (datatypeandstring.getDataType() != DataType.BYTE)
        {
            break;
        }
        datacomposer.writeByte((byte)abyte0.length);
_L4:
        stringbuilder.append(abyte0.length).append(" ");
        datacomposer.writeBytes(abyte0);
        Logger.v(TAG, (new StringBuilder("mDataList[")).append(k).append("]           : ").append(ByteUnitConverter.bytesToHexString(abyte0, abyte0.length)).toString());
        k++;
        if (true) goto _L2; else goto _L1
_L1:
        if (datatypeandstring.getDataType() != DataType.SHORT) goto _L4; else goto _L3
_L3:
        datacomposer.writeShort((short)abyte0.length);
          goto _L4
    }
     */
    
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
