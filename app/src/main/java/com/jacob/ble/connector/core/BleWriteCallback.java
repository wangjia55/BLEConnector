package com.jacob.ble.connector.core;

public interface BleWriteCallback {
    public void onWriteSuccess(byte[] bytes);

    public void onWriteFail(int errorCode, String reason);
}
