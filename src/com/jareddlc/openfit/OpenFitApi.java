package com.jareddlc.openfit;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.TimeZone;

import com.jareddlc.openfit.protocol.OpenFitNotificationGeneralProtocol;
import com.jareddlc.openfit.util.OpenFitDataComposer;
import com.jareddlc.openfit.util.OpenFitTimeZoneUtil;
import com.jareddlc.openfit.util.OpenFitVariableDataComposer;

import android.util.Log;

public class OpenFitApi {
    private static final String LOG_TAG = "OpenFit:OpenFitApi";

    public static final byte TEXT_DATE_FORMAT_TYPE = 0; // 0,1,2
    public static final byte NUMBER_DATE_FORMAT_TYPE = 0; // 0,1,2
    public static final boolean IS_TIME_DISPLAY_24 = false; // 0,1

    public static final String REQUEST_1 = "000400000003000000";
    public static final byte[] REQ_1 = OpenFitApi.hexStringToByteArray(REQUEST_1);

    public static boolean sentFota = false;
    
    public static byte[] getOdin() {
        String hex = "4d040000004f44494e";
        byte[] b = OpenFitApi.hexStringToByteArray(hex);
        return b;
        /*OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)77);
        oVariableDataComposer.writeByte((byte)04);
        oVariableDataComposer.writeByte((byte)0);
        oVariableDataComposer.writeByte((byte)0);
        oVariableDataComposer.writeByte((byte)0);
        oVariableDataComposer.writeByte((byte)79);
        oVariableDataComposer.writeByte((byte)68);
        oVariableDataComposer.writeByte((byte)73);
        oVariableDataComposer.writeByte((byte)78);
        return oVariableDataComposer.toByteArray();*/
    }
    
    public static byte[] afterOdin() {
        String hex = "64080000000402010104020501";
        byte[] b = OpenFitApi.hexStringToByteArray(hex);
        return b;
    }
    
    public static byte[] preTime() {
        String hex = "4e020000000101";
        byte[] b = OpenFitApi.hexStringToByteArray(hex);
        return b;
    }
    
    public static byte[] getTime() {
        String hex = "011e0000000141cb3555f8ffffff000000000101010201a01dfc5490d43556100e0000";
        byte[] b = OpenFitApi.hexStringToByteArray(hex);
        return b;
    }

    public static byte[] getFotaCommand(ByteBuffer paramByteBuffer) {
        return OpenFitApi.buildDataComposer((byte)78, paramByteBuffer.array());
    }

    public static byte[] getFotaData(ByteBuffer paramByteBuffer) {
        return OpenFitApi.buildDataComposer((byte)77, paramByteBuffer.array());
    }

    public static byte[] getRequestRSSI(ByteBuffer paramByteBuffer) {
        return OpenFitApi.buildDataComposer((byte)44, paramByteBuffer.array());
    }

    public static byte[] buildDataComposer(byte paramByte, byte[] paramArrayOfByte) {
        OpenFitDataComposer oDataComposer = new OpenFitDataComposer(paramArrayOfByte.length + 5);
        oDataComposer.writeByte(paramByte);
        oDataComposer.writeInt(paramArrayOfByte.length);
        oDataComposer.writeBytes(paramArrayOfByte);
        return oDataComposer.toByteArray();
    }

    public static ByteBuffer getByteBuffer() {
        Object oObject = ByteBuffer.allocate(2);
        ((ByteBuffer)oObject).put((byte)1);
        ((ByteBuffer)oObject).put((byte)1);
        return (ByteBuffer)oObject;
    }

    public static byte[] getFotaCommandByte() {
        Log.d(LOG_TAG, "getFotaCommand");
        Object localObject = ByteBuffer.allocate(2);
        ((ByteBuffer)localObject).put((byte)1);
        ((ByteBuffer)localObject).put((byte)1);
        ByteBuffer oByteBuffer = (ByteBuffer)localObject;
        byte[] oArrayOfByte = oByteBuffer.array();
        byte oFota = (byte)78;

        OpenFitDataComposer oDataComposer = new OpenFitDataComposer(oArrayOfByte.length + 5);
        oDataComposer.writeByte(oFota);
        oDataComposer.writeInt(oArrayOfByte.length);
        oDataComposer.writeBytes(oArrayOfByte);
        Log.d(LOG_TAG, "FotaCommand bytes: "+ oDataComposer);
        Log.d(LOG_TAG, "FotaCommand bytes: "+ oDataComposer.toByteArray());
        return oDataComposer.toByteArray();
    }

    public static byte[] getCurrentTimeInfo() {
        Object localObject = Calendar.getInstance();
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeInt((int)(System.currentTimeMillis() / 1000L));
        TimeZone localTimeZone = ((Calendar)localObject).getTimeZone();
        long l = ((Calendar)localObject).getTimeInMillis();
        //Log.d(LOG_TAG, String.format("current time : %d", new Object[] { Long.valueOf(System.currentTimeMillis()) }));
        int i = localTimeZone.getRawOffset() / 60000;
        //Log.d(LOG_TAG, String.format("offset : %d mins", new Object[] { Integer.valueOf(i) }));
        int j = i / 60;
        //Log.d(LOG_TAG, "text Date format type : " + TEXT_DATE_FORMAT_TYPE);
        //Log.d(LOG_TAG, "number Date format type : " + NUMBER_DATE_FORMAT_TYPE);
        int k = (int)(OpenFitTimeZoneUtil.prevTransition(localTimeZone, l) / 1000L);
        int m = (int)(OpenFitTimeZoneUtil.nextTransition(localTimeZone, l) / 1000L);
        //Log.d(LOG_TAG, "prev transition : " + k);
        //Log.d(LOG_TAG, "next transition : " + m);
        oVariableDataComposer.writeInt(j);
        oVariableDataComposer.writeInt(i % 60);
        oVariableDataComposer.writeByte(TEXT_DATE_FORMAT_TYPE);
        oVariableDataComposer.writeBoolean(IS_TIME_DISPLAY_24);
        oVariableDataComposer.writeBoolean(localTimeZone.inDaylightTime(((Calendar)localObject).getTime()));
        oVariableDataComposer.writeByte(NUMBER_DATE_FORMAT_TYPE);
        oVariableDataComposer.writeBoolean(localTimeZone.useDaylightTime());
        oVariableDataComposer.writeInt(k);
        oVariableDataComposer.writeInt(m);
        oVariableDataComposer.writeInt(localTimeZone.getDSTSavings() / 1000);
        Log.d(LOG_TAG, "CurrentTimeInfo bytes: "+ oVariableDataComposer);
        return oVariableDataComposer.toByteArray();
        /*this.mDataSender.send(WingtipApp.HOME, localDataComposer.toByteArray());
        i = AbstractNotificationProtocol.EDataType.DATA_TYPE_TIME_FORMAT.ordinal();
        localObject = DataComposer.newVariableDataComposer();
        ((DataComposer)localObject).writeByte((byte)i);
        if (this.mIsTimeDisplay24) {}
        for (i = 1;; i = 0)
        {
          ((DataComposer)localObject).writeByte((byte)i);
          localObject = ((DataComposer)localObject).toByteArray();
          this.mDataSender.send(WingtipApp.NOTIFICATION, (byte[])localObject);
          Log.i(TAG, "Send to Wingtip(Notification) : DATA_TYPE_TIME_FORMAT : " + this.mIsTimeDisplay24);
          this.mAlarmManager.cancel(this.mTimeSyncIntent);
          this.mAlarmManager.set(0, System.currentTimeMillis() + 3600000L, this.mTimeSyncIntent);
          return;
        }*/
    }
    
    public static byte[] getNotification() {
        Log.d(LOG_TAG, "new  OpenFitNotificationGeneralProtocol");
        OpenFitNotificationGeneralProtocol oNotification = new OpenFitNotificationGeneralProtocol(100L + 6, "com.jareddlc.openfit", "OpenFit Label", "unknown", "", "", "unknown 2", true, 0);
        Log.d(LOG_TAG, "new  createGeneralProtocol()");
        oNotification.createGeneralProtocol();
        Log.d(LOG_TAG, "getNotification bytes: "+ oNotification);
        return oNotification.getByteArray();
    }

    /*public static byte[] hexStringToDataComposer(String s) {
        int length = s.length();
    }*/

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String hexStringToString(String hex){
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        for(int i=0; i<hex.length()-1; i+=2 ) {
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static int[] byteArrayToIntArray(byte[] bArray) {
      int[] iarray = new int[bArray.length];
      int i = 0;
      for(byte b : bArray) {
          iarray[i++] = b & 0xff;
      }
      return iarray;
    }
}


