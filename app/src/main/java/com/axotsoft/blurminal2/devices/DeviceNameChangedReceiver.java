package com.axotsoft.blurminal2.devices;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.axotsoft.blurminal2.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal2.provider.BluetoothDevicesDao;

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
