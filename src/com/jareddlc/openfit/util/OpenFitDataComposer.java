package com.jareddlc.openfit.util;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import android.graphics.Bitmap;

public class OpenFitDataComposer {
    private static final String LOG_TAG = "OpenFit:OpenFitDataComposer";
    public static final int TIME_INFO_LENGTH = 4;
    private final ByteBuffer mByteBuffer;
    private final int mPayloadSize;

    static final ByteOrder BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;
    static final Charset DEFAULT_CHARSET = Charset.forName("UCS-2");
    static final Charset DEFAULT_DECODING_CHARSET = Charset.forName("US-ASCII");
    public static final int MAX_UNSIGNED_BYTE_VALUE = 255;
    public static final int SIZE_OF_DOUBLE = 8;
    public static final int SIZE_OF_FLOAT = 4;
    public static final int SIZE_OF_INT = 4;
    public static final int SIZE_OF_LONG = 8;
    public static final int SIZE_OF_SHORT = 2;

    public OpenFitDataComposer(int paramInt) {
      this.mByteBuffer = ByteBuffer.allocate(paramInt);
      this.mByteBuffer.order(BYTE_ORDER);
      this.mPayloadSize = paramInt;
    }

    public static byte[] convertToByteArray(String pString) {
      return pString.getBytes(DEFAULT_CHARSET);
    }

    public static void writeCurrentTimeInfo(OpenFitDataComposer pDataComposer)  {
      pDataComposer.writeInt((int)(System.currentTimeMillis() / 1000L));
    }

    public static void writeStringWithOneByteLength(OpenFitDataComposer pDataComposer, String pString) {
      writeStringWithOneByteLength(pDataComposer, pString, 255);
    }

    public static void writeStringWithOneByteLength(OpenFitDataComposer pDataComposer, String pString, int paramInt) {
      byte[] oString = convertToByteArray(pString);
      paramInt = Math.min(paramInt, oString.length);
      pDataComposer.writeByte((byte)paramInt);
      if (paramInt > 0) {
        pDataComposer.writeBytes(oString, 0, paramInt);
      }
    }

    public static void writeTimeInfo(OpenFitDataComposer paramDataComposer, long paramLong) {
      paramDataComposer.writeInt((int)(paramLong / 1000L));
    }

    public int getCapcity() {
      return this.mByteBuffer.capacity();
    }
    
    protected int getPayloadSize() {
      return this.mPayloadSize;
    }
    
    public int getRemainig() {
      return this.mByteBuffer.remaining();
    }
    
    public void reset() {
      this.mByteBuffer.reset();
    }
    
    public void toByteArray(byte[] paramArrayOfByte, int paramInt) {
      byte[] arrayOfByte = this.mByteBuffer.array();
      int i = arrayOfByte.length;
      if(i > paramArrayOfByte.length - paramInt) {
        throw new BufferOverflowException();
      }
      System.arraycopy(arrayOfByte, 0, paramArrayOfByte, paramInt, i);
    }
    
    public byte[] toByteArray() {
      return this.mByteBuffer.array();
    }
    
    public String toString() {
      return "OpenFitDataComposer : Payload(" + this.mPayloadSize + ")";
    }
    
    public void writeBoolean(boolean paramBoolean) {
      if(paramBoolean == true) {
          this.mByteBuffer.put((byte)1);
      }
      else {
          this.mByteBuffer.put((byte)0);
      }
    }
    
    public void writeByte(byte paramByte) {
      this.mByteBuffer.put(paramByte);
    }
    
    public void writeBytes(byte[] paramArrayOfByte) {
      this.mByteBuffer.put(paramArrayOfByte);
    }
    
    public void writeBytes(byte[] paramArrayOfByte, int paramInt1, int paramInt2) {
      this.mByteBuffer.put(paramArrayOfByte, paramInt1, paramInt2);
    }
    
    public void writeDouble(double paramDouble) {
      this.mByteBuffer.putDouble(paramDouble);
    }
    
    public void writeFloat(float paramFloat) {
      this.mByteBuffer.putFloat(paramFloat);
    }
    
    public void writeImage(Bitmap paramBitmap) {
      throw new UnsupportedOperationException("Not impelemented!");
    }
    
    public void writeInt(int paramInt) {
      this.mByteBuffer.putInt(paramInt);
    }
    
    public void writeLong(long paramLong) {
      this.mByteBuffer.putLong(paramLong);
    }
    
    public void writeShort(short paramShort) {
      this.mByteBuffer.putShort(paramShort);
    }
    
    public void writeString(String pString) {
      writeBytes(pString.getBytes(DEFAULT_CHARSET));
    }
}
