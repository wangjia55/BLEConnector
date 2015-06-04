package com.jacob.ble.connector;

import android.app.Application;
import android.widget.Toast;

import com.jacob.ble.connector.exception.InitException;
import com.jacob.ble.connector.logic.BleManager;
import com.jacob.ble.connector.utils.BleUtils;

/**
 * Package : com.jacob.ble.connector
 * Author : jacob
 * Date : 15-6-3
 */
public class BLEApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BleUtils.isBleSupported(this)) {
            try {
                BleManager.getInstance().init(this);
            } catch (InitException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Not Support BLE", Toast.LENGTH_LONG).show();
        }
    }
}
