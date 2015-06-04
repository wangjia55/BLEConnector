package com.jacob.ble.connector;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {


    private List<BLEBean> deviceList = new ArrayList<>();

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public DeviceAdapter(Context context, List<BLEBean> deviceList) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.deviceList = deviceList;
    }


    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public BLEBean getItem(int position) {
        return deviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.layout_device_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.textViewName = (TextView) convertView.findViewById(R.id.text_view_name);
            viewHolder.textViewRssi = (TextView) convertView.findViewById(R.id.text_view_rssi);
            viewHolder.textViewMacAddress = (TextView) convertView.findViewById(R.id.text_view_mac_address);

            convertView.setTag(viewHolder);
        }
        initializeViews(getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(BLEBean device, ViewHolder holder) {
        String name = device.getBluetoothDevice().getName();
        name = (name == null) ? "Unkown" : name;
        holder.textViewName.setText(name);
        holder.textViewMacAddress.setText(device.getBluetoothDevice().getAddress());
        holder.textViewRssi.setText("RSSI:" + device.getRssi());
    }

    protected class ViewHolder {
        private TextView textViewName;
        private TextView textViewRssi;
        private TextView textViewMacAddress;
    }
}


