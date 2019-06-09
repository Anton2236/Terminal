package com.axotsoft.blurminal.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.axotsoft.blurminal.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal.provider.BluetoothDevicesDao;

public abstract class AbstarctDeviceChooserClientActivity extends Activity
{

    protected BluetoothDeviceRecord deviceRecord;

    @Override
    protected void onResume()
    {
        super.onResume();
        if (deviceRecord != null)
        {
            deviceRecord = new BluetoothDevicesDao(this).getDeviceById(deviceRecord.getId());
            updateDeviceData();
        }
    }

    protected void startChoosingActivity()
    {
        Intent intent = DeviceChooserActivity.makeIntent(this);
        startActivityForResult(intent, DeviceChooserActivity.REQUEST_DEVICE);
    }

    protected abstract void updateDeviceData();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == DeviceChooserActivity.REQUEST_DEVICE)
        {
            if (resultCode == RESULT_OK)
            {
                deviceRecord = new BluetoothDevicesDao(this).getDeviceByUri(data.getData());
                updateDeviceData();
            }
            else
            {
                deviceRecord = null;
            }
        }
    }
}
