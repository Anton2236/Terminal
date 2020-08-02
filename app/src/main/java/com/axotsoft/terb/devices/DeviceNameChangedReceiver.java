package com.axotsoft.terb.devices;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.axotsoft.terb.realm.Database;

public class DeviceNameChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_NAME_CHANGED.equals(intent.getAction())) {
            Database dao = new Database();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            if (device != null) {
                DeviceRecord record = dao.getDevice(device.getAddress());
                if (record != null) {
                    dao.execute(realm -> {
                        record.setDeviceName(name);
                    });
                }
            }
        }
    }
}
