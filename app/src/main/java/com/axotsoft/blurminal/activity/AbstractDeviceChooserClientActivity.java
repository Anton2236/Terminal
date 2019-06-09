package com.axotsoft.blurminal.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

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

                deviceRecord = devicesDao.getDeviceByUri(data.getData());
                updateDeviceData();
            }
            else
            {
                deviceRecord = null;
            }
        }
    }
}
