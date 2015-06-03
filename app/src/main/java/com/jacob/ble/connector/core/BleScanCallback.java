package com.jacob.ble.connector.core;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

@SuppressLint("NewApi")
public interface BleScanCallback extends BluetoothAdapter.LeScanCallback {
    @Override
    void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord);

    void onError(int errorCode, String message);
}
