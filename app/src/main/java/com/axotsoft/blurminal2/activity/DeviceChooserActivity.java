package com.axotsoft.blurminal2.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.blurminal2.R;
import com.axotsoft.blurminal2.devices.BluetoothDevicesManager;
import com.axotsoft.blurminal2.provider.BluetoothDeviceContract;
import com.axotsoft.blurminal2.provider.BluetoothDeviceRecord;

import java.util.ArrayList;
import java.util.List;

public class DeviceChooserActivity extends AppCompatActivity
{
    public static final int REQUEST_DEVICE = 1;
    private BluetoothStateChangedReceiver stateChangedReceiver;

    private BluetoothDevicesManager devicesManager;
    private DevicesAdapter devicesAdapter;
    private List<DeviceData> devices;

    @Override
    protected void onResume()
    {
        super.onResume();
        stateChangedReceiver = new BluetoothStateChangedReceiver(this);
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(stateChangedReceiver, intentFilter);
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        onBluetoothStateChanged(BluetoothAdapter.getDefaultAdapter().getState());
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (stateChangedReceiver != null)
        {
            unregisterReceiver(stateChangedReceiver);
        }
        devicesManager.unregisterReceivers();
    }

    private void onBluetoothStateChanged(int state)
    {
        devices.clear();
        List<BluetoothDeviceRecord> records = devicesManager.getSavedDevices();
        for (BluetoothDeviceRecord record : records)
        {
            devices.add(new DeviceData(record.getDeviceName(), record.getMacAddress(), true));
        }
        if (state == BluetoothAdapter.STATE_ON)
        {
            List<BluetoothDevice> bondedDevices = devicesManager.getAvailableDevices();
            for (BluetoothDevice device : bondedDevices)
            {
                DeviceData data = new DeviceData(device.getName(), device.getAddress(), false);
                data.setDevice(device);
                devices.add(data);
            }
        }
        devicesAdapter.notifyDataSetChanged();
    }

    public void onDeviceClick(DeviceData data)
    {
        if (data.isSaved())
        {
            selectDevice(data);
        }
        else
        {
            saveDevice(data.getDevice());
        }
    }

    private void saveDevice(BluetoothDevice device)
    {
        if (device != null)
        {
            devicesManager.addDevice(device);
        }
    }

    private void selectDevice(DeviceData data)
    {
        BluetoothDeviceRecord record = devicesManager.getDevicesDao().getDeviceByMacAddress(data.getAddress());
        if (record != null)
        {
            Uri uri = BluetoothDeviceContract.DeviceEntry.buildUri(record.getId());
            Intent intent = new Intent();
            intent.setData(uri);
            setResult(RESULT_OK, intent);
            finish();
        }
    }


    public static Intent makeIntent(Context context)
    {
        return new Intent(context, DeviceChooserActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_chooser);

        devicesManager = new BluetoothDevicesManager(this, this::onDeviceSaved);
        devices = new ArrayList<>();
        devicesAdapter = new DevicesAdapter(this::onDeviceClick, devices);
        RecyclerView devicesView = findViewById(R.id.devices);
        devicesView.setAdapter(devicesAdapter);
    }

    private void onDeviceSaved(BluetoothDevice device)
    {
        runOnUiThread(() ->
        {
            for (int i = 0; i < devices.size(); i++)
            {
                if (devices.get(i).getAddress().equals(device.getAddress()))
                {
                    devices.get(i).setSaved(true);
                    devices.get(i).setDevice(null);
                    devicesAdapter.notifyItemChanged(i);
                    break;
                }
            }
        });
    }

    public void onDiscoverClick(View v)
    {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.cancelDiscovery();
        }
        else
        {
            onBluetoothStateChanged(bluetoothAdapter.getState());
            devicesManager.discoverNewDevices(this::onDeviceFound);
        }
    }

    private void onDeviceFound(BluetoothDevice device)
    {
        runOnUiThread(() ->
        {
            DeviceData data = new DeviceData(device.getName(), device.getAddress(), false);
            data.setDevice(device);
            devices.add(data);
            devicesAdapter.notifyItemInserted(devices.size() - 1);
        });
    }

    private static class BluetoothStateChangedReceiver extends BroadcastReceiver
    {
        private DeviceChooserActivity activity;

        private BluetoothStateChangedReceiver(DeviceChooserActivity activity)
        {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_DISCONNECTED);
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            activity.onBluetoothStateChanged(state);
        }
    }


}
