package com.jacob.ble.connector.core;

import android.bluetooth.BluetoothDevice;
public interface BleConnectCallback {

     void onConnectSuccess(BluetoothDevice bluetoothDevice);

     void onDeviceFound(BluetoothDevice bluetoothDevice);

     void onError(int errorCode, String reason);

}
