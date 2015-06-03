package com.jacob.ble.connector.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class BleUtils {
    public static boolean isBleSupported(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return true;
        } else {
            return false;
        }
    }
}
