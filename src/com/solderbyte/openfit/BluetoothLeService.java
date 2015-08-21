package com.solderbyte.openfit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class BluetoothLeService extends Service {
    private static final String LOG_TAG = "OpenFit:BluetoothLeService";

    private static Handler mHandler;
    private static String mDeviceMac;
    private String mBluetoothDeviceAddress;
    public static InputStream mInStream;
    public static OutputStream mOutStream;
    public static CharSequence[] pairedEntries;
    public static CharSequence[] pairedEntryValues;
    public static CharSequence[] scannedEntries;
    public static CharSequence[] scannedEntryValues;
    private static EnableBluetoothThread eBluetooth;
    private static BluetoothGatt mBluetoothGatt;
    private static BluetoothSocket mBluetoothSocket;
    private static BluetoothDevice mBluetoothDevice;
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothManager mBluetoothManager;
    private static Set<BluetoothDevice> pairedDevices;
    private static Set<BluetoothDevice> scannedDevices;
    private static BluetoothServerSocket mBluetoothServerSocket;
    public static BluetoothGattCharacteristic mWriteCharacteristic;

    public int mConnectionState = 0;
    public static boolean isEnabled = false;
    public static boolean isConnected = false;
    public static boolean isScanning = false;
    public static volatile boolean isThreadRunning = false;

    private static onConnectThread onconnect;
    private static ConnectThread connect;

    private static final int STATE_FORCE = 3;
    private static final long SCAN_PERIOD = 5000;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_DISCONNECTED = 0;
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";
    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    private static final UUID MY_UUID_SECURE = UUID.fromString("9c86c750-870d-11e3-baa7-0800200c9a66");
    public static String[] gattStatus = {"Success", "Failure"};
    public static String[] gattState = {"Disconnected", "Connecting", "Connected", "Disconnecting"};
    public static String[] gattServiceType = {"Primary", "Secondary"};
    //00001801-0000-1000-8000-00805f9b34fb

    // bluetoothle callback
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(LOG_TAG, "BluetoothLe onConnectionStateChange: "+status);

            if(newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(LOG_TAG, "BluetoothLe Connected to GATT: status:"+status+", state: "+gattState[newState]);
                isConnected = true;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);
                if(mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("bluetooth", "isConnected");
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
                // attempts to discover services after successful connection.
                Log.d(LOG_TAG, "Starting discoverServices");
                mBluetoothGatt.discoverServices();
            }
            else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(LOG_TAG, "BluetoothLe Disconnected from GATT");
                isConnected = false;
                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
                if(mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("bluetooth", "isDisconnected");
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(LOG_TAG, "onServicesDiscovered: "+status);
            if(status == BluetoothGatt.GATT_SUCCESS) {    
                // loops through available GATT Services.
                for(BluetoothGattService gattService : gatt.getServices()) {
                    String uuid = gattService.getUuid().toString();
                    String type = gattServiceType[gattService.getType()];

                    Log.d(LOG_TAG, "onServicesDiscovered type: "+type);
                    Log.d(LOG_TAG, "onServicesDiscovered uuid: "+uuid);
                    //Log.d(LOG_TAG, "onServicesDiscovered: getCharacteristic: "+mWriteCharacteristic);
                    for(BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                        String cUuid = gattCharacteristic.getUuid().toString();
                        int cInstanceId = gattCharacteristic.getInstanceId();
                        int cPermissions = gattCharacteristic.getPermissions();
                        int cProperties = gattCharacteristic.getProperties();
                        byte[] cValue = gattCharacteristic.getValue();
                        int cWriteType = gattCharacteristic.getWriteType();

                        Log.d(LOG_TAG, "onServicesDiscovered cUuid: "+cUuid);
                        Log.d(LOG_TAG, "onServicesDiscovered cInstanceId: "+cInstanceId);
                        Log.d(LOG_TAG, "onServicesDiscovered cPermissions: "+cPermissions);
                        Log.d(LOG_TAG, "onServicesDiscovered cProperties: "+cProperties);
                        Log.d(LOG_TAG, "onServicesDiscovered cValue: "+cValue);
                        Log.d(LOG_TAG, "onServicesDiscovered cWriteType: "+cWriteType);
                    }
                }
                Log.d(LOG_TAG, "BluetoothLe Service discovered: "+status);
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            }
            else {
                Log.d(LOG_TAG, "BluetoothLe onServicesDiscovered received: "+status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(LOG_TAG, "BluetoothLe onCharacteristicRead received: "+status);
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(LOG_TAG, "BluetoothLe onCharacteristicChanged received: "+characteristic);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Log.d(LOG_TAG, "BluetoothLe broadcastUpdate received: "+characteristic);
        final Intent intent = new Intent(action);
        // for all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if(data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
            }
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            Log.d(LOG_TAG, "getService");
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "BluetoothLe onBind.");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        close();
        Log.d(LOG_TAG, "unbind.");
        mHandler = null;
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        Log.d(LOG_TAG, "BLE Initialize.");
        if(mBluetoothManager == null) {
            Log.d(LOG_TAG, "Initialize BluetoothManager.");
            mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if(mBluetoothManager == null) {
                Log.e(LOG_TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        scannedDevices = new LinkedHashSet<BluetoothDevice>();
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null) {
            Log.e(LOG_TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if(mBluetoothAdapter.isEnabled()) {
            isEnabled = true;
        }
        else {
            Log.d(LOG_TAG, "Bluetooth is not enabled.");
            isEnabled = false;
        }
        return true;
    }

    public boolean connect(final String address) {
        Log.d(LOG_TAG, "BLE connect: "+address);
        if(mBluetoothAdapter == null || address == null) {
            Log.d(LOG_TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if(mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(LOG_TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if(mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                forceConnect();
                return true;
            }
            else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.d(LOG_TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // auto connect to the device
        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        Log.d(LOG_TAG, "Trying to create a new connection to: "+address);
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        Log.d(LOG_TAG, "BLE disconnect");
        if(mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void forceConnect() {
        if(mBluetoothDeviceAddress != null) {
            Log.d(LOG_TAG, "Trying to force connection: "+mBluetoothDeviceAddress);
            mConnectionState = STATE_FORCE;
            mBluetoothGatt.disconnect();
            final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
            mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
        }
        else {
            Log.d(LOG_TAG, "Force connect called without previous connection");
        }
    }

    public void close() {
        Log.d(LOG_TAG, "BLE close");
        if(mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        /*if(onconnect != null) {
            onconnect.close();
            onconnect = null;
        }*/
        if(connect != null) {
            connect.close();
            connect = null;
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if(mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if(mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(LOG_TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if(mBluetoothGatt == null) {
            Log.w(LOG_TAG, "getSupportedGattServices mBluetoothGatt not initialized");
            return null;
        }
        Log.d(LOG_TAG, "getSupportedGattServices");
        return mBluetoothGatt.getServices();
    }

    /* Helper Functions */
    public void setHandler(Handler mHndlr) {
        Log.d(LOG_TAG, "Setting handler");
        mHandler = mHndlr;
    }

    public void connectRfcomm() {
        if(!mBluetoothAdapter.isEnabled()) {
            Log.d(LOG_TAG, "connect called when Bluetooth is not enabled.");
            return;
        }
        if(mBluetoothDevice != null) {
            connect = new ConnectThread();
            connect.start();
            Log.d(LOG_TAG, "Connecting to Rfcomm");
        }
        else {
            Log.d(LOG_TAG, "connectRfcomm called mBluetoothDevice is null");
        }
    }

    public void disconnectRfcomm() {
        if(mBluetoothDevice != null && isConnected) {
            connect.close();
            Log.d(LOG_TAG, "closing connectRmcomm");
        }
        else {
            Log.d(LOG_TAG, "disconnectRfcomm called while not connected");
        }
    }

    public void enableBluetooth() {
        if(!mBluetoothAdapter.isEnabled()) {
            eBluetooth = new EnableBluetoothThread();
            eBluetooth.start();
        }
        else {
            Log.d(LOG_TAG, "enableBluetooth called when BT enabled");
        }
    }

    public void disableBluetooth() {
        mBluetoothAdapter.disable();
        isEnabled = false;
    }

    public void setDevice(String devMac) {
        mDeviceMac = devMac;
        setBluetoothDevice();
    }

    public static void setBluetoothDevice() {
        // loop through devices
        if(pairedDevices != null) {
            for(BluetoothDevice device : pairedDevices) {
                if(device.getAddress().equals(mDeviceMac)) {
                    Log.d(LOG_TAG, "Set paired device: "+device.getName()+":"+device.getAddress());
                    mBluetoothDevice = device;
                }
            }
        }
        else if(scannedDevices.size() > 0) {
            for(BluetoothDevice device : scannedDevices) {
                if(device.getAddress().equals(mDeviceMac)) {
                    Log.d(LOG_TAG, "Set scanned device: "+device.getName()+":"+device.getAddress());
                    mBluetoothDevice = device;
                }
            }
        }
        else {
            Log.d(LOG_TAG, "setBluetoothDevice called with empty devices");
        }
    }

    public void setEntries() {
        if(isEnabled) {
            List<CharSequence> entries = new ArrayList<CharSequence>();
            List<CharSequence> values = new ArrayList<CharSequence>();
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            // loop through paired devices
            if(pairedDevices.size() > 0) {
                for(BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceAddr = device.getAddress();
                    Log.d(LOG_TAG, "Paired Device: "+deviceName+":"+deviceAddr);
                    if(deviceName != null && !deviceName.isEmpty() && deviceAddr != null && !deviceAddr.isEmpty()) {
                        entries.add(deviceName);
                        values.add(deviceAddr);
                    }
                }
            }
            else {
                Log.d(LOG_TAG, "No pairedDevices");
            }
            // loop trough scanned devices
            if(scannedDevices.size() > 0) {
                for(BluetoothDevice device : scannedDevices) {
                    // make sure we dont add duplicates
                    if(!entries.contains(device.getName())) {
                        String deviceName = device.getName();
                        String deviceAddr = device.getAddress();
                        Log.d(LOG_TAG, "Scanned Device: "+deviceName+":"+deviceAddr);
                        if(deviceName != null && !deviceName.isEmpty() && deviceAddr != null && !deviceAddr.isEmpty()) {
                            entries.add(deviceName);
                            values.add(deviceAddr);
                        }
                    }
                }
            }
            else {
                Log.d(LOG_TAG, "No scannedDevices");
            }
            
            pairedEntries = entries.toArray(new CharSequence[entries.size()]);
            pairedEntryValues = values.toArray(new CharSequence[values.size()]);

            Message msg = mHandler.obtainMessage();
            Bundle b = new Bundle();
            b.putString("bluetoothDevicesList", "bluetoothDevicesList");
            b.putCharSequenceArray("bluetoothEntries", pairedEntries);
            b.putCharSequenceArray("bluetoothEntryValues", pairedEntryValues);
            msg.setData(b);
            mHandler.sendMessage(msg);
        }
        else {
            Log.d(LOG_TAG, "setEntries called without BT enabled");
        }
    }

    public static CharSequence[] getEntries() {
        if(pairedEntries != null && pairedEntries.length > 0) {
            return pairedEntries;
        }
        else {
            CharSequence[] entries = {"No Devices"};
            return entries;
        }
    }

    public static CharSequence[] getEntryValues() {
        if(pairedEntryValues!= null && pairedEntryValues.length > 0) {
            return pairedEntryValues;
        }
        else {
            CharSequence[] entryValues = {"None"};
            return entryValues;
        }
    }

    @SuppressWarnings("deprecation")
    public void scanLeDevice() {
        Log.d(LOG_TAG, "scanLeDevice");
        if(isEnabled) {
            if(mHandler != null) {
                if(!isScanning) {
                    /*if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                        Log.d(LOG_TAG, "scanning with startLeScan");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isScanning = false;
                                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                Message msg = mHandler.obtainMessage();
                                Bundle b = new Bundle();
                                b.putString("bluetooth", "scanStopped");
                                msg.setData(b);
                                mHandler.sendMessage(msg);
                                setEntries();
                            }
                        }, SCAN_PERIOD);
                        isScanning = true;
                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                    }
                    
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Log.d(LOG_TAG, "scanning with startScan");
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isScanning = false;
                                BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                                mBluetoothLeScanner.stopScan(mScanCallback);
                                Message msg = mHandler.obtainMessage();
                                Bundle b = new Bundle();
                                b.putString("bluetooth", "scanStopped");
                                msg.setData(b);
                                mHandler.sendMessage(msg);
                                setEntries();
                            }
                        }, SCAN_PERIOD);
                        
                        isScanning = true;
                        BluetoothLeScanner mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                        mBluetoothLeScanner.startScan(mScanCallback);
                    }*/
                    // Stops scanning after a pre-defined scan period.
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isScanning = false;
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            Message msg = mHandler.obtainMessage();
                            Bundle b = new Bundle();
                            b.putString("bluetooth", "scanStopped");
                            msg.setData(b);
                            mHandler.sendMessage(msg);
                            setEntries();
                        }
                    }, SCAN_PERIOD);
                    
                    Log.d(LOG_TAG, "scanLeDevice starting scan for: "+SCAN_PERIOD+"ms");
                    isScanning = true;
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
                else {
                    Log.d(LOG_TAG, "scanLeDevice currently scanning");
                }
            }
            else{
                Log.d(LOG_TAG, "scanLeDevice no mHandler");
            }
        }
        else {
            Log.d(LOG_TAG, "scanLeDevice called without BT enabled");
        }
    }
    
    public void write(byte[] bytes) {
        if(onconnect != null) {
            Log.d(LOG_TAG, "Writting bytes");
            onconnect.write(bytes);
        }
        else {
            Log.d(LOG_TAG, "write called without BT connected");
        }
    }

    private LeScanCallback mLeScanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if(scannedDevices.add(device)) {
                Log.d(LOG_TAG, device.getName()+" : "+device.getAddress()+" : "+device.getType()+" : "+device.getBondState());
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("bluetoothDevice", device.getName()+","+device.getAddress());
                msg.setData(b);
                mHandler.sendMessage(msg);
                setEntries();
            }
        }
    };

    /*private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if(scannedDevices.add(device)) {
                Log.d(LOG_TAG, device.getName()+" : "+device.getAddress()+" : "+device.getType()+" : "+device.getBondState());
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("bluetoothDevice", device.getName()+","+device.getAddress());
                msg.setData(b);
                mHandler.sendMessage(msg);
                setEntries();
            }
            Log.d(LOG_TAG, device.getName()+" : "+device.getAddress()+" : "+device.getType()+" : "+device.getBondState());
        }
    };*/

    private class EnableBluetoothThread extends Thread {
        public void run() {
            boolean bluetoothEnabled = true;
            long timeStart = Calendar.getInstance().getTimeInMillis();
            Log.d(LOG_TAG, "Enabling Bluetooth: "+timeStart);

            mBluetoothAdapter.enable();
            while(!mBluetoothAdapter.isEnabled()) {
                try
                {
                    long timeDiff =  Calendar.getInstance().getTimeInMillis() - timeStart;
                    if(timeDiff >= 5000) {
                        bluetoothEnabled = false;
                        break;
                    }
                    Thread.sleep(100L);
                }
                catch (InterruptedException ie)
                {
                    // unexpected interruption while enabling bluetooth
                    Thread.currentThread().interrupt(); // restore interrupted flag
                    return;
                }
            }
            if(bluetoothEnabled) {
                isEnabled = true;
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("bluetooth", "isEnabled");
                msg.setData(b);
                mHandler.sendMessage(msg);
                Log.d(LOG_TAG, "Enabled");
            }
            else {
                Message msg = mHandler.obtainMessage();
                Bundle b = new Bundle();
                b.putString("bluetooth", "isEnabledFailed");
                msg.setData(b);
                mHandler.sendMessage(msg);
                Log.d(LOG_TAG, "Enabling Bluetooth timed out");
            }
        }
    }

    private class ConnectThread extends Thread {
        public ConnectThread() {
            Log.d(LOG_TAG, "Initializing ConnectThread");

            // get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                Log.d(LOG_TAG, "try ConnectThread: "+mBluetoothDevice.getName()+" with UUID: "+MY_UUID_SECURE.toString());
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            }
            catch(Exception e) {
                Log.e(LOG_TAG, "Error: mBluetoothDevice.createRfcommSocketToServiceRecord", e);
            }
        }

        public void run() {
            Log.d(LOG_TAG, "Running ConnectThread");
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // connect the device through the socket. This will block until it succeeds or throws an exception
                mBluetoothSocket.connect();
                isConnected = true;
                if(mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("bluetooth", "isConnectedRfcomm");
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
            catch(IOException connectException) {
                Log.e(LOG_TAG, "Error: mBluetoothSocket.connect()", connectException);
                try {
                    mBluetoothSocket.close();
                    if(mHandler != null) {
                        Message msg = mHandler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("bluetooth", "isConnectedRfcommFailed");
                        msg.setData(b);
                        mHandler.sendMessage(msg);
                    }
                }
                catch (IOException closeException) {
                    Log.e(LOG_TAG, "Error: mBluetoothSocket.close()", closeException);
                }
                return;
            }
            Log.d(LOG_TAG, "ConnectThread connected");
            // Do work to manage the connection
            onconnect = new onConnectThread();
            onconnect.start();
        }

        public void close() {
            if(onconnect != null) {
                isThreadRunning = false;
                onconnect.close();
                isConnected = false;
                Log.d(LOG_TAG, "Bluetooth rfComm Disconnected");
                if(mHandler != null) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("bluetooth", "isDisconnectedRfComm");
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    public class onConnectThread extends Thread {
        public onConnectThread() {
            Log.d(LOG_TAG, "Initializing onConnectThread");

            // get the input and output streams
            try {
                mInStream = mBluetoothSocket.getInputStream();
                mOutStream = mBluetoothSocket.getOutputStream();
                isThreadRunning = true;
            }
            catch(IOException e) {
                close();
                Log.e(LOG_TAG, "Error: mBluetoothSocket.getInputStream()/socket.getOutputStream()", e);
            }
        }

        public void run() {
            Log.d(LOG_TAG, "Running onConnectThread");
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            // listen to the InputStream
            while(isThreadRunning) {
                try {
                    int bytes = mInStream.read(buffer);
                    byteArray.write(buffer, 0, bytes);
                    Log.d(LOG_TAG, "Received: "+byteArray);
                    try {
                        Message msg = mHandler.obtainMessage();
                        Bundle b = new Bundle();
                        b.putString("bluetoothData", "bluetoothData");
                        b.putByteArray("data", byteArray.toByteArray());
                        msg.setData(b);
                        mHandler.sendMessage(msg);
                    }
                    catch(Exception e) {
                        Log.e(LOG_TAG, "Error: mHandler.obtainMessage()", e);
                    }
                    
                    byteArray.reset();
                }
                catch (IOException e) {
                    if(isThreadRunning) {
                        Log.e(LOG_TAG, "Error: mInStream.read()", e);
                        close();
                        onconnect.close();
                        if(connect != null) {
                            connect.close();
                            connect = null;
                        }
                    }
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                byteArray.write(bytes, 0, bytes.length);
                Log.d(LOG_TAG, "Sending: "+byteArray);
                mOutStream.write(bytes);
                mOutStream.flush();
            }
            catch(IOException e) {
                Log.e(LOG_TAG, "Error: mOutStream.write()", e);
            }
        }

        public void close() {
            try {
                mInStream.close();
            }
            catch(IOException e) {
                Log.e(LOG_TAG, "Error: mInStream.close()", e);
            }
            try {
                mOutStream.close();
            }
            catch(IOException e) {
                Log.e(LOG_TAG, "Error: mOutStream.close()", e);
            }
            try {
                mBluetoothSocket.close();
            }
            catch(IOException e) {
                Log.e(LOG_TAG, "Error: mBluetoothSocket.close()", e);
            }
        }
    }

    @SuppressWarnings("unused")
    private class ServerThread extends Thread {
        public ServerThread() {
            Log.d(LOG_TAG, "Initializing ServerThread");

            try {
                Log.d(LOG_TAG, "try ServerThread with UUID: "+MY_UUID_SECURE);
                mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("SessionManagerSecure", MY_UUID_SECURE);
            } catch (IOException e1) {
                Log.e(LOG_TAG, "Error listenUsingRfcommWithServiceRecord");
                e1.printStackTrace();
            }
        }

        public void run() {
            Log.d(LOG_TAG, "Running ServerThread");

            try {
                mBluetoothServerSocket.accept();
                Log.d(LOG_TAG, "mBluetoothServerSocket.accept() success");
            } catch (IOException e1) {
                Log.e(LOG_TAG, "Error mBluetoothServerSocket.accept()");
                e1.printStackTrace();
            }
        }

        public void close() {
            try {
                mBluetoothServerSocket.close();
            }
            catch (IOException e) {
                Log.e(LOG_TAG, "Error: mmSocket.close()", e);
            }
        }
    }
}
