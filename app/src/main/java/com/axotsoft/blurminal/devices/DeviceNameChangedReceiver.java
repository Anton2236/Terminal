package com.axotsoft.blurminal.devices;

import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.axotsoft.blurminal.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal.provider.BluetoothDevicesDao;

public class DeviceNameChangedReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (BluetoothDevice.ACTION_NAME_CHANGED.equals(intent.getAction()))
        {
            BluetoothDevicesDao dao = new BluetoothDevicesDao(context);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDeviceRecord record = dao.getDeviceByMacAddress(device.getAddress());
            if (record != null)
            {
                record.setDeviceName(name);
                dao.updateDevice(record);
            }
        }
    }
}
