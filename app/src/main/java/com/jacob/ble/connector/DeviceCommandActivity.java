package com.jacob.ble.connector;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.jacob.ble.connector.core.BleConnectCallback;
import com.jacob.ble.connector.core.BleReadCallback;
import com.jacob.ble.connector.core.ConnectState;
import com.jacob.ble.connector.logic.BleCommand;
import com.jacob.ble.connector.logic.BleManager;

import java.util.ArrayList;
import java.util.List;

public class DeviceCommandActivity extends Activity implements View.OnClickListener {

    private TextView mTextViewState;
    private Spinner mSpinnerCommand;
    private ListView mListViewDetails;

    // ble state
    private static final String STATE_CONNECTING = "Ble is connecting";
    private static final String STATE_DISCONNECTED = "Ble is disconnected";
    private static final String STATE_CONNECTED = "Ble is connected";

    //COMMAND {
    private static final String COMMAND_GET_IMEI = "Get IMEI";
    private static final String COMMAND_GET_IMEI_DATA = "gmi:";
    private static final String COMMAND_GET_IMSI = "Get IMSI";
    private static final String COMMAND_GET_IMSI_DATA = "gii:";
    private static final String COMMAND_GET_BATTERY = "Get Battery Status";
    private static final String COMMAND_GET_BATTERY_DATA = "gbi:";
    private static final String COMMAND_GET_GSM = "Get GSM Status";
    private static final String COMMAND_GET_GSM_DATA = "ggi:";
    private static final String COMMAND_GET_WIFI_MAC = "Get WIFI MAC";
    private static final String COMMAND_GET_WIFI_MAC_DATA = "gwm:";
    private static final String COMMAND_GET_BT_MAC = "Get BT MAC";
    private static final String COMMAND_GET_BT_MAC_DATA = "gbm:";
    private static final String COMMAND_REBOOT = "Reboot";
    private static final String COMMAND_REBOOT_DATA = "rb:";
    private static final String COMMAND_POWER_OFF = "Power Off";
    private static final String COMMAND_POWER_OFF_DATA = "pof:";

    private static final String COMMAND_PULL_FILE = "Pull File";
    private static final String COMMAND_PUSH_FILE = "Push File";
    private static final String COMMAND_OPEN_FILE_DATA = "fo:";
    private static final String COMMAND_SET_FILE_PATH_DATA = "fsp:";
    private static final String COMMAND_CLOSE_FILE_DATA = "fc:";
    private static final String COMMAND_FILE_READ_DATA = "fr:";
    private static final String COMMAND_FILE_WRITE_DATA = "fw:";

    private List<String> mResultString = new ArrayList<>();
    private DeviceInfo mDeviceInfo;

    private static final String[] mCommandString = {
            COMMAND_GET_IMEI,
            COMMAND_GET_IMSI,
            COMMAND_GET_BATTERY,
            COMMAND_GET_GSM,
            COMMAND_GET_WIFI_MAC,
            COMMAND_GET_BT_MAC,
            COMMAND_REBOOT,
            COMMAND_POWER_OFF,
            COMMAND_PULL_FILE,
            COMMAND_PUSH_FILE,
    };


    private BluetoothDevice mBluetoothDevice;
    private ConnectState mBleConnectState;
    private BleManager mBleManager;

    private ArrayAdapter<String> mBleCommandAdapter;

    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_command);

        mBluetoothDevice = getIntent().getParcelableExtra("BleDevice");

        mTextViewState = (TextView) findViewById(R.id.text_view_state);
        mSpinnerCommand = (Spinner) findViewById(R.id.spinner_ble_command);

        findViewById(R.id.button_send_command).setOnClickListener(this);
        mListViewDetails = (ListView) findViewById(R.id.list_view_details);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mCommandString);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCommand.setAdapter(adapter);
        mSpinnerCommand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mBleCommandAdapter = new ArrayAdapter<>(this, R.layout.layout_command_item, mResultString);
        mListViewDetails.setAdapter(mBleCommandAdapter);

        mBleManager = BleManager.getInstance();
        mBleConnectState = mBleManager.getConnectState();
        addResultString("Device Name: " + mBluetoothDevice.getName() + " - " + mBluetoothDevice.getAddress());
        showBleState(STATE_CONNECTING);

        mDeviceInfo = new DeviceInfo("900000000000001");

        mBleManager.connectDevice(mBluetoothDevice, mDeviceInfo, false, new BleConnectCallback() {
            @Override
            public void onConnectSuccess(BluetoothDevice bluetoothDevice) {
                showBleState(STATE_CONNECTED);
                //注意： 这里是向设备写一条命令，这里根据实际的情况操作
                mBleManager.writeToDevice(BleCommand.getVerifyCommand(mDeviceInfo.getImei()));
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBleManager.readData(new BleReadCallback() {

                            @Override
                            public void readDataSuccess(byte[] bytes) {
                                String msg = new String(bytes);
                                addResultString("Data: " + msg);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_send_command:
                //只有当蓝牙链接成功的时候才能发送命令
                if (mBleConnectState == ConnectState.Connected) {
                    //TODO:
                }
                break;
        }
    }
}
