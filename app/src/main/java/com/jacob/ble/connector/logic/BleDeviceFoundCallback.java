package com.jacob.ble.connector.logic;

import android.bluetooth.BluetoothDevice;

import java.util.List;

public interface BleDeviceFoundCallback {
    public void onManyDeviceFound(List<BluetoothDevice> bluetoothDevices);

    public void onOneDeviceFound(BluetoothDevice bluetoothDevices);

    public void onDeviceConnected(BluetoothDevice bluetoothDevice);

    public void onDeviceNoFound();

    public void onError(int errorCode, String message);
}
