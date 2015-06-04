package com.jacob.ble.connector;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.jacob.ble.connector.core.BleScanCallback;
import com.jacob.ble.connector.logic.BleManager;
import com.jacob.ble.connector.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;


public class DeviceScanActivity extends FragmentActivity implements View.OnClickListener, BleScanCallback {
    public static final String TAG = "DeviceScanActivity";
    private static final int REQUEST_START_BLE = 10;
    private static final int MSG_STOP_SCAN = 100;
    private ListView mListView;
    private Button mButtonScanDevice;
    private DeviceAdapter mDeviceAdapter;

    private DeviceInfo mDeivceInfo;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BLEBean> mListBle = new ArrayList<>();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_STOP_SCAN:
                    BleManager.getInstance().stopScan();
                    mButtonScanDevice.setEnabled(true);
                    break;
            }
        }
    };

    private BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButtonScanDevice.setEnabled(true);
                        }
                    });

                    LogUtils.LOGE(TAG, "BluetoothAdapter.STATE_ON");
                    break;
                case BluetoothAdapter.STATE_OFF:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mButtonScanDevice.setEnabled(false);
                        }
                    });
                    LogUtils.LOGE(TAG, "BluetoothAdapter.STATE_OFF");
                    break;
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_device);
        mButtonScanDevice = (Button) findViewById(R.id.button_send_command);
        mButtonScanDevice.setOnClickListener(this);

        mListView = (ListView) findViewById(R.id.list_view_device);
        mDeviceAdapter = new DeviceAdapter(getApplicationContext(), mListBle);
        mListView.setAdapter(mDeviceAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DeviceScanActivity.this, DeviceCommandActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("BleDevice", mDeviceAdapter.getDevice(position).getBluetoothDevice());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        // 优先判断设备是否支持ble， 再次判断ble是否开关， 如果没有打开就请求开启
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Not Support BLE", Toast.LENGTH_LONG).show();
            return;
        } else {
            if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intent, REQUEST_START_BLE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //注册ble 状态的广播，如果蓝牙 开／关 都会收到广播
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBleReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mBleReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_send_command:
                mListBle.clear();
                mDeviceAdapter.notifyDataSetChanged();
                mDeivceInfo = new DeviceInfo("900000000000001");

                //这里扫描10s钟， 10s后停止扫描
                BleManager.getInstance().scanDevice(this);
                mHandler.sendEmptyMessageDelayed(MSG_STOP_SCAN, 10000);

                mButtonScanDevice.setEnabled(false);
                break;
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        BLEBean bleBean = new BLEBean();
        bleBean.setBluetoothDevice(device);
        bleBean.setRssi(rssi);
        mListBle.add(bleBean);
        mDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onError(int errorCode, String message) {
        LogUtils.LOGE(TAG, message);
    }


    /**
     * 开启ble的绘制
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_START_BLE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "同意开启 BLE", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "拒绝开启 BLE", Toast.LENGTH_LONG).show();
            }
        }

    }
}

