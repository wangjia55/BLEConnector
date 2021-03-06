package com.jacob.ble.connector.core;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jacob.ble.connector.exception.InitException;
import com.jacob.ble.connector.utils.LogUtils;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@TargetApi(18)
public class GoogleBle implements BluetoothAdapter.LeScanCallback {

    private static final String TAG = "google gatt";
    private static final int MSG_SCAN_RESULT = 0x11223;
    private static final int MSG_DATA_RECEIVE = 0x11224;
    private static final int MSG_CONNECTION_CHECK = 0x11225;
    private static final int MSG_CONNECT_ERROR = 0x11226;
    private static final int MSG_CONNECT_SUCCESS = 0x11227;
    private static final int MSG_RSSI_READ_SUCCESS = 0x11228;
    private static final int MSG_RSSI_READ_ERROR = 0x11229;
    private static final int MSG_READ_DATA = 0x11230;
    private static final int CONNECTION_CHECK_TIME = 30 * 1000;
    private static final String PARAM_DEVICE = "device";
    private static final String PARAM_RSSI = "rssi";
    private static final String PARAM_BYTE = "byte";
    private static final String PARAM_ERROR_CODE = "code";
    private static final String PARAM_ERROR_REASON = "reason";
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private ScanState mScanState = ScanState.ScanStop;
    private ConnectState mConnectState = ConnectState.Disconnect;
    private BluetoothState mBluetoothState = BluetoothState.Bluetooth_Off;
    private BleScanCallback mBleScanCallback;
    private BleConnectCallback mBleConnectCallback;
    private BleRssiCallback mBleRssiCallback;
    private BleWriteCallback mBleWriteCallback;
    private BleReadCallback mBleReadCallback;
    private BleConnectInfo mBleConnectInfo;
    private BluetoothGattCharacteristic mReadCharacteristic;
    private BluetoothGattCharacteristic mWriteCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice mBluetoothDevice;
    private static final Queue<byte[]> sWriteQueue = new ConcurrentLinkedQueue<byte[]>();
    private static boolean sIsWriting = false;


    private BroadcastReceiver mBlueStateBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
            switch (blueState) {
                case BluetoothAdapter.STATE_OFF:
                    mBluetoothState = BluetoothState.Bluetooth_Off;
                    notifyBluetoothOff();
                    break;
                case BluetoothAdapter.STATE_ON:
                    mBluetoothState = BluetoothState.Bluetooth_On;
                    break;
                default:
                    break;
            }
        }
    };

    private void notifyBluetoothOff() {
        if (mScanState == ScanState.Scanning) {
            stopScan();
            if (mBleScanCallback != null) {
                mBleScanCallback.onError(ErrorStatus.BLUETOOTH_NO_OPEN, "bluetooth is no open");
            }
        }

        if (mConnectState != ConnectState.Disconnect) {
            disconnect();
            if (mBleConnectCallback != null) {
                mBleConnectCallback.onError(ErrorStatus.BLUETOOTH_NO_OPEN, "bluetooth is no open");
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCAN_RESULT:
                    callOnScanCallback(msg);
                    break;
                case MSG_CONNECTION_CHECK:
                    connectFail();
                    break;
                case MSG_DATA_RECEIVE:
                    callOnWriteCallback(msg);
                    break;
                case MSG_CONNECT_ERROR:
                    callOnConnectError(msg);
                    break;
                case MSG_CONNECT_SUCCESS:
                    callOnConnectSuccess();
                    break;
                case MSG_RSSI_READ_SUCCESS:
                    callOnRssiReadSuccess(msg);
                    break;
                case MSG_RSSI_READ_ERROR:
                    callOnRssiReadError(msg);
                    break;
                case MSG_READ_DATA:
                    callOnDataRead(msg);
                    break;
                default:
                    break;
            }

        }
    };

    private void callOnDataRead(Message msg) {
        if (mBleReadCallback != null){
            byte[] value = msg.getData().getByteArray(PARAM_BYTE);
            mBleReadCallback.readDataSuccess(value);
        }
    }

    private void callOnRssiReadError(Message msg) {
        if (mBleRssiCallback != null) {
            Bundle bundle = msg.getData();
            int errorCode = bundle.getInt(PARAM_ERROR_CODE);
            String reason = bundle.getString(PARAM_ERROR_REASON);
            mBleRssiCallback.onBleRssiReadError(errorCode, reason);
        }
    }

    private void callOnRssiReadSuccess(Message msg) {
        if (mBleRssiCallback != null) {
            Bundle bundle = msg.getData();
            int rssi = bundle.getInt(PARAM_RSSI);
            mBleRssiCallback.onBleRssiRead(rssi);
        }
    }

    private void callOnConnectSuccess() {
        if (mBleConnectCallback != null) {
            mBleConnectCallback.onConnectSuccess(mBluetoothDevice);
        }
    }

    private void callOnConnectError(Message msg) {
        Bundle bundle = msg.getData();
        int error = bundle.getInt(PARAM_ERROR_CODE);
        String reason = bundle.getString(PARAM_ERROR_REASON);
        if (mBleConnectCallback != null) {
            mBleConnectCallback.onError(error, reason);
        }
    }

    private void callOnScanCallback(Message msg) {
        Bundle bundle = msg.getData();
        BluetoothDevice device = bundle.getParcelable(PARAM_DEVICE);
        int rssi = bundle.getInt(PARAM_RSSI);
        byte[] bytes = bundle.getByteArray(PARAM_BYTE);
        if (mBleScanCallback != null && device != null) {
            mBleScanCallback.onLeScan(device, rssi, bytes);
        }
    }

    private void connectFail() {
        if (mConnectState != ConnectState.Connected) {
            disconnect();
            if (mBleConnectCallback != null) {
                mBleConnectCallback.onError(ErrorStatus.CONNECT_TIME_OUT, "connect time out");
            }
        }
    }

    private void callOnWriteCallback(Message msg) {
        Bundle dataReceiveBundle = msg.getData();
        byte[] rawData = dataReceiveBundle.getByteArray(PARAM_BYTE);
        if (mBleWriteCallback != null) {
            mBleWriteCallback.onWriteSuccess(rawData);
        }
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtils.LOGD(TAG, "connection state change " + "gatt status " + status + "bluetoothProfile new State " + newState);
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                LogUtils.LOGD(TAG, "connected to GATT server and discovery service");
                mBluetoothGatt.discoverServices();
            } else {
                LogUtils.LOGD(TAG, "connection state change disconnect" + "gatt status " + status + "bluetoothProfile new State " + newState);
                disconnect();
                mHandler.removeMessages(MSG_CONNECTION_CHECK);
                sendConnectErrorMessage(ErrorStatus.CONNECT_STATE_FAIL, "fail at onConnectionStateChange status " + status + " newState " + newState);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(mBleConnectInfo.getServiceUUID());
                if (service != null) {
                    LogUtils.LOGD(TAG, "service discovery ");
                    if (mBleConnectInfo.getReadCharacteristicUUID() != null) {
                        mReadCharacteristic = service.getCharacteristic(mBleConnectInfo.getReadCharacteristicUUID());
                        setCharacteristicNotification(mReadCharacteristic);
                    }
                    if (mBleConnectInfo.getWriteCharacteristicUUID() != null) {
                        mWriteCharacteristic = service.getCharacteristic(mBleConnectInfo.getWriteCharacteristicUUID());
                    }
                }
                mHandler.removeMessages(MSG_CONNECTION_CHECK);
                mConnectState = ConnectState.Connected;
                sendConnectSuccessMessage();

            } else if (status == BluetoothGatt.GATT_FAILURE) {
                LogUtils.LOGD(TAG, "onServicesDiscovered fail: " + status);
                disconnect();
                mHandler.removeMessages(MSG_CONNECTION_CHECK);
                sendConnectErrorMessage(ErrorStatus.CONNECT_STATE_FAIL, "fail at onServicesDiscovered status " + status);

            }
        }


        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            LogUtils.LOGD(TAG, "onCharacteristicWrite: " + status);
            sIsWriting = false;
            nextWrite();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            LogUtils.LOGD(TAG, "onCharacteristicRead: " + status);
            sendCharacteristicRead(characteristic.getValue());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            LogUtils.LOGD(TAG, "----------onCharacteristicChanged------------");
            sendRawData(characteristic.getValue());
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendRssiReadSuccessMessage(rssi);
            } else {
                sendRssiReadErrorMessage(status);
            }
        }
    };

    private void sendCharacteristicRead(byte[] value) {
        Message message = Message.obtain();
        message.what = MSG_READ_DATA;
        Bundle bundle = new Bundle();
        bundle.putByteArray(PARAM_BYTE, value);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private void sendRssiReadErrorMessage(int status) {
        Message message = Message.obtain();
        message.what = MSG_RSSI_READ_ERROR;
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_ERROR_CODE, ErrorStatus.GATT_FAIL);
        bundle.putString(PARAM_ERROR_REASON, "status is not gatt success, status " + status);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private void sendRssiReadSuccessMessage(int rssi) {
        Message message = Message.obtain();
        message.what = MSG_RSSI_READ_SUCCESS;
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_RSSI, rssi);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private void sendConnectSuccessMessage() {
        mHandler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
    }

    private void sendConnectErrorMessage(int error, String reason) {
        Message message = Message.obtain();
        message.what = MSG_CONNECT_ERROR;
        Bundle bundle = new Bundle();
        bundle.putInt(PARAM_ERROR_CODE, error);
        bundle.putString(PARAM_ERROR_REASON, reason);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }


    public void init(Context context) throws InitException {
        LogUtils.LOGD(TAG, "google ble init");
        mContext = context;
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            mContext.registerReceiver(mBlueStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                mBluetoothState = BluetoothState.Bluetooth_On;
            }
        }

        if (!isBleSupported()) {
            throw new InitException("Device not support ble");
        }

    }

    public void dispose() {
        stopScan();
        disconnect();
        mContext.unregisterReceiver(mBlueStateBroadcastReceiver);
        mBluetoothManager = null;
        mBluetoothGatt = null;
        mScanState = ScanState.ScanStop;
        mConnectState = ConnectState.Disconnect;
    }

    public void startScan(BleScanCallback bleScanCallback) {
        if (mBluetoothState == BluetoothState.Bluetooth_Off) {
            if (bleScanCallback != null) {
                bleScanCallback.onError(ErrorStatus.BLUETOOTH_NO_OPEN, "bluetooth is no open");
            }
            return;
        }
        if (mScanState == ScanState.Scanning) {
            if (bleScanCallback != null) {
                bleScanCallback.onError(ErrorStatus.ALREADY_SCAN, "ble is current scanning");
            }
            return;
        } else {
            mBleScanCallback = bleScanCallback;
            mScanState = ScanState.Scanning;
            mBluetoothAdapter.startLeScan(this);
        }
    }

    public void stopScan() {
        mScanState = ScanState.ScanStop;
        mBluetoothAdapter.stopLeScan(this);
    }

    public void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mConnectState = ConnectState.Disconnect;
    }

    public void connect(BluetoothDevice device, BleConnectInfo bleConnectInfo, boolean isAuto,
                        BleConnectCallback bleConnectCallback) {
        mBluetoothDevice = device;
        mBleConnectInfo = bleConnectInfo;
        mBleConnectCallback = bleConnectCallback;
        if (mBluetoothState == BluetoothState.Bluetooth_Off) {
            disconnect();
            if (mBleConnectCallback != null) {
                mBleConnectCallback.onError(ErrorStatus.BLUETOOTH_NO_OPEN, "bluetooth is no open");
            }
            return;
        }

        if (mConnectState != ConnectState.Disconnect) {
            if (mBleConnectCallback != null) {
                if (mConnectState == ConnectState.Connecting) {
                    mBleConnectCallback.onError(ErrorStatus.ALREADY_CONNECTING, "current state is connecting");
                }

                if (mConnectState == ConnectState.Connected) {
                    mBleConnectCallback.onError(ErrorStatus.ALREADY_CONNECTED, "current state is connecting");
                }
            }
            return;
        }

        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, isAuto, mGattCallback);
        mConnectState = ConnectState.Connecting;
        mHandler.sendEmptyMessageDelayed(MSG_CONNECTION_CHECK, CONNECTION_CHECK_TIME);
    }

    protected void sendRawData(byte[] value) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putByteArray(PARAM_BYTE, value);
        message.what = MSG_DATA_RECEIVE;
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);
        if (mBleConnectInfo.getCharacteristicDescriptorUUID() != null) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(mBleConnectInfo.getCharacteristicDescriptorUUID());
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }


    public void setBleWriteCallback(BleWriteCallback bleWriteCallback) {
        mBleWriteCallback = bleWriteCallback;
    }

    public synchronized void write(byte[] bytes, BleWriteCallback bleWriteCallback) {
        mBleWriteCallback = bleWriteCallback;
        if (mBluetoothState == BluetoothState.Bluetooth_Off) {
            if (mBleWriteCallback != null) {
                mBleWriteCallback.onWriteFail(ErrorStatus.BLUETOOTH_NO_OPEN, "bluetooth is no open");
            }
            return;
        }

        if (mConnectState != ConnectState.Connected) {
            if (mBleWriteCallback != null) {
                mBleWriteCallback.onWriteFail(ErrorStatus.STATE_DISCONNECT, "current state is not connected");
            }
            return;
        }

        if (sWriteQueue.isEmpty() && !sIsWriting) {
            doWrite(bytes);
        } else {
            sWriteQueue.add(bytes);
        }
    }

    private synchronized void nextWrite() {
        if (!sWriteQueue.isEmpty() && !sIsWriting) {
            doWrite(sWriteQueue.poll());
        }
    }

    private synchronized void doWrite(byte[] bytes) {
        if (mBluetoothState == BluetoothState.Bluetooth_Off) {
            if (mBleWriteCallback != null) {
                mBleWriteCallback.onWriteFail(ErrorStatus.BLUETOOTH_NO_OPEN, "bluetooth is no open");
            }
            return;
        }

        if (mConnectState != ConnectState.Connected) {
            if (mBleWriteCallback != null) {
                mBleWriteCallback.onWriteFail(ErrorStatus.STATE_DISCONNECT, "current state is not connected");
            }
            return;
        }

        if (mBluetoothGatt != null && mWriteCharacteristic != null) {
            mWriteCharacteristic.setValue(bytes);
            boolean success = mBluetoothGatt.writeCharacteristic(mWriteCharacteristic);
            LogUtils.LOGD(TAG, "success write:" + success);
        } else {
            if (mBleWriteCallback != null) {
                mBleWriteCallback.onWriteFail(ErrorStatus.GATT_NULL, "bluetooth gatt is null or write characteristic is null");
            }
            LogUtils.LOGD(TAG, "write date error. connected state: ");
        }
    }

    public void readRssi() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.readRemoteRssi();
        }
    }

    public boolean isBleSupported() {
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Message message = Message.obtain();
        message.what = MSG_SCAN_RESULT;
        Bundle bundle = new Bundle();
        bundle.putParcelable(PARAM_DEVICE, device);
        bundle.putInt(PARAM_RSSI, rssi);
        bundle.putByteArray(PARAM_BYTE, scanRecord);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    public ScanState getScanState() {
        return mScanState;
    }

    public ConnectState getConnectState() {
        return mConnectState;
    }

    public BluetoothState getBluetoothState() {
        return mBluetoothState;
    }

    public void setBleRssiCallback(BleRssiCallback bleRssiCallback) {
        this.mBleRssiCallback = bleRssiCallback;
    }

    /**
     * 从设备读取数据
     */
    public void readData(BleReadCallback bleReadCallback) {
        mBleReadCallback = bleReadCallback;
        if (mBluetoothState == BluetoothState.Bluetooth_Off) {
            if (mBleReadCallback != null) {
                mBleReadCallback.readDataFail(ErrorStatus.BLUETOOTH_NO_OPEN, "bluetooth is no open");
            }
            return;
        }

        if (mConnectState != ConnectState.Connected) {
            if (mBleReadCallback != null) {
                mBleReadCallback.readDataFail(ErrorStatus.STATE_DISCONNECT, "current state is not connected");
            }
            return;
        }


        if (mBluetoothGatt != null && mReadCharacteristic != null) {
            boolean success = mBluetoothGatt.readCharacteristic(mReadCharacteristic);
            LogUtils.LOGD(TAG, "success write:" + success);
        } else {
            if (mBleReadCallback != null) {
                mBleReadCallback.readDataFail(ErrorStatus.GATT_NULL, "bluetooth gatt is null or write characteristic is null");
            }
            LogUtils.LOGD(TAG, "write date error. connected state: ");
        }
    }
}
