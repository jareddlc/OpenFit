package com.solderbyte.openfit.util;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class OpenFitData {
    // Data Type
    public static final byte PORT_CM = 99;
    public static final byte PORT_CM_FEATURE = 100;
    public static final byte PORT_CUP = 66;
    public static final byte PORT_FEATURE = 0;
    public static final byte PORT_FOTA = 77;
    public static final byte PORT_FOTA_COMMAND = 78;
    public static final byte PORT_REQUEST_READ_RSSI = 44;
    public static final byte PORT_RSSI = 127;
    public static final byte PORT_SENSOR = 8;

    public static byte DATA_TYPE_INCOMING_CALL = 0;
    public static byte DATA_TYPE_MISSCALL = 1;
    public static byte DATA_TYPE_CALL_ENDED = 2;
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
    public static byte DATA_TYPE_MEDIATRACK = 2;
    public static byte DATA_TYPE_ALARMCLOCK = 1;

    public static byte NOTIFICATION_TYPE_INCOMING_CALL = 0;
    public static byte NOTIFICATION_TYPE_MISSED_CALL = 1;
    public static byte NOTIFICATION_TYPE_EMAIL = 2;
    public static byte NOTIFICATION_TYPE_MESSAGE = 3;
    public static byte NOTIFICATION_TYPE_ALARM = 4;
    public static byte NOTIFICATION_TYPE_SPLANNER = 5;
    public static byte NOTIFICATION_TYPE_WEATHER = 6;
    public static byte NOTIFICATION_TYPE_DOSAGE = 7;
    public static byte NOTIFICATION_TYPE_CHATON = 8;
    public static byte NOTIFICATION_TYPE_MYSINGLE = 9;
    public static byte NOTIFICATION_TYPE_BABY_CRYING_DETECTOR = 10;
    public static byte NOTIFICATION_TYPE_VOICE_MAIL = 11;
    public static byte NOTIFICATION_TYPE_GENERAL = 12;
    public static byte NOTIFICATION_ENABLED = 13;
    public static byte NOTIFICATION_TYPE_WATERINTAKE = 14;
    public static byte NOTIFICATION_OTHER_APP = 15;
    public static byte NOTIFICATION_LIMIT = 16;
    public static byte NOTIFICATION_PREVIEWMESSAGE = 17;
    public static byte NOTIFICATION_SCREENOFF = 18;
    public static byte NOTIFICATION_MORE_NOTIFICATION = 19;
    public static byte NOTIFICATION_TYPE_Docomo_Mailer = 20;
    public static byte NOTIFICATION_TYPE_AreaMail = 21;
    public static byte NOTIFICATION_TYPE_auEmail = 22;
    public static byte NOTIFICATION_TYPE_auSMS = 23;
    public static byte NOTIFICATION_TYPE_auDisaster = 24;
    public static byte NOTIFICATION_TYPE_Disaster_App = 25;
    public static byte NOTIFICATION_INIT_MODE = 26;
    public static byte NOTIFICATION_TYPE_RESERVED = 27;
    public static byte NOTIFICATION_TYPE_SMARTRELAY = 28;
    public static byte NOTIFICATION_TYPE_VZW_CMAS = 29;

    // Media Controller
    public static final byte FORWARD = 4;
    public static final byte FORWARD_RELEASE = 6;
    public static final byte OPEN = 0;
    public static final byte PAUSE = 2;
    public static final byte PLAY = 1;
    public static final byte REWIND = 5;
    public static final byte REWIND_RELEASE = 7;
    public static final byte STOP = 3;

    public static final byte CONTROL = 0;
    public static final byte INFO = 2;
    public static final byte REQUEST_START = 3;
    public static final byte REQUEST_STOP = 4;
    public static final byte VOLUME = 1;

    // Weather
    public static final int WEATHER_TYPE_CLEAR = 0;
    public static final int WEATHER_TYPE_COLD = 1;
    public static final int WEATHER_TYPE_FLURRIES = 2;
    public static final int WEATHER_TYPE_FOG = 3;
    public static final int WEATHER_TYPE_HAIL = 4;
    public static final int WEATHER_TYPE_HEAVY_RAIN = 5;
    public static final int WEATHER_TYPE_HOT = 6;
    public static final int WEATHER_TYPE_ICE = 7;
    public static final int WEATHER_TYPE_MOSTLY_CLEAR = 8;
    public static final int WEATHER_TYPE_MOSTLY_CLOUDY = 9;
    public static final int WEATHER_TYPE_MOSTLY_CLOUDY_FLURRIES = 10;
    public static final int WEATHER_TYPE_MOSTLY_CLOUDY_THUNDER_SHOWER = 11;
    public static final int WEATHER_TYPE_PARTLY_SUNNY = 12;
    public static final int WEATHER_TYPE_PARTLY_SUNNY_SHOWERS = 13;
    public static final int WEATHER_TYPE_RAIN = 14;
    public static final int WEATHER_TYPE_RAIN_SNOW = 15;
    public static final int WEATHER_TYPE_SANDSTORM = 16;
    public static final int WEATHER_TYPE_SHOWERS = 17;
    public static final int WEATHER_TYPE_SNOW = 18;
    public static final int WEATHER_TYPE_SUNNY = 19;
    public static final int WEATHER_TYPE_THUNDERSTORMS = 20;
    public static final int WEATHER_TYPE_WINDY = 21;
    public static final int WEATHER_TYPE_RESERVED = 22;

    // Battery
    public static final byte CHARGING_AC = 2;
    public static final byte CHARGING_USB = 1;
    //public static final byte DISCHARGE;

    // Unknown Data Type
    public static final byte OPENFIT_DATA = 100;

    // Byte Type
    static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    static final Charset DEFAULT_CHARSET = Charset.forName("UCS-2");
    static final Charset DEFAULT_DECODING_CHARSET = Charset.forName("US-ASCII");
    public static final int MAX_UNSIGNED_BYTE_VALUE = 255;
    public static final int SIZE_OF_DOUBLE = 8;
    public static final int SIZE_OF_FLOAT = 4;
    public static final int SIZE_OF_INT = 4;
    public static final int SIZE_OF_LONG = 8;
    public static final int SIZE_OF_SHORT = 2;

    // Protocol specific
    public static final byte TEXT_DATE_FORMAT_TYPE = 1; // 0,1,2
    public static final byte NUMBER_DATE_FORMAT_TYPE = 2; // 0,1,2
    public static final boolean IS_TIME_DISPLAY_24 = false; // 0,1
}
