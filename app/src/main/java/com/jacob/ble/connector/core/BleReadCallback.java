package com.jacob.ble.connector.core;

public interface BleReadCallback {
    public void readDataSuccess(byte[] bytes);

    public void readDataFail(int errorCode, String reason);
}
