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

    private static final int MSG_ID_CONNECTED = 2;
    private static final int MSG_ID_DATA_RECEIVED = 5;
    private static final int MSG_ID_DISCONNECTED = 3;
    private static final int MSG_ID_ETC_DATA_RECEIVED = 6;
    
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
