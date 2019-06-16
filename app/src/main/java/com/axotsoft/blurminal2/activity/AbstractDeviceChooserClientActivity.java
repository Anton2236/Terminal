package com.axotsoft.blurminal2.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.axotsoft.blurminal2.R;
import com.axotsoft.blurminal2.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal2.provider.BluetoothDevicesDao;
import com.axotsoft.blurminal2.utils.UiUtils;

public abstract class AbstractDeviceChooserClientActivity extends AppCompatActivity
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
