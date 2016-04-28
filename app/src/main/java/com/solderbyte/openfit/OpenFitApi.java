package com.solderbyte.openfit;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.solderbyte.openfit.protocol.OpenFitNotificationProtocol;
import com.solderbyte.openfit.util.OpenFitData;
import com.solderbyte.openfit.util.OpenFitDataType;
import com.solderbyte.openfit.util.OpenFitDataTypeAndString;
import com.solderbyte.openfit.util.OpenFitTimeZoneUtil;
import com.solderbyte.openfit.util.OpenFitVariableDataComposer;

public class OpenFitApi {

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
        oVariableDataComposer.writeByte(OpenFitData.OPENFIT_DATA);
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

    public static byte[] getFindStart() {
        //05020000000100
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)5);
        oVariableDataComposer.writeInt(2);
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeByte((byte)OpenFitData.FIND_START);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getFindStop() {
        //05020000000101
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)5);
        oVariableDataComposer.writeInt(2);
        oVariableDataComposer.writeByte((byte)1);
        oVariableDataComposer.writeByte((byte)OpenFitData.FIND_STOP);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getMediaPrev() {
        //06020000000005
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)6);
        oVariableDataComposer.writeInt(2);
        oVariableDataComposer.writeByte((byte)OpenFitData.CONTROL);
        oVariableDataComposer.writeByte((byte)OpenFitData.REWIND);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getMediaNext() {
        //06020000000004
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)6);
        oVariableDataComposer.writeInt(2);
        oVariableDataComposer.writeByte((byte)OpenFitData.CONTROL);
        oVariableDataComposer.writeByte((byte)OpenFitData.FORWARD);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getMediaPlay() {
        //06020000000001
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)6);
        oVariableDataComposer.writeInt(2);
        oVariableDataComposer.writeByte((byte)OpenFitData.CONTROL);
        oVariableDataComposer.writeByte((byte)OpenFitData.PLAY);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getMediaPause() {
        //06020000000002
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)6);
        oVariableDataComposer.writeInt(2);
        oVariableDataComposer.writeByte((byte)OpenFitData.CONTROL);
        oVariableDataComposer.writeByte((byte)OpenFitData.PAUSE);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getMediaStop() {
        //06020000000003
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)6);
        oVariableDataComposer.writeInt(2);
        oVariableDataComposer.writeByte((byte)OpenFitData.CONTROL);
        oVariableDataComposer.writeByte((byte)OpenFitData.STOP);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getMediaVolume() {
        //060200000001XX
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)6);
        oVariableDataComposer.writeInt(2);
        oVariableDataComposer.writeByte((byte)OpenFitData.VOLUME);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getMediaSetVolume(byte vol) {
        //060200000001XX
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)6);
        oVariableDataComposer.writeInt(2);
        oVariableDataComposer.writeByte((byte)OpenFitData.VOLUME);
        oVariableDataComposer.writeByte((byte)vol);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getMediaReqStart() {
        //060100000003
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)6);
        oVariableDataComposer.writeInt(1);
        oVariableDataComposer.writeByte((byte)OpenFitData.REQUEST_START);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getMediaReqStop() {
        //060100000004
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)6);
        oVariableDataComposer.writeInt(1);
        oVariableDataComposer.writeByte((byte)OpenFitData.REQUEST_STOP);
        return oVariableDataComposer.toByteArray();
    }
    //06020000000006
    //06020000000007

    public static byte[] getFitness() {
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getFitnessSyncRes() {
        //02080000000300000001000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(8);
        oVariableDataComposer.writeInt(3);
        oVariableDataComposer.writeInt(1);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getFitnessRequest() {
        //02050000000001000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(5);
        oVariableDataComposer.writeByte((byte)0);
        oVariableDataComposer.writeInt(1);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getFitnessMenu() {
        //02040000001b000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(27);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getFitnessMenuResponse() {
        //02010000001c
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(28);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getFitnessCycling() {
        //02040000001200000002100000001300000003000000 3D8A2C4359DAC742
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(18);
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(16);
        oVariableDataComposer.writeInt(19);
        oVariableDataComposer.writeInt(3);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getHealthApp() {
        //02040000001200000002100000001300000003000000 3D8A2C4359DAC742
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(21);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getHealthAppResponse() {
        //02040000001200000002100000001300000003000000 3D8A2C4359DAC742
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(20);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getHealthHeartResponse() {
        //02040000001200000002100000001300000003000000 3D8A2C4359DAC742
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(2);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getGPSReady(int exerciseType) {
        //0204000000120000000210000000130000000100000000003B430000D642 -- walking
        //0204000000120000000210000000130000000200000000003B430000D642 -- running
        //0204000000120000000210000000130000000300000000003B430000D642 -- cycling
        //0204000000120000000210000000130000000400000000003B430000D642 -- hiking
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_WINGTIP_TO_HOST_GPS_READY);
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(16);
        oVariableDataComposer.writeInt(19);
        oVariableDataComposer.writeInt(exerciseType);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getResponseGPSReady() {
        //02240000001600000000000000268fbb42b4ecb6420000000000000000000000000000000000000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_HOST_TO_WINGTIP_GPS_READY);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getGPSSubscribe() {
        //0204000000120000000210000000130000000400000000003B430000D642
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_WINGTIP_TO_HOST_GPS_SUBSCRIBE);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getGPSUnSubscribe() {
        //0204000000120000000210000000130000000400000000003B430000D642
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_WINGTIP_TO_HOST_GPS_UNSUBSCRIBE);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getResponseGPSData(float tD, float cS, float cC, float cA) {
        //02040000000E000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(24);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_HOST_TO_WINGTIP_GPS_DATA);
        oVariableDataComposer.writeFloat(tD);
        oVariableDataComposer.writeFloat(cS);
        oVariableDataComposer.writeFloat(cC);
        oVariableDataComposer.writeDouble(cA);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getResponseGPSResult(float tD, float maxA, float minA, float maxS, float avgS, float cC, float iD, float dD) {
        //02040000000E000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(36);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_HOST_TO_WINGTIP_GPS_RESULT);
        oVariableDataComposer.writeFloat(tD);
        oVariableDataComposer.writeFloat(maxA);
        oVariableDataComposer.writeFloat(minA);
        oVariableDataComposer.writeFloat(maxS);
        oVariableDataComposer.writeFloat(avgS);
        oVariableDataComposer.writeFloat(cC);
        oVariableDataComposer.writeFloat(iD);
        oVariableDataComposer.writeFloat(dD);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getSync() {
        //020400000005000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_WINGTIP_TO_HOST_SYNC_REQUEST);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getResponseSyncDone() {
        //020400000005000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_HOST_TO_WINGTIP_SYNC_DONE);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getGPSEnd() {
        //02040000000A00000002040000000A00000002040000001B000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_WINGTIP_TO_HOST_GPS_END);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getResponseGPSOFF() {
        //02040000000A000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_HOST_TO_WINGTIP_GPS_GPSOFF);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getResponseGPSON() {
        //02040000000A000000
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(OpenFitData.DATA_TYPE_HOST_TO_WINGTIP_GPS_GPSON);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getFitnessRunning() {
        //02040000001200000002100000001300000002000000 3D8A2C4359DAC742
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(4);
        oVariableDataComposer.writeInt(18);
        oVariableDataComposer.writeByte((byte)2);
        oVariableDataComposer.writeInt(16);
        oVariableDataComposer.writeInt(19);
        oVariableDataComposer.writeInt(2);
        //return oVariableDataComposer.toByteArray();
        return hexStringToByteArray("02040000000c000000");
    }

    public static byte[] getCurrentTimeInfo(boolean is24Hour) {
        //011E0000000141CB3555F8FFFFFF000000000101010201A01DFC5490D43556100E0000
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
        //oVDC.writeBoolean(OpenFitData.IS_TIME_DISPLAY_24);
        oVDC.writeBoolean(is24Hour);
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
    }

    public static byte[] getOpenFitWelcomeNotification() {
        //03
        //71000000 = size of msg
        //04 = DATA_TYPE_MESSAGE
        //0400000000000000 = id
        //10 = sender name size + 2
        //FF
        //FE
        //4F00700065006E00460069007400 = OpenFit
        //16 = sender number size + 2
        //FF
        //FE
        //3500350035003100320033003400350036003700 = 5551234567
        //10 = msg title + 2
        //FF
        //FE
        //4E004F005400490054004C004500 = NOTITLE
        //28 = msg data + 2
        //00
        //FF
        //FE
        //570065006C0063006F006D006500200074006F0020004F00700065006E004600690074002100 = Welcome to OpenFit!
        //00
        //5E0E8955 = time stamp
        List<OpenFitDataTypeAndString> mDataList = new ArrayList<OpenFitDataTypeAndString>();
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, "OpenFit"));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, "5551234567"));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, "NOTITLE"));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.SHORT, "Welcome to OpenFit!"));

        long id = System.currentTimeMillis() / 1000L;
        byte[] msg = OpenFitNotificationProtocol.createNotificationProtocol(OpenFitData.DATA_TYPE_MESSAGE, id, mDataList, System.currentTimeMillis());
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)3);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenNotification(String sender, String number, String title, String message, long id) {
        //03
        //71000000 = size of msg
        //04 = DATA_TYPE_MESSAGE
        //0400000000000000 = id
        //10 = sender name size + 2
        //FF
        //FE
        //4F00700065006E00460069007400 = OpenFit
        //16 = sender number size + 2
        //FF
        //FE
        //3500350035003100320033003400350036003700 = 5551234567
        //10 = msg title + 2
        //FF
        //FE
        //4E004F005400490054004C004500 = NOTITLE
        //28 = msg data + 2
        //00
        //FF
        //FE
        //570065006C0063006F006D006500200074006F0020004F00700065006E004600690074002100 = Welcome to OpenFit!
        //00
        //5E0E8955 = time stamp
        if(sender == null || sender.isEmpty()) {
            sender = "OpenFit";
        }
        if(number == null || number.isEmpty()) {
            number = "OpenFit";
        }
        if(title == null || title.isEmpty()) {
            title = "OpenFit Title";
        }
        if(message == null || message.isEmpty()) {
            message = "OpenFit Message";
        }

        List<OpenFitDataTypeAndString> mDataList = new ArrayList<OpenFitDataTypeAndString>();
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, sender));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, number));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, trimTitle(title)));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.SHORT, trimMessage(message)));

        byte[] msg = OpenFitNotificationProtocol.createNotificationProtocol(OpenFitData.DATA_TYPE_MESSAGE, id, mDataList, System.currentTimeMillis());
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)3);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenEmail(String sender, String number, String title, String message, long id) {
        if(sender == null || sender.isEmpty()) {
            sender = "OpenFit";
        }
        if(number == null || number.isEmpty()) {
            number = "OpenFit";
        }
        if(title == null || title.isEmpty()) {
            title = "OpenFit Title";
        }
        if(message == null || message.isEmpty()) {
            message = "OpenFit Email";
        }

        List<OpenFitDataTypeAndString> mDataList = new ArrayList<OpenFitDataTypeAndString>();
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, sender));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, number));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, trimTitle(title)));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.SHORT, trimMessage(message)));

        byte[] msg = OpenFitNotificationProtocol.createEmailProtocol(OpenFitData.DATA_TYPE_EMAIL, id, mDataList, System.currentTimeMillis());
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)3);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenIncomingCall(String sender, String number, long id) {
        //09
        //30000000 = size of msg
        //00 = DATA_TYPE_INCOMING_CALL
        //fb73770c4f010000 = id
        //00 = call flag
        //0a = size + 2
        //ff
        //fe
        //48006f006d006500 = sender
        //16 = size + 2
        //ff
        //fe
        //0000000000000000000000000000000000000000 = phone number
        //5fc0c555
        if(sender == null || sender.isEmpty()) {
            sender = "OpenFit";
        }
        if(number == null || number.isEmpty()) {
            number = "OpenFit";
        }

        List<OpenFitDataTypeAndString> mDataList = new ArrayList<OpenFitDataTypeAndString>();
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, sender));
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, number));

        byte[] msg = OpenFitNotificationProtocol.createIncomingCallProtocol(OpenFitData.DATA_TYPE_INCOMING_CALL, id, mDataList, System.currentTimeMillis());
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)9);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenRejectCall() {
        //090600000003013FE1CA55
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)9);
        oVariableDataComposer.writeInt(6);
        oVariableDataComposer.writeByte((byte)3);
        oVariableDataComposer.writeByte((byte)1);

        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getOpenIncomingCallEnd() {
        //090100000002
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)9);
        oVariableDataComposer.writeInt(1);
        oVariableDataComposer.writeByte((byte)2);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getOpenRejectCallMessage() {
        //  0906000000030201000000
        //all msg size  _| |_ index of message
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        oVariableDataComposer.writeByte((byte)9);
        oVariableDataComposer.writeInt(6);
        oVariableDataComposer.writeByte((byte)3);
        oVariableDataComposer.writeByte((byte)2);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getOpenRejectCallMessageForBracelet(int allCount, int index, String msg) {
        OpenFitVariableDataComposer oVariableDataComposer = new OpenFitVariableDataComposer();
        byte[] arr = OpenFitVariableDataComposer.convertToByteArray(msg);
        oVariableDataComposer.writeByte((byte)9);
        oVariableDataComposer.writeInt(5 + arr.length);
        oVariableDataComposer.writeByte((byte)4);
        oVariableDataComposer.writeByte((byte)allCount);
        oVariableDataComposer.writeByte((byte)index);
        oVariableDataComposer.writeShort((short)arr.length);
        oVariableDataComposer.writeBytes(arr);
        return oVariableDataComposer.toByteArray();
    }

    public static byte[] getOpenMediaTrack(String track) {
        //06
        //26000000
        //02
        //24 = size + 2
        //ff
        //fe
        //44006100660074002000500075006e006b0020002d00200046007200650073006800 = track name

        byte[] msg = OpenFitNotificationProtocol.createMediaTrackProtocol(OpenFitData.DATA_TYPE_MEDIATRACK, track);

        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)6);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenAlarm(long id) {
        //0a
        //1e000000 = size of mg
        //01 = msg type
        //0100000000000000 = msg id
        //0c = size of string
        //ff
        //fe
        //41006c00610072006d00 = string
        //c1040000 = little endian odd time stamp
        //00000000 = snooze = false
        List<OpenFitDataTypeAndString> mDataList = new ArrayList<OpenFitDataTypeAndString>();
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, "Alarm"));

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        String timeString = Integer.toString(hour)+Integer.toString(minute);
        int time = Integer.parseInt(timeString);

        byte[] msg = OpenFitNotificationProtocol.createAlarmProtocol(OpenFitData.DATA_TYPE_ALARMCLOCK, id, mDataList, time);

        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)10);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenAlarmClear() {
        //0a0100000000 clear from phone
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)10);
        oDatacomposer.writeInt(1);
        oDatacomposer.writeByte((byte)0);

        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenAlarmCleared() {
        //0A020000000300 clear from gear
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)10);
        oDatacomposer.writeInt(2);
        oDatacomposer.writeByte((byte)3);
        oDatacomposer.writeByte((byte)0);

        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenAlarmSnoozed() {
        //0A020000000301 snooze from gear
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)10);
        oDatacomposer.writeInt(2);
        oDatacomposer.writeByte((byte)3);
        oDatacomposer.writeByte((byte)1);

        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenWeatherReq() {
        //01010000000C
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)1);
        oDatacomposer.writeInt(1);
        oDatacomposer.writeByte((byte)12);

        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenWeather(String weather, String icon, long id) {
        int i = getOpenWeatherIcon(icon);
        byte[] msg = OpenFitNotificationProtocol.createWeatherProtocol(OpenFitData.DATA_TYPE_WEATHER, id, weather, i, System.currentTimeMillis());
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)3);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();
    }

    public static byte[] getOpenWeatherClock(String location, String temp, String unit, String icon) {
        //01
        //3d000000
        //09
        //14 = size + 2?
        //ff
        //fe
        //4e0069006500640065007200720061006400 = city name
        //06
        //40060000
        //01 = units 01 C, 00 F
        //00
        //c944e055 = time stamp
        //06
        //98080000
        //14050000
        //06000000
        //00000000
        //00
        //0600
        //00000000
        //00000000

        //usage 9 = 0, 90 = 1, 900 = 9, 9000 = 90
        List<OpenFitDataTypeAndString> mDataList = new ArrayList<OpenFitDataTypeAndString>();
        mDataList.add(new OpenFitDataTypeAndString(OpenFitDataType.BYTE, location));

        if(temp == null) {
            temp = "0";
        }
        float t = Float.parseFloat(temp);
        int tempInt = Math.round(t);
        if(tempInt < 10) {
            tempInt = tempInt * 100;
        }
        else if(tempInt < 100) {
            tempInt = tempInt * 100;
        }
        else if(tempInt < 1000) {
            tempInt = tempInt * 10;
        }

        int tempUnit = 1;
        if(unit.contains("F")) {
            tempUnit = 0;
        }

        int i = getOpenWeatherClockIcon(icon);
        byte[] msg = OpenFitNotificationProtocol.createWeatherClockProtocol(9, mDataList, tempInt, tempUnit, i, System.currentTimeMillis());
        OpenFitVariableDataComposer oDatacomposer = new OpenFitVariableDataComposer();
        oDatacomposer.writeByte((byte)1);
        oDatacomposer.writeInt(msg.length);
        oDatacomposer.writeBytes(msg);
        return oDatacomposer.toByteArray();
    }

    public static int getOpenWeatherIcon(String icon) {
        int i = 0;
        if(icon == null) {
            icon = "01";
        }
        if(icon.contains("01")) {
            i = OpenFitData.WEATHER_TYPE_SUNNY;
        }
        else if(icon.contains("02")) {
            i = OpenFitData.WEATHER_TYPE_MOSTLY_CLEAR;
        }
        else if(icon.contains("03")) {
            i = OpenFitData.WEATHER_TYPE_MOSTLY_CLOUDY;
        }
        else if(icon.contains("04")) {
            i = OpenFitData.WEATHER_TYPE_MOSTLY_CLOUDY;
        }
        else if(icon.contains("09")) {
            i = OpenFitData.WEATHER_TYPE_HEAVY_RAIN;
        }
        else if(icon.contains("10")) {
            i = OpenFitData.WEATHER_TYPE_PARTLY_SUNNY_SHOWERS;
        }
        else if(icon.contains("11")) {
            i = OpenFitData.WEATHER_TYPE_THUNDERSTORMS;
        }
        else if(icon.contains("13")) {
            i = OpenFitData.WEATHER_TYPE_SNOW;
        }
        else if(icon.contains("50")) {
            i = OpenFitData.WEATHER_TYPE_FOG;
        }
        return i;
    }

    public static int getOpenWeatherClockIcon(String icon) {
        int i = 0;
        if(icon == null) {
            icon = "01";
        }
        if(icon.contains("01")) {
            i = OpenFitData.WEATHER_CLOCK_SUNNY;
        }
        else if(icon.contains("02")) {
            i = OpenFitData.WEATHER_CLOCK_CLEAR;
        }
        else if(icon.contains("03")) {
            i = OpenFitData.WEATHER_CLOCK_MOSTLY_CLOUDY;
        }
        else if(icon.contains("04")) {
            i = OpenFitData.WEATHER_CLOCK_MOSTLY_CLOUDY;
        }
        else if(icon.contains("09")) {
            i = OpenFitData.WEATHER_CLOCK_SHOWERS;
        }
        else if(icon.contains("10")) {
            i = OpenFitData.WEATHER_CLOCK_PARTLY_SUNNY_SHOWERS;
        }
        else if(icon.contains("11")) {
            i = OpenFitData.WEATHER_CLOCK_THUNDERSTORMS;
        }
        else if(icon.contains("13")) {
            i = OpenFitData.WEATHER_CLOCK_SNOW;
        }
        else if(icon.contains("50")) {
            i = OpenFitData.WEATHER_CLOCK_FOG;
        }
        return i;
    }

    public static String trimTitle(String s) {
        s = s.substring(0, Math.min(s.length(), 50));
        return s;
    }

    public static String trimMessage(String s) {
        s = s.substring(0, Math.min(s.length(), 250));
        return s;
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
