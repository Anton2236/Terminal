package com.axotsoft.blurminal.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import com.axotsoft.blurminal.bluetooth.LINE_ENDING_TYPE;
import com.axotsoft.blurminal.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal.provider.BluetoothDevicesDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BluetoothDevicesManager
{
    private BluetoothDevicesDao devicesDao;
    private Context context;
    private BluetoothDeviceConsumer foundDeviceConsumer;
    private BluetoothDeviceConsumer dbDevicesConsumer;

    private BroadcastReceiver bondedReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
            if (bondState == BluetoothDevice.BOND_BONDED)
            {
                addDevice(device);
            }
            if (bondState != BluetoothDevice.BOND_BONDING)
            {
                context.unregisterReceiver(this);
            }
        }
    };

    private BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (foundDeviceConsumer != null)
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                List<BluetoothDevice> devices = new ArrayList<>();
                devices.add(device);
                devices = filterDevices(devices);
                if (devices.size() > 0)
                {
                    foundDeviceConsumer.accept(device);
                }
            }
        }
    };

    public BluetoothDevicesManager(Context context, BluetoothDeviceConsumer dbDevicesConsumer)
    {
        this.devicesDao = new BluetoothDevicesDao(context);
        this.context = context;
        this.dbDevicesConsumer = dbDevicesConsumer;
    }


    public BluetoothDevicesDao getDevicesDao()
    {
        return devicesDao;
    }

    public List<BluetoothDeviceRecord> getSavedDevices()
    {
        return devicesDao.getAllDevices();
    }

    public void addDevice(BluetoothDevice device)
    {
        if (device.getBondState() == BluetoothDevice.BOND_NONE)
        {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            registerBondedReceiver();
            device.createBond();
        }
        else if (device.getBondState() == BluetoothDevice.BOND_BONDED)
        {
            addBondedDevice(device);
        }
    }

    private void registerBondedReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(bondedReceiver, intentFilter);
    }


    private void addBondedDevice(BluetoothDevice device)
    {
        if (device.getBondState() == BluetoothDevice.BOND_BONDED)
        {
            Uri uri = devicesDao.insertDevice(device.getAddress());
            BluetoothDeviceRecord record = devicesDao.getDeviceByUri(uri);
            record.setLineEnding(LINE_ENDING_TYPE.CRLF);
            record.setCommands(new ArrayList<>());
            record.setDeviceName(device.getName());
            devicesDao.updateDevice(record);
            dbDevicesConsumer.accept(device);
        }
    }

    public List<BluetoothDevice> getAvailableDevices()
    {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled())
        {
            context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            return null;
        }
        else
        {
            return filterDevices(adapter.getBondedDevices());
        }
    }


    public void discoverNewDevices(BluetoothDeviceConsumer consumer)
    {
        this.foundDeviceConsumer = consumer;
        registerDiscoveredReceiver();
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
        registerDiscoveryStopReceiver();
    }

    private void registerDiscoveryStopReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                context.unregisterReceiver(deviceFoundReceiver);
                context.unregisterReceiver(this);
                foundDeviceConsumer = null;
            }
        }, intentFilter);
    }

    private void registerDiscoveredReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(deviceFoundReceiver, intentFilter);
    }

    private List<BluetoothDevice> filterDevices(Collection<BluetoothDevice> initialDevices)
    {
        List<BluetoothDevice> devices = new ArrayList<>(initialDevices);
        List<BluetoothDeviceRecord> allDevices = getSavedDevices();
        for (BluetoothDevice device : initialDevices)
        {
            String address = device.getAddress();
            for (BluetoothDeviceRecord record : allDevices)
            {
                if (record.getMacAddress().equals(address))
                {
                    devices.remove(device);
                    break;
                }
            }
        }
        return devices;

    }
}
