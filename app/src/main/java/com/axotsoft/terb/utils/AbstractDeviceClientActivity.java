package com.axotsoft.terb.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.axotsoft.terb.R;
import com.axotsoft.terb.activity.DeviceChooserActivity;
import com.axotsoft.terb.devices.DeviceRecord;
import com.axotsoft.terb.realm.Database;

public abstract class AbstractDeviceClientActivity extends AppCompatActivity {

    protected DeviceRecord deviceRecord;
    protected Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            UiUtils.makeToast(this, getResources().getString(R.string.toast_bluetooth_not_supported));
            finish();
            return;
        }
        database = new Database();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDeviceData(deviceRecord);
    }

    protected void startChoosingActivity() {
        Intent intent = DeviceChooserActivity.makeIntent(this);
        startActivityForResult(intent, DeviceChooserActivity.REQUEST_DEVICE);
    }

    protected abstract void updateDeviceData(DeviceRecord deviceRecord);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DeviceChooserActivity.REQUEST_DEVICE) {
            if (resultCode == RESULT_OK) {
                String address = data.getStringExtra(DeviceChooserActivity.EXTRA_DEVICE_ADDRESS);
                deviceRecord = database.getDevice(address);
            }
            else {
                deviceRecord = null;
            }
        }
    }
}
