package com.jacob.ble.connector.logic;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import com.jacob.ble.connector.core.BleConnectCallback;
import com.jacob.ble.connector.core.BleConnectInfo;
import com.jacob.ble.connector.core.BleReadCallback;
import com.jacob.ble.connector.core.BleRssiCallback;
import com.jacob.ble.connector.core.BleScanCallback;
import com.jacob.ble.connector.core.BleWriteCallback;
import com.jacob.ble.connector.core.BluetoothState;
import com.jacob.ble.connector.core.ConnectState;
import com.jacob.ble.connector.core.GoogleBle;
import com.jacob.ble.connector.core.ScanState;
import com.jacob.ble.connector.exception.InitException;
import com.jacob.ble.connector.utils.LogUtils;


public class BleManager {
    private static BleManager mBleManager;
    private GoogleBle mGoogleBle = new GoogleBle();
    private BleConnectCallback mBleConnectCallback;
    private BleConnectInfo mBleConnectInfo;
    private boolean mIsAuto;
    private ScanType mCurrentScanType;

    private BleScanCallback mBleScanCallback = new BleScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            switch (mCurrentScanType) {
                case DEVICE:
                    connectIfDeviceFound(device, scanRecord);
                    break;
            }

        }

        @Override
        public void onError(int errorCode, String message) {
            mGoogleBle.stopScan();
            switch (mCurrentScanType) {
                case DEVICE:
                    if (mBleConnectCallback != null) {
                        mBleConnectCallback.onError(errorCode, message);
                    }
                    break;
            }
        }
    };


    private void connectIfDeviceFound(BluetoothDevice device, byte[] broadcast) {
        LogUtils.LOGD("device find ", " " + device.getName());
        if (mBleConnectInfo != null && mBleConnectInfo.shouldConnectDevice(device, broadcast)) {
            mBleConnectCallback.onDeviceFound(device);
            LogUtils.LOGD("device connect ", " " + device.getName());
            mGoogleBle.stopScan();
            mGoogleBle.connect(device, mBleConnectInfo, mIsAuto, mBleConnectCallback);
        }
    }


    private BleWriteCallback mBleDataReceiveCallback = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(byte[] bytes) {
            LogUtils.LOGD("ble", "write success");
        }

        @Override
        public void onWriteFail(int errorCode, String errorReason) {
            LogUtils.LOGD("ble", "write error errorCode " + errorCode + " errorReason " + errorReason);
        }
    };


    private BleManager() {
    }

    public static BleManager getInstance() {
        if (mBleManager == null) {
            mBleManager = new BleManager();
        }

        return mBleManager;
    }

    public void init(Context context) throws InitException {
        mGoogleBle.init(context);
    }

    public void dispose() {
        mGoogleBle.dispose();
    }


    /**
     * 根据给出的设备信息，直接连接某个固定的ble设备
     * 这个方法是将扫描设备，到连接设备全部封装了
     */
    public void scanAndConnectDevice(BleConnectInfo bleConnectInfo, boolean isAuto, BleConnectCallback bleDeviceFoundCallback) {
        if (mGoogleBle.getScanState() != ScanState.Scanning) {
            mCurrentScanType = ScanType.DEVICE;
            mBleConnectCallback = bleDeviceFoundCallback;
            mIsAuto = isAuto;
            mBleConnectInfo = bleConnectInfo;
            mGoogleBle.startScan(mBleScanCallback);
        }
    }

    /**
     * 连接设备
     */
    public void connectDevice(BluetoothDevice device, BleConnectInfo bleConnectInfo,
                              boolean isAuto, BleConnectCallback bleDeviceFoundCallback) {
        if (mGoogleBle.getScanState() != ScanState.Scanning) {
            mBleConnectCallback = bleDeviceFoundCallback;
            mIsAuto = isAuto;
            mBleConnectInfo = bleConnectInfo;
            mGoogleBle.stopScan();
            mGoogleBle.connect(device, mBleConnectInfo, mIsAuto, mBleConnectCallback);
        }

    }


    /**
     * 扫描ble设备，仅仅只做扫描的操作，不进行连接的功能
     */
    public void scanDevice(BleScanCallback scanCallback) {
        if (mGoogleBle.getScanState() != ScanState.Scanning) {
            mCurrentScanType = ScanType.DEVICE;
            mIsAuto = false;
            mGoogleBle.startScan(scanCallback);
        }
    }

    public void writeToDevice(byte[] bytes) {
        mGoogleBle.write(bytes, mBleDataReceiveCallback);
    }


    public void readData(BleReadCallback bleReadCallback) {
        mGoogleBle.readData(bleReadCallback);
    }

    public void readRssi() {
        mGoogleBle.readRssi();
    }


    public void stopScan() {
        mGoogleBle.stopScan();
    }


    public void disconnect() {
        mGoogleBle.disconnect();
    }


    /**
     * 返回当前蓝牙的开启状态
     */
    public BluetoothState getCurrentBluetoothState() {
        return mGoogleBle.getBluetoothState();
    }

    /**
     * 返回当前蓝牙连接的状态
     */
    public ConnectState getConnectState() {
        return mGoogleBle.getConnectState();
    }

    public void setBleRssiCallback(BleRssiCallback bleRssiCallback) {
        mGoogleBle.setBleRssiCallback(bleRssiCallback);
    }

}
