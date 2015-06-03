package com.jacob.ble.connector;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;


public class DeviceScanActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);

        findViewById(R.id.button_scan_device).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_scan_device:

                break;
        }
    }
}

