package com.solderbyte.openfit.util;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class OpenFitData {
    // Application type
    public static final byte FEATURE_EXCHANGE = 0;
    public static final byte HOME = 1;
    public static final byte HEALTH = 2;
    public static final byte NOTIFICATION = 3;
    public static final byte STATUS_MANAGER = 4;
    public static final byte FIND_MY_WINGTIP = 5;
    public static final byte MEDIA_CONTROLLER = 6;
    public static final byte GESTURE_SERVICE = 7;
    public static final byte SENSORDATA_SVC = 8;
    public static final byte CALL_APP = 9;
    public static final byte ALARM_APP = 10;
    public static final byte SPLANNER_APP = 11;
    
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

    // Fitness type
    public static byte DATA_TYPE_USER_PROFILE = 0;
    public static byte DATA_TYPE_PEDOMETER_PROFILE = 1;
    public static byte DATA_TYPE_COACHING_PROFILE = 2;
    public static byte DATA_TYPE_PEDO_RESULTRECORD = 3;
    public static byte DATA_TYPE_PEDO_INFO = 4;
    public static byte DATA_TYPE_SLEEP_RESULTRECORD = 5;
    public static byte DATA_TYPE_SLEEP_INFO = 6;
    public static byte DATA_TYPE_HEARTRATE_RESULTRECORD = 7;
    public static byte DATA_TYPE_COACHING_VARS = 8;
    public static byte DATA_TYPE_COACHING_EXERCISERESULT = 9;
    public static byte DATA_TYPE_COACHING_RUNNINGEXERCISE = 10;
    public static byte DATA_TYPE_COACHING_ENERGYEXERCISE = 11;
    public static byte DATA_TYPE_COACHING_RESULTRECORD = 12;
    public static byte DATA_TYPE_GPS_INFO = 13;
    public static byte STRUCT_TYPE_DASHBOARD_PEDO_RESULT = 14;
    public static byte STRUCT_TYPE_DASHBOARD_COACHING_RESULT = 15;
    public static byte STRUCT_TYPE_DASHBOARD_HRM_RESULT = 16;
    public static byte STRUCT_TYPE_DASHBOARD_SLEEP_RESULT = 17;

    public static byte RUN = 0;
    public static byte WALK = 1;
    public static byte CYCLING = 2;
    public static byte HIKING = 3;

    public static byte DATA_TYPE_HOST_TO_WINGTIP_SYNC_REQUEST = 0;
    public static byte DATA_TYPE_HOST_TO_WINGTUP_SYNC_DONE = 1;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_SYNC_DATA = 2;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_SYNC_DONE = 3;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_SYNC_DATA = 4;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_SYNC_REQUEST = 5;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_HIKING_START = 6;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_CYCLING_START = 7;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_WALKING_START = 8;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_RUNNING_START = 9;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_END = 10;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_REQUEST = 11;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_GPS_READY = 12;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_GPS_DATA = 13;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_SUBSCRIBE = 14;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_UNSUBSCRIBE = 15;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_GPS_GPSON = 16;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_GPS_GPSOFF = 17;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_READY = 18;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_GPS_EXERCISE_START = 19;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_SET_HEALTH_APP = 20;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_SET_HEALTH_APP_DONE = 21;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_GPS_RESULT = 22;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_DASHBOARD_SYNC_REQUEST = 23;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_DASHBOARD_SYNC_DATA = 24;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_DASHBOARD_SYNC_DATA = 25;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_DASHBOARD_SYNC_DONE = 26;
    public static byte DATA_TYPE_WINGTIP_TO_HOST_BAROMETER_READY = 27;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_BAROMETER_ON = 28;
    public static byte DATA_TYPE_HOST_TO_WINGTIP_BAROMETER_OFF = 29;

    // ActivityType
    public static final int TYPE_ACTIVITY_HEAVY = 180004;
    public static final int TYPE_ACTIVITY_LIGHT = 180002;
    public static final int TYPE_ACTIVITY_LITTLE_TO_NO = 180001;
    public static final int TYPE_ACTIVITY_MODERATE = 180003;
    public static final int TYPE_ACTIVITY_VERY_HEAVY = 180005;

    // Gender
    public static final int FEMALE = 190006;
    public static final int MALE = 190005;

    // DistanceUnit
    public static final int KILLOMETERS = 170001;
    public static final int MILES = 170003;
    public static final int YARDS = 170002;

    // HeightUnit
    public static final int CM = 150001;
    public static final int FEET = 150002;

    // WeightUnit
    public static final int KILOGRAM = 130001;
    public static final int LBS = 130002;

    public static final int FITNESS_MENU = 27;
    public static final int FITNESS_CANCEL = 10;
    public static final int FITNESS_UNKOWN = 5;

    // Find device
    public static final byte FIND_START = 0;
    public static final byte FIND_STOP  = 1;

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

    // Weather Type
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

    // Weather Clock
    public static final int WEATHER_CLOCK_RESERVED = 0;
    public static final int WEATHER_CLOCK_SUNNY = 1;
    public static final int WEATHER_CLOCK_PARTLY_SUNNY = 2;
    public static final int WEATHER_CLOCK_MOSTLY_CLOUDY = 3;
    public static final int WEATHER_CLOCK_RAIN = 4;
    public static final int WEATHER_CLOCK_FOG = 5;
    public static final int WEATHER_CLOCK_SHOWERS = 6;
    public static final int WEATHER_CLOCK_PARTLY_SUNNY_SHOWERS = 7;
    public static final int WEATHER_CLOCK_THUNDERSTORMS = 8;
    public static final int WEATHER_CLOCK_MOSTLY_CLOUDY_THUNDER_SHOWER = 9;
    public static final int WEATHER_CLOCK_FLURRIES = 10;
    public static final int WEATHER_CLOCK_MOSTLY_CLOUDY_FLURRIES = 11;
    public static final int WEATHER_CLOCK_SNOW = 12;
    public static final int WEATHER_CLOCK_RAIN_SNOW = 13;
    public static final int WEATHER_CLOCK_ICE = 14;
    public static final int WEATHER_CLOCK_HOT = 15;
    public static final int WEATHER_CLOCK_COLD = 16;
    public static final int WEATHER_CLOCK_WINDY = 17;
    public static final int WEATHER_CLOCK_CLEAR = 18;
    public static final int WEATHER_CLOCK_MOSTLY_CLOUDY2 = 19;
    public static final int WEATHER_CLOCK_HAIL = 20;
    public static final int WEATHER_CLOCK_HEAVY_RAIN = 21;

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

    public static String getGender(int i) {
        if(i == FEMALE) {
            return "Female";
        }
        else if(i == MALE) {
            return "Male";
        }
        else {
            return null;
        }
    }

    public static String getDistanceUnit(int i) {
        if(i == KILLOMETERS) {
            return "Km";
        }
        else if(i == MILES) {
            return "Mi";
        }
        else if(i == YARDS) {
            return "Yd";
        }
        else {
            return null;
        }
    }

    public static String getHeightUnit(int i) {
        if(i == CM) {
            return "cm";
        }
        else if(i == FEET) {
            return "ft";
        }
        else {
            return null;
        }
    }
    
    public static String getWeightUnit(int i) {
        if(i == KILOGRAM) {
            return "Kg";
        }
        else if(i == LBS) {
            return "lbs";
        }
        else {
            return null;
        }
    }
}
