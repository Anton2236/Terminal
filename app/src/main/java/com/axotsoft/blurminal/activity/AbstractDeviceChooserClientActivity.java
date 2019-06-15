package com.axotsoft.blurminal.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import com.axotsoft.blurminal.R;
import com.axotsoft.blurminal.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal.provider.BluetoothDevicesDao;
import com.axotsoft.blurminal.utils.UiUtils;

public abstract class AbstractDeviceChooserClientActivity extends Activity
{

    protected BluetoothDeviceRecord deviceRecord;
    BluetoothDevicesDao devicesDao;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (BluetoothAdapter.getDefaultAdapter() == null)
        {
            UiUtils.makeToast(this, getResources().getString(R.string.toast_bluetooth_not_supported));
            finish();
            return;
        }
        devicesDao = new BluetoothDevicesDao(this);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (deviceRecord != null)
        {
            deviceRecord = devicesDao.getDeviceById(deviceRecord.getId());
        }
        updateDeviceData(deviceRecord);
    }

    protected void startChoosingActivity()
    {
        Intent intent = DeviceChooserActivity.makeIntent(this);
        startActivityForResult(intent, DeviceChooserActivity.REQUEST_DEVICE);
    }

    protected abstract void updateDeviceData(BluetoothDeviceRecord deviceRecord);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == DeviceChooserActivity.REQUEST_DEVICE)
        {
            if (resultCode == RESULT_OK)
            {
                deviceRecord = devicesDao.getDeviceByUri(data.getData());
            }
            else
            {
                deviceRecord = null;
            }
        }
    }
}
