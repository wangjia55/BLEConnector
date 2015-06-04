package com.jacob.ble.connector;

import android.bluetooth.BluetoothDevice;

import com.jacob.ble.connector.core.BleConnectInfo;

import java.util.Arrays;
import java.util.UUID;

/**
 * Package : com.jacob.ble.connector
 * Author : jacob
 * Date : 15-6-3
 * Description : 这个类是用来封装你需要链接的蓝牙设备的信息， （gatt是用过service的uuid连接到指定的设备）
 */
public class DeviceInfo implements BleConnectInfo {
    private String imei;

    public DeviceInfo(String imbt) {
        this.imei = imbt;
    }

    public String getImei() {
        return imei;
    }

    @Override
    public UUID getWriteCharacteristicUUID() {
        return UUID.fromString("00002A1A-0000-1000-8000-00805F9B34FB");
    }

    @Override
    public UUID getServiceUUID() {
        return UUID.fromString("0000110F-0000-1000-8000-00805F9B34FB");
    }

    @Override
    public UUID getReadCharacteristicUUID() {
        return UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");
    }

    @Override
    public UUID getCharacteristicDescriptorUUID() {
        return null;
    }

    @Override
    public UUID getNotificationService() {
        return null;
    }

    @Override
    public boolean shouldConnectDevice(BluetoothDevice bluetoothDevice, byte[] bytes) {

        int startIndex = 9;
        byte[] imbtBytes = Arrays.copyOfRange(bytes, startIndex, startIndex + imei.length());
        String scanImbt = new String(imbtBytes);
        if (imei.equals(scanImbt)) {
            return true;
        } else {
            return false;
        }
    }

}
