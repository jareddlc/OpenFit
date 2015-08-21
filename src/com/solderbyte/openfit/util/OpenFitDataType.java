package com.solderbyte.openfit.util;

public final class OpenFitDataType {

    public static final OpenFitDataType BIT;
    public static final OpenFitDataType BYTE;
    public static final OpenFitDataType DOUBLE;
    public static final OpenFitDataType ENUM$VALUES[];
    public static final OpenFitDataType FLOAT;
    public static final OpenFitDataType INT;
    public static final OpenFitDataType LONG;
    public static final OpenFitDataType SHORT;
    public String STRING;
    public int INTEGER;

    private OpenFitDataType(String s, int i) {
        STRING = s;
        INTEGER = i;
    }

    public static OpenFitDataType valueOf(String s) {
        OpenFitDataType o = null;
        if(s.equals("BYTE")) {
            o = new OpenFitDataType("BYTE", 0);
        }
        else if(s.equals("SHORT")) {
            o = new OpenFitDataType("SHORT", 1);
        }
        else if(s.equals("INT")) {
            o = new OpenFitDataType("INT", 2);
        }
        else if(s.equals("LONG")) {
            o = new OpenFitDataType("LONG", 3);
        }
        else if(s.equals("FLOAT")) {
            o = new OpenFitDataType("FLOAT", 4);
        }
        else if(s.equals("DOUBLE")) {
            o = new OpenFitDataType("DOUBLE", 5);
        }
        else if(s.equals("BIT")) {
            o = new OpenFitDataType("BIT", 6);
        }
        return o;
    }

    public static OpenFitDataType[] values() {
        OpenFitDataType oType[] = ENUM$VALUES;
        int i = oType.length;
        OpenFitDataType fType[] = new OpenFitDataType[i];
        System.arraycopy(oType, 0, fType, 0, i);
        return fType;
    }

    static {
        BYTE = new OpenFitDataType("BYTE", 0);
        SHORT = new OpenFitDataType("SHORT", 1);
        INT = new OpenFitDataType("INT", 2);
        LONG = new OpenFitDataType("LONG", 3);
        FLOAT = new OpenFitDataType("FLOAT", 4);
        DOUBLE = new OpenFitDataType("DOUBLE", 5);
        BIT = new OpenFitDataType("BIT", 6);
        OpenFitDataType oType[] = new OpenFitDataType[7];
        oType[0] = BYTE;
        oType[1] = SHORT;
        oType[2] = INT;
        oType[3] = LONG;
        oType[4] = FLOAT;
        oType[5] = DOUBLE;
        oType[6] = BIT;
        ENUM$VALUES = oType;
    }
}
