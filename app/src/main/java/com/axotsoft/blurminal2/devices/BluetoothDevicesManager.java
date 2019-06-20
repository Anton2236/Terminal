package com.axotsoft.blurminal2.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import com.axotsoft.blurminal2.bluetooth.LINE_ENDING_TYPE;
import com.axotsoft.blurminal2.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal2.provider.BluetoothDevicesDao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BluetoothDevicesManager
{
    private BluetoothDevicesDao devicesDao;
    private Context context;
    private BluetoothDeviceConsumer foundDeviceConsumer;
    private BluetoothDeviceConsumer dbDevicesConsumer;

    private BroadcastReceiver bondedReceiver = null;

    private BroadcastReceiver getBondedReceiver()
    {
        return new BroadcastReceiver()
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
                    unregisterBondedReceiver(context);
                }
            }
        };
    }

    private void unregisterBondedReceiver(Context context)
    {
        if (bondedReceiver != null)
        {
            context.unregisterReceiver(bondedReceiver);
            bondedReceiver = null;
        }
    }

    private BroadcastReceiver deviceFoundReceiver = null;

    private BroadcastReceiver getFoundReceiver()
    {
        return new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (foundDeviceConsumer != null && BluetoothDevice.ACTION_FOUND.equals(intent.getAction()))
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
    }

    private BroadcastReceiver discoveryStopReceiver = null;

    private BroadcastReceiver getDiscoveryStopReceiver()
    {
        return new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                unregisterDeviceFoundReceiver(context);
                unregisterDiscoveryStopReceiver(context);
                foundDeviceConsumer = null;
            }
        };
    }

    private void unregisterDeviceFoundReceiver(Context context)
    {
        if (deviceFoundReceiver != null)
        {
            context.unregisterReceiver(deviceFoundReceiver);
            deviceFoundReceiver = null;
        }
    }

    private void unregisterDiscoveryStopReceiver(Context context)
    {
        if (discoveryStopReceiver != null)
        {
            context.unregisterReceiver(discoveryStopReceiver);
            discoveryStopReceiver = null;
        }
    }

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
        if (bondedReceiver == null)
        {
            bondedReceiver = getBondedReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            context.registerReceiver(bondedReceiver, intentFilter);
        }
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
        return filterDevices(adapter.getBondedDevices());
    }


    public void discoverNewDevices(BluetoothDeviceConsumer consumer)
    {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled())
        {
            context.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
        else
        {
            if (adapter.startDiscovery())
            {
                this.foundDeviceConsumer = consumer;
                registerDiscoveredReceiver();
                registerDiscoveryStopReceiver();
            }
        }
    }

    private void registerDiscoveryStopReceiver()
    {
        if (discoveryStopReceiver == null)
        {
            discoveryStopReceiver = getDiscoveryStopReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            context.registerReceiver(discoveryStopReceiver, intentFilter);
        }
    }

    private void registerDiscoveredReceiver()
    {
        if (deviceFoundReceiver == null)
        {
            deviceFoundReceiver = getFoundReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            context.registerReceiver(deviceFoundReceiver, intentFilter);
        }
    }


    public void unregisterReceivers()
    {
        unregisterDeviceFoundReceiver(context);
        unregisterDiscoveryStopReceiver(context);
        unregisterBondedReceiver(context);
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
