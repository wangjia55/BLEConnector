package com.jacob.ble.connector.logic;

import java.util.Arrays;

public class BleCommand {
    public static byte[] getVerifyCommand(String imei) {
        return imei.getBytes();
    }

    public static byte[] getShutDownCommand(String imei) {
        byte[] bytes = imei.getBytes();
        byte[] closeCommand = Arrays.copyOf(bytes, bytes.length + 1);
        closeCommand[bytes.length] = 0x04;
        return closeCommand;
    }
}
