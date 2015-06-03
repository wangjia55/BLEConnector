package com.jacob.ble.connector;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;


public class DeviceScanActivity extends FragmentActivity implements View.OnClickListener {

    private Button mScanDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);
        mScanDevice = (Button) findViewById(R.id.button_scan_device);
        mScanDevice.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_scan_device:

                break;
        }
    }
}

