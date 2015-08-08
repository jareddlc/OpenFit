package com.jareddlc.openfit.util;

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

    // Status Data Type
    public static final byte ALL_INFO = 2;
    public static final byte AUTO_LOCK = 3;
    public static final byte BATTERY_STATUS = 11;
    public static final byte CLOCK_TYPE_ORDER = 16;
    public static final byte DISPLAY_TYPE = 24;
    public static final byte DOUBLE_PRESS_LAUNCH_APP_TYPE = 21;
    public static final byte FONT_SIZE = 9;
    public static final byte FOTA = 14;
    public static final byte HOME_BG_COLOR = 7;
    public static final byte HOME_BG_GALLERY = 15;
    public static final byte HOME_BG_WALLPAPER = 8;
    public static final byte HOME_LAYOUT_ORDER = 18;
    public static final byte LANGUAGE = 10;
    public static final byte LANGUAGE_RESOURCE = 20;
    public static final byte LANGUAGE_RESOURCE_REQUEST = 19;
    public static final byte OPEN_SOURCE_GUIDE_TYPE = 22;
    public static final byte REQUEST_ALL_INFO = 1;
    public static final byte SCREEN_TIMEOUT = 6;
    public static final byte SHAKE_TO_CONTROL = 17;
    public static final byte SMART_RELAY = 4;
    public static final byte SMART_RELAY_CURRENT_DISPLAY = 12;
    public static final byte SOS = 13;
    public static final byte WAKEUP_BY_GESTURE = 5;
    public static final byte WINGTIP_DEVICE_INFO = 23;
    public static final byte WINGTIP_VERSION = 0;

    // Weather
    /*WEATHER_TYPE_CLEAR = new EWeatherType("WEATHER_TYPE_CLEAR", 0);
    WEATHER_TYPE_COLD = new EWeatherType("WEATHER_TYPE_COLD", 1);
    WEATHER_TYPE_FLURRIES = new EWeatherType("WEATHER_TYPE_FLURRIES", 2);
    WEATHER_TYPE_FOG = new EWeatherType("WEATHER_TYPE_FOG", 3);
    WEATHER_TYPE_HAIL = new EWeatherType("WEATHER_TYPE_HAIL", 4);
    WEATHER_TYPE_HEAVY_RAIN = new EWeatherType("WEATHER_TYPE_HEAVY_RAIN", 5);
    WEATHER_TYPE_HOT = new EWeatherType("WEATHER_TYPE_HOT", 6);
    WEATHER_TYPE_ICE = new EWeatherType("WEATHER_TYPE_ICE", 7);
    WEATHER_TYPE_MOSTLY_CLEAR = new EWeatherType("WEATHER_TYPE_MOSTLY_CLEAR", 8);
    WEATHER_TYPE_MOSTLY_CLOUDY = new EWeatherType("WEATHER_TYPE_MOSTLY_CLOUDY", 9);
    WEATHER_TYPE_MOSTLY_CLOUDY_FLURRIES = new EWeatherType("WEATHER_TYPE_MOSTLY_CLOUDY_FLURRIES", 10);
    WEATHER_TYPE_MOSTLY_CLOUDY_THUNDER_SHOWER = new EWeatherType("WEATHER_TYPE_MOSTLY_CLOUDY_THUNDER_SHOWER", 11);
    WEATHER_TYPE_PARTLY_SUNNY = new EWeatherType("WEATHER_TYPE_PARTLY_SUNNY", 12);
    WEATHER_TYPE_PARTLY_SUNNY_SHOWERS = new EWeatherType("WEATHER_TYPE_PARTLY_SUNNY_SHOWERS", 13);
    WEATHER_TYPE_RAIN = new EWeatherType("WEATHER_TYPE_RAIN", 14);
    WEATHER_TYPE_RAIN_SNOW = new EWeatherType("WEATHER_TYPE_RAIN_SNOW", 15);
    WEATHER_TYPE_SANDSTORM = new EWeatherType("WEATHER_TYPE_SANDSTORM", 16);
    WEATHER_TYPE_SHOWERS = new EWeatherType("WEATHER_TYPE_SHOWERS", 17);
    WEATHER_TYPE_SNOW = new EWeatherType("WEATHER_TYPE_SNOW", 18);
    WEATHER_TYPE_SUNNY = new EWeatherType("WEATHER_TYPE_SUNNY", 19);
    WEATHER_TYPE_THUNDERSTORMS = new EWeatherType("WEATHER_TYPE_THUNDERSTORMS", 20);
    WEATHER_TYPE_WINDY = new EWeatherType("WEATHER_TYPE_WINDY", 21);
    WEATHER_TYPE_RESERVED = new EWeatherType("WEATHER_TYPE_RESERVED", 22);*/

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

    // Font Type
    public static final byte TYPE_FONT_LARGE = 2;
    public static final byte TYPE_FONT_NORMAL = 1;
    public static final byte TYPE_FONT_SMALL = 0;

    // HOME BG Type
    public static final int TYPE_HOME_BG_TY_COLOR = 0;
    public static final int TYPE_HOME_BG_IMAGE = 2;
    public static final int TYPE_HOME_BG_WALLPAPER = 1;

    // Unknown Data
    public static final int DISCONNECTED_BY_ACL_DISCONNECTED = 2;
    public static final int DISCONNECTED_BY_SOCKET_CLOSED = 1;
    public static final int DISCONNECTED_BY_TIMEOUT = 3;

    public static final int MSG_ID_CONNECTED = 2;
    public static final int MSG_ID_DATA_RECEIVED = 5;
    public static final int MSG_ID_DISCONNECTED = 3;
    public static final int MSG_ID_ETC_DATA_RECEIVED = 6;

    public static final byte LAUNCHER_APP_TYPE_CLOCK = 1;
    public static final byte LAUNCHER_APP_TYPE_CUIP = 0;
    public static final byte LAUNCHER_APP_TYPE_EXERCISE = 6;
    public static final byte LAUNCHER_APP_TYPE_FIND_MY_DEIVCE = 4;
    public static final byte LAUNCHER_APP_TYPE_HEARTRATE = 7;
    public static final byte LAUNCHER_APP_TYPE_MAX = 12;
    public static final byte LAUNCHER_APP_TYPE_MEDIA_CONTROLLER = 3;
    public static final byte LAUNCHER_APP_TYPE_NOTIFICATIONS = 2;
    public static final byte LAUNCHER_APP_TYPE_PEDOMETER = 5;
    public static final byte LAUNCHER_APP_TYPE_SETTINGS = 10;
    public static final byte LAUNCHER_APP_TYPE_SLEEP = 11;
    public static final byte LAUNCHER_APP_TYPE_STOP_WATCH = 9;
    public static final byte LAUNCHER_APP_TYPE_TIMER = 8;

    public static final byte TEXT_DATE_FORMAT_TYPE = 1; // 0,1,2
    public static final byte NUMBER_DATE_FORMAT_TYPE = 2; // 0,1,2
    public static final boolean IS_TIME_DISPLAY_24 = false; // 0,1
}
