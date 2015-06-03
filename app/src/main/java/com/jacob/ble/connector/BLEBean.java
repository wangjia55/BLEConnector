package com.jacob.ble.connector;

import android.bluetooth.BluetoothDevice;

/**
 * Package : com.jacob.ble.connector
 * Author : jacob
 * Date : 15-6-3
 * Description : 这个类是用来xxx
 */
public class BLEBean {

    private BluetoothDevice bluetoothDevice;

    private int rssi;

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
