/*
    //020400000005000000 heart rate 82BPM
    //020400000005000000 heart reate 83BPM
      
    exercuse: 02040000001B000000
    
    running: 020400000012000000021000000013000000020000007A142F4359DAC742
    
    running done: 02040000000A000000
    
    020400000005000000 heart rate ready?
    
    
    private void readFromParcel(Parcel paramParcel)
  {
    this.time = paramParcel.readLong();
    this.heartRate = paramParcel.readInt();
    this.eventTime = paramParcel.readLong();
    this.interval = paramParcel.readLong();
    this.SNR = paramParcel.readFloat();
    this.SNRUnit = paramParcel.readInt();
    Parcelable[] arrayOfParcelable = paramParcel.readParcelableArray(SHeartRateRawData.class.getClassLoader());
    SHeartRateRawData[] arrayOfSHeartRateRawData;
    int i;
    if (arrayOfParcelable != null)
    {
      arrayOfSHeartRateRawData = new SHeartRateRawData[arrayOfParcelable.length];
      i = 0;
      if (i < arrayOfParcelable.length) {}
    }
    for (this.heartRateRawData = arrayOfSHeartRateRawData;; this.heartRateRawData = null)
    {
      this.extra = paramParcel.readBundle();
      return;
      arrayOfSHeartRateRawData[i] = ((SHeartRateRawData)arrayOfParcelable[i]);
      i += 1;
      break;
    }
  }
  
      private void readFromParcel(Parcel paramParcel)
    {
      this.samplingTime = paramParcel.readLong();
      this.heartRate = paramParcel.readInt();
      this.extra = paramParcel.readBundle();
    }
    
    //find device
     * 05020000000100 find
     * 05020000000101 done
*/
