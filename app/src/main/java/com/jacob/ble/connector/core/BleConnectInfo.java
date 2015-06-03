package com.jacob.ble.connector.core;

import android.bluetooth.BluetoothDevice;

import java.util.UUID;

public interface BleConnectInfo {

    public UUID getWriteCharacteristicUUID();

    public UUID getServiceUUID();

    public UUID getReadCharacteristicUUID();

    public UUID getCharacteristicDescriptorUUID();

    public UUID getNotificationService();

    public boolean shouldConnectDevice(BluetoothDevice bluetoothDevice, byte[] bytes);


}
