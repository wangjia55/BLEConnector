package com.jacob.ble.connector.core;

public interface BleRssiCallback {
    public void onBleRssiRead(int rssi);

    public void onBleRssiReadError(int error, String message);
}
