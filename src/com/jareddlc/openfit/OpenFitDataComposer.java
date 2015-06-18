package com.jareddlc.openfit;
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
    
    public void writeString(String paramString) {
      writeBytes(paramString.getBytes(DEFAULT_CHARSET));
    }
}
