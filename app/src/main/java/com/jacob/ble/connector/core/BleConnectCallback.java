package com.jacob.ble.connector.core;

import android.bluetooth.BluetoothDevice;
public interface BleConnectCallback {

    public void onConnectSuccess(BluetoothDevice bluetoothDevice);

    public void onDeviceFound(BluetoothDevice bluetoothDevice);

    public void onError(int errorCode, String reason);

}
