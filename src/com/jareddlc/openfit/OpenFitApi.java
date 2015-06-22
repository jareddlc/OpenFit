package com.jareddlc.openfit;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.jareddlc.openfit.util.OpenFitData;
import com.jareddlc.openfit.protocol.OpenFitNotificationGeneralProtocol;
import com.jareddlc.openfit.util.OpenFitTimeZoneUtil;
import com.jareddlc.openfit.util.OpenFitVariableDataComposer;

import android.util.Log;

public class OpenFitApi {
    private static final String LOG_TAG = "OpenFit:OpenFitApi";

    public static byte[] getReady() {
        //000400000003000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)0);
        oVariableDataComposer.writeInt(OpenFitData.SIZE_OF_INT);
        oVariableDataComposer.writeInt(3);
        return oVariableDataComposer.toByteArray();
    }
    public static byte[] getUpdate() {
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte(OpenFitData.PORT_FOTA);
        oVariableDataComposer.writeInt(OpenFitData.SIZE_OF_INT);
        oVariableDataComposer.writeBytes("ODIN".getBytes());
        //oVariableDataComposer.writeByte((byte)79); // O
        //oVariableDataComposer.writeByte((byte)68); // D
        //oVariableDataComposer.writeByte((byte)73); // I
        //oVariableDataComposer.writeByte((byte)78); // N
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getUpdateFollowUp() {
        //640800000004020501
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte(OpenFitData.OPENFIT_DATA); // 100
        oVariableDataComposer.writeInt(OpenFitData.SIZE_OF_DOUBLE);
        oVariableDataComposer.writeByte((byte)4);
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeByte((byte)4);
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeByte((byte)5);
        oVariableDataComposer.writeByte((byte)1);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getFotaCommand() {
        //4E020000000101
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte(OpenFitData.PORT_FOTA_COMMAND);
        oVariableDataComposer.writeInt(OpenFitData.SIZE_OF_SHORT);
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeByte((byte)1);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getCurrentTimeInfo() {
        //011E0000000141CB3555F8FFFFFF000000000101010201A01DFC5490D43556100E0000
        // build time data
        int millis = (int)(System.currentTimeMillis() / 1000L);
        Calendar oCalendar = Calendar.getInstance();
        TimeZone oTimeZone = oCalendar.getTimeZone();
        int i = oTimeZone.getRawOffset() / 60000;
        int j = i / 60;
        int k = i % 60;
        Date oDate = oCalendar.getTime();
        boolean inDaylightTime = oTimeZone.inDaylightTime(oDate);
        boolean useDaylightTime = oTimeZone.useDaylightTime();
        long l = oCalendar.getTimeInMillis();
        int m = (int)(OpenFitTimeZoneUtil.prevTransition(oTimeZone, l) / 1000L);
        int n = (int)(OpenFitTimeZoneUtil.nextTransition(oTimeZone, l) / 1000L);
        int dst = oTimeZone.getDSTSavings() / 1000;

        // write time data
        OpenFitVariableDataComposer oVDC = new OpenFitVariableDataComposer();
        oVDC.writeByte((byte)1);
        oVDC.writeInt(millis);
        oVDC.writeInt(j);
        oVDC.writeInt(k);
        oVDC.writeByte(OpenFitData.TEXT_DATE_FORMAT_TYPE);
        oVDC.writeBoolean(OpenFitData.IS_TIME_DISPLAY_24);
        oVDC.writeBoolean(inDaylightTime);
        oVDC.writeByte(OpenFitData.NUMBER_DATE_FORMAT_TYPE);
        oVDC.writeBoolean(useDaylightTime);
        oVDC.writeInt(m);
        oVDC.writeInt(n);
        oVDC.writeInt(dst);
        int length = oVDC.toByteArray().length;

        // write time byte array
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeInt(length);
        oVariableDataComposer.writeBytes(oVDC.toByteArray());
        return oVariableDataComposer.toByteArray();
        //01
        //1e000000
        //01
        //41cb3555
        //f8ffffff
        //00000000
        //01
        //01
        //01
        //02
        //01
        //a01dfc54
        //90d43556
        //100e0000
    }

    public static byte[] getNotification() {
        Log.d(LOG_TAG, "new  OpenFitNotificationGeneralProtocol");
        // build message data
        OpenFitVariableDataComposer sender = new OpenFitVariableDataComposer();
        //4f 70 65 6e 46 69 74 "OpenFit"
        sender.writeByte((byte)79);
        sender.writeByte((byte)0);
        sender.writeByte((byte)112);
        sender.writeByte((byte)0);
        sender.writeByte((byte)101);
        sender.writeByte((byte)0);
        sender.writeByte((byte)110);
        sender.writeByte((byte)0);
        sender.writeByte((byte)70);
        sender.writeByte((byte)0);
        sender.writeByte((byte)105);
        sender.writeByte((byte)0);
        sender.writeByte((byte)116);
        sender.writeByte((byte)0);
        byte senderLength = 10; //14+2


        OpenFitVariableDataComposer number = new OpenFitVariableDataComposer();
        //000-000-0000
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        number.writeByte((byte)48);
        number.writeByte((byte)0);
        byte numberLength = 16;//20+2
        OpenFitVariableDataComposer title = new OpenFitVariableDataComposer();
        // 4f 70 65 6e 46 69 74 NOTITLE
        title.writeByte((byte)79);
        title.writeByte((byte)0);
        title.writeByte((byte)112);
        title.writeByte((byte)0);
        title.writeByte((byte)101);
        title.writeByte((byte)0);
        title.writeByte((byte)110);
        title.writeByte((byte)0);
        title.writeByte((byte)70);
        title.writeByte((byte)0);
        title.writeByte((byte)105);
        title.writeByte((byte)0);
        title.writeByte((byte)116);
        title.writeByte((byte)0);
        byte titleLength = 10;//14+2

        OpenFitVariableDataComposer message = new OpenFitVariableDataComposer();
        //57 65 6c 63 6f 6d 65 20 74 6f 20 4f 70 65 6e 46 69 74 21 Welcome to OpenFit!
        message.writeByte((byte)87);
        message.writeByte((byte)0);
        message.writeByte((byte)101);
        message.writeByte((byte)0);
        message.writeByte((byte)108);
        message.writeByte((byte)0);
        message.writeByte((byte)99);
        message.writeByte((byte)0);
        message.writeByte((byte)111);
        message.writeByte((byte)0);
        message.writeByte((byte)109);
        message.writeByte((byte)0);
        message.writeByte((byte)101);
        message.writeByte((byte)0);
        message.writeByte((byte)32);
        message.writeByte((byte)0);
        message.writeByte((byte)116);
        message.writeByte((byte)0);
        message.writeByte((byte)111);
        message.writeByte((byte)0);
        message.writeByte((byte)32);
        message.writeByte((byte)0);
        message.writeByte((byte)79);
        message.writeByte((byte)0);
        message.writeByte((byte)112);
        message.writeByte((byte)0);
        message.writeByte((byte)101);
        message.writeByte((byte)0);
        message.writeByte((byte)110);
        message.writeByte((byte)0);
        message.writeByte((byte)70);
        message.writeByte((byte)0);
        message.writeByte((byte)105);
        message.writeByte((byte)0);
        message.writeByte((byte)116);
        message.writeByte((byte)0);
        message.writeByte((byte)33);
        message.writeByte((byte)0);
        byte messageLength = 28;//38+2

        int millis = (int)(System.currentTimeMillis() / 1000L);

        OpenFitVariableDataComposer oVDC = new OpenFitVariableDataComposer();
        oVDC.writeByte((byte)4);
        oVDC.writeInt(102);
        oVDC.writeInt(0);
        oVDC.writeByte(senderLength);
        oVDC.writeByte((byte)255);
        oVDC.writeByte((byte)254);
        oVDC.writeBytes(sender.toByteArray());
        oVDC.writeByte(numberLength);
        oVDC.writeByte((byte)255);
        oVDC.writeByte((byte)254);
        oVDC.writeBytes(number.toByteArray());
        oVDC.writeByte(titleLength);
        oVDC.writeByte((byte)255);
        oVDC.writeByte((byte)254);
        oVDC.writeBytes(title.toByteArray());
        oVDC.writeByte(messageLength);
        oVDC.writeByte((byte)0);
        oVDC.writeByte((byte)255);
        oVDC.writeByte((byte)254);
        oVDC.writeBytes(message.toByteArray());
        oVDC.writeByte((byte)0);
        oVDC.writeInt(millis);

        int length = oVDC.toByteArray().length;

        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)3);
        oVariableDataComposer.writeInt(length);
        oVariableDataComposer.writeBytes(oVDC.toByteArray());
        return oVariableDataComposer.toByteArray();

        //return OpenFitApi.hexStringToByteArray("036500000004660000000000000022fffe41006d0062006100720020004400650020004c00610020004300720075007a0016fffe340030003800340038003300330037003300300010fffe4e004f005400490054004c0045000a00fffe5400650073007400008ed13555");
    }

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
