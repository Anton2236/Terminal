package com.axotsoft.terb.chooser;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.axotsoft.terb.bluetooth.LINE_ENDING_TYPE;
import com.axotsoft.terb.provider.BluetoothDeviceContract;
import com.axotsoft.terb.provider.BluetoothDeviceRecord;
import com.axotsoft.terb.provider.BluetoothDevicesDao;
import com.axotsoft.terb.utils.ContextAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class BluetoothDevicesManager {
    private final BluetoothAdapter adapter;
    private final BluetoothDevicesDao devicesDao;
    private final ContextAdapter contextAdapter;
    private final DevicesAdapter devicesAdapter;
    private final List<DeviceData> devices;
    private final Map<String, DeviceData> devicesMap;
    private Consumer<Uri> selectionConsumer;

    public BluetoothDevicesManager(ContextAdapter contextAdapter, Consumer<Uri> selectionConsumer) {
        this.devicesDao = new BluetoothDevicesDao(contextAdapter.getContext());
        this.contextAdapter = contextAdapter;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.devices = new ArrayList<>();
        this.devicesMap = new HashMap<>();
        this.devicesAdapter = new DevicesAdapter(this::onDeviceClick, devices);
        this.selectionConsumer = selectionConsumer;
        registerReceivers();
    }

    private void registerReceivers() {
        contextAdapter.registerBroadcastConsumer(BluetoothDevice.ACTION_BOND_STATE_CHANGED, this::onDeviceBonded);
        contextAdapter.registerBroadcastConsumer(BluetoothDevice.ACTION_FOUND, this::onDeviceFound);
        contextAdapter.registerBroadcastConsumer(BluetoothAdapter.ACTION_STATE_CHANGED, (context, intent) -> updateDevices());
    }


    public void onDeviceClick(DeviceData data) {
        if (data.isSaved() || saveDevice(data.getDevice())) {
            BluetoothDeviceRecord record = devicesDao.getDeviceByMacAddress(data.getAddress());
            if (record != null) {
                Uri uri = BluetoothDeviceContract.DeviceEntry.buildUri(record.getId());
                selectionConsumer.accept(uri);
            }
        }
    }

    public boolean saveDevice(BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            String deviceAddress = device.getAddress();
            Uri uri = devicesDao.insertDevice(deviceAddress);

            BluetoothDeviceRecord record = devicesDao.getDeviceByUri(uri);
            record.setLineEnding(LINE_ENDING_TYPE.CRLF);
            record.setCommands(new ArrayList<>());
            record.setDeviceName(device.getName());
            devicesDao.updateDevice(record);

            DeviceData deviceData = devicesMap.get(deviceAddress);
            if (deviceData != null) {
                deviceData.setSaved(true);
                devicesAdapter.notifyItemChanged(devices.indexOf(deviceData));
            }
            return true;
        }

        if (device.getBondState() == BluetoothDevice.BOND_NONE) {
            if (adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }
            device.createBond();
        }
        return false;
    }


    private void onDeviceBonded(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
        if (device != null && bondState == BluetoothDevice.BOND_BONDED) {
            DeviceData deviceData = devicesMap.get(device.getAddress());
            if (deviceData != null) {
                deviceData.setBonded(true);
                deviceData.setDevice(null);
                devicesAdapter.notifyItemChanged(devices.indexOf(deviceData));
            }
        }
    }


    private void onDeviceFound(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device != null) {
            DeviceData data = new DeviceData(device.getAddress(), device.getName(), false, false);
            data.setDevice(device);
            DeviceData storedData = devicesMap.get(data.getAddress());
            if (storedData == null) {
                devices.add(data);
                devicesMap.put(data.getAddress(), data);
                devicesAdapter.notifyItemInserted(devices.size() - 1);
            }
            else {
                storedData.setName(device.getName());
                storedData.setDevice(device);
                devicesAdapter.notifyItemChanged(devices.indexOf(storedData));
                if (storedData.isSaved()) {
                    BluetoothDeviceRecord deviceRecord = devicesDao.getDeviceByMacAddress(storedData.getAddress());
                    if (deviceRecord != null) {
                        deviceRecord.setDeviceName(device.getName());
                        devicesDao.updateDevice(deviceRecord);
                    }
                }
            }
        }
    }

    public DevicesAdapter getDevicesAdapter() {
        return devicesAdapter;
    }

    public void updateDevices() {
        devices.clear();
        devicesMap.clear();
        List<BluetoothDeviceRecord> records = devicesDao.getAllDevices();

        for (BluetoothDeviceRecord record : records) {
            DeviceData data = new DeviceData(record.getMacAddress(), record.getDeviceName(), true, true);
            if (devicesMap.get(data.getAddress()) == null) {
                devicesMap.put(data.getAddress(), data);
                devices.add(data);
            }
        }

        if (adapter.getState() == BluetoothAdapter.STATE_ON) {
            Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                DeviceData data = new DeviceData(device.getAddress(), device.getName(), false, true);
                data.setDevice(device);
                if (devicesMap.get(data.getAddress()) == null) {
                    devicesMap.put(data.getAddress(), data);
                    devices.add(data);
                }
            }
        }
        devicesAdapter.notifyDataSetChanged();
    }
}
