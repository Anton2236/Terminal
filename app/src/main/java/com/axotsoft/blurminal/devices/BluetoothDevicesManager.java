package com.axotsoft.blurminal.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;

import com.axotsoft.blurminal.bluetooth.LINE_ENDING_TYPE;
import com.axotsoft.blurminal.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal.provider.BluetoothDevicesDao;
import com.axotsoft.blurminal.provider.BluetoothMessageRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BluetoothDevicesManager
{
    private BluetoothDevicesDao devicesDao;
    private Context context;
    private BluetoothDeviceConsumer deviceConsumer;

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
            if (deviceConsumer != null)
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                List<BluetoothDevice> devices = new ArrayList<>();
                devices.add(device);
                devices = filterDevices(devices);
                if (devices.size() > 0)
                {
                    deviceConsumer.accept(device);
                }
            }
        }
    };

    public BluetoothDevicesManager(Context context)
    {
        this.devicesDao = new BluetoothDevicesDao(context);
        this.context = context;
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
            devicesDao.updateDevice(record);
        }
    }

    public void updateDevice(BluetoothDeviceRecord record)
    {
        devicesDao.updateDevice(record);
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
        this.deviceConsumer = consumer;
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
                deviceConsumer = null;
            }
        }, intentFilter);
    }

    private void registerDiscoveredReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(deviceFoundReceiver, intentFilter);
    }

    public List<BluetoothDevice> filterDevices(Collection<BluetoothDevice> initialDevices)
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

    public void addMessage(long deviceId, String message, boolean fromDevice)
    {
        new InsertMessageTask(devicesDao).execute(deviceId, message, fromDevice);
    }

    public void getAllMessagesForDevice(long deviceId, MessagesConsumer consumer)
    {
        new ShowMessagesTask(consumer, devicesDao).execute(deviceId);
    }


    private static class ShowMessagesTask extends AsyncTask<Object, Void, List<BluetoothMessageRecord>>
    {
        private MessagesConsumer consumer;
        private BluetoothDevicesDao devicesDao;

        private ShowMessagesTask(MessagesConsumer consumer, BluetoothDevicesDao devicesDao)
        {
            this.consumer = consumer;
            this.devicesDao = devicesDao;
        }

        @Override
        protected List<BluetoothMessageRecord> doInBackground(Object... objects)
        {
            return devicesDao.getAllMessagesForDevice((long) objects[0]);
        }

        @Override
        protected void onPostExecute(List<BluetoothMessageRecord> messageRecords)
        {
            consumer.accept(messageRecords);
        }
    }

    private static class InsertMessageTask extends AsyncTask<Object, Void, Uri>
    {
        private BluetoothDevicesDao devicesDao;

        private InsertMessageTask(BluetoothDevicesDao devicesDao)
        {
            this.devicesDao = devicesDao;
        }

        @Override
        protected Uri doInBackground(Object... objects)
        {

            return devicesDao.insertMessage((long) objects[0], (String) objects[1], (boolean) objects[2]);
        }
    }
}
