package com.jareddlc.openfit;

import java.nio.ByteBuffer;

import android.util.Log;

public class OpenFitApi {
    private static final String LOG_TAG = "OpenFit:OpenFitApi";

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


