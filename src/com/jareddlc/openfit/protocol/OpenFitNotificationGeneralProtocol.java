/*
    
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
    
    linkin park
    0630000000022efffe4c0069006e006b0069006e0020005000610072006b0020002d00200047006900760065006e00200055007000
    
    06020000000105
    
    06020000000005 Prev
    06020000000007
    
    06020000000004 Next
    06020000000006
    
    daft punk
    06260000000224fffe44006100660074002000500075006e006b0020002d00200046007200650073006800
    
    genitallica
    062e000000022cfffe470065006e006900740061006c006c0069006300610020002d00200049006d006100670069006e006100

*/
