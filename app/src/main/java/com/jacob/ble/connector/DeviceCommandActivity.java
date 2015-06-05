package com.jacob.ble.connector;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jacob.ble.connector.core.BleConnectCallback;
import com.jacob.ble.connector.core.BleReadCallback;
import com.jacob.ble.connector.logic.BleCommand;
import com.jacob.ble.connector.logic.BleManager;

import java.util.ArrayList;
import java.util.List;

public class DeviceCommandActivity extends Activity {
    private TextView mTextViewState;
    private ListView mListViewDetails;

    // ble state
    private static final String STATE_CONNECTING = "Ble is connecting";
    private static final String STATE_DISCONNECTED = "Ble is disconnected";
    private static final String STATE_CONNECTED = "Ble is connected";

    private List<String> mResultString = new ArrayList<>();
    private DeviceInfo mDeviceInfo;

    private BluetoothDevice mBluetoothDevice;
    private BleManager mBleManager;

    private ArrayAdapter<String> mBleCommandAdapter;

    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_command);
        mBluetoothDevice = getIntent().getParcelableExtra("BleDevice");

        mTextViewState = (TextView) findViewById(R.id.text_view_state);
        mListViewDetails = (ListView) findViewById(R.id.list_view_details);
        mBleCommandAdapter = new ArrayAdapter<>(this, R.layout.layout_command_item, mResultString);
        mListViewDetails.setAdapter(mBleCommandAdapter);

        mBleManager = BleManager.getInstance();
        addResultString("Device Name: " + mBluetoothDevice.getName() + " - " + mBluetoothDevice.getAddress());
        showBleState(STATE_CONNECTING);

        mDeviceInfo = new DeviceInfo("90000000000000102");
        mBleManager.connectDevice(mBluetoothDevice, mDeviceInfo, false, new BleConnectCallback() {
            @Override
            public void onConnectSuccess(BluetoothDevice bluetoothDevice) {
                showBleState(STATE_CONNECTED);
                //注意： 这里是向设备写一条命令，这里根据实际的情况操作
                mBleManager.writeToDevice(BleCommand.getVerifyCommand(mDeviceInfo.getImei()));
                addResultString("Send Data : " + mDeviceInfo.getImei());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBleManager.readData(new BleReadCallback() {

                            @Override
                            public void readDataSuccess(byte[] bytes) {
                                String msg = new String(bytes);
                                addResultString("Receive Data: " + msg);
                            }

                            @Override
                            public void readDataFail(int errorCode, String reason) {

                            }
                        });
                    }
                }, 1000);
            }

            @Override
            public void onDeviceFound(BluetoothDevice bluetoothDevice) {
                showBleState(STATE_CONNECTING);
            }

            @Override
            public void onError(int errorCode, String reason) {
                showBleState(STATE_DISCONNECTED);
            }
        });
    }

    /**
     * 将每次获取的返回值都添加在adapter中,使用listview展示结果数据
     */
    private void addResultString(String message) {
        mResultString.add(message);
        mBleCommandAdapter.notifyDataSetChanged();
    }

    /**
     * 显示当前蓝牙的连接状态
     */
    private void showBleState(String state) {
        mTextViewState.setText(state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBleManager != null) {
            mBleManager.disconnect();
            mBleManager.dispose();
        }
    }
}
