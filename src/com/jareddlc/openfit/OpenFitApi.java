package com.jareddlc.openfit;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.TimeZone;

import android.util.Log;

public class OpenFitApi {
    private static final String LOG_TAG = "OpenFit:OpenFitApi";

    public static final byte TEXT_DATE_FORMAT_TYPE = 0; // 0,1,2
    public static final byte NUMBER_DATE_FORMAT_TYPE = 0; // 0,1,2
    public static final boolean IS_TIME_DISPLAY_24 = false; // 0,1

    public static boolean sentFota = false;

    public static byte[] getFotaCommand() {
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
        return oDataComposer.toByteArray();
    }

    public static byte[] getCurrentTimeInfo() {
        Object localObject = Calendar.getInstance();
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeInt((int)(System.currentTimeMillis() / 1000L));
        TimeZone localTimeZone = ((Calendar)localObject).getTimeZone();
        long l = ((Calendar)localObject).getTimeInMillis();
        Log.d(LOG_TAG, String.format("current time : %d", new Object[] { Long.valueOf(System.currentTimeMillis()) }));
        int i = localTimeZone.getRawOffset() / 60000;
        Log.d(LOG_TAG, String.format("offset : %d mins", new Object[] { Integer.valueOf(i) }));
        int j = i / 60;
        Log.d(LOG_TAG, "text Date format type : " + TEXT_DATE_FORMAT_TYPE);
        Log.d(LOG_TAG, "number Date format type : " + NUMBER_DATE_FORMAT_TYPE);
        int k = (int)(OpenFitTimeZoneUtil.prevTransition(localTimeZone, l) / 1000L);
        int m = (int)(OpenFitTimeZoneUtil.nextTransition(localTimeZone, l) / 1000L);
        Log.d(LOG_TAG, "prev transition : " + k);
        Log.d(LOG_TAG, "next transition : " + m);
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

    // Functions below are not being used, but exist for reference
    public static void sendFotaCommand(ByteBuffer paramByteBuffer) {
        OpenFitApi.send((byte)78, paramByteBuffer.array());
    }

    public static void sendFotaData(ByteBuffer paramByteBuffer) {
        OpenFitApi.send((byte)77, paramByteBuffer.array());
        OpenFitApi.sentFota = true;
    }

    public static void send(byte paramByte, byte[] paramArrayOfByte) {
        OpenFitDataComposer oDataComposer = new OpenFitDataComposer(paramArrayOfByte.length + 5);
        oDataComposer.writeByte(paramByte);
        oDataComposer.writeInt(paramArrayOfByte.length);
        oDataComposer.writeBytes(paramArrayOfByte);
        Log.d(LOG_TAG, "Not sending bytes: "+ oDataComposer);
    }
    
    public static void onConnection() {
        Log.d(LOG_TAG, "onConnection");
        Object localObject = ByteBuffer.allocate(2);
        ((ByteBuffer)localObject).put((byte)1);
        ((ByteBuffer)localObject).put((byte)1);
        OpenFitApi.sendFotaCommand((ByteBuffer)localObject);
    }
}


