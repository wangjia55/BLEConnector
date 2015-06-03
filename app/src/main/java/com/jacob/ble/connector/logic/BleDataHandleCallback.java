package com.jacob.ble.connector.logic;

public interface BleDataHandleCallback {
    public void onDataHandle(Object o);

    public void onError(int errorCode, String message);
}
