package com.axotsoft.terb.devices;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.realm.Database;
import com.axotsoft.terb.utils.ContextAdapter;
import com.axotsoft.terb.utils.LINE_ENDING_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class BluetoothDevicesManager {
    private final BluetoothAdapter adapter;
    private final Database database;
    private final ContextAdapter contextAdapter;
    private final DevicesAdapter devicesAdapter;
    private final RecyclerView devicesView;
    private final List<DeviceData> devices;
    private final Map<String, DeviceData> devicesMap;
    private final Consumer<String> selectionConsumer;


    public BluetoothDevicesManager(ContextAdapter contextAdapter, RecyclerView devicesView, Consumer<String> selectionConsumer) {
        this.database = new Database();
        this.contextAdapter = contextAdapter;
        this.devicesView = devicesView;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.devices = new ArrayList<>();
        this.devicesMap = new HashMap<>();
        this.devicesAdapter = new DevicesAdapter(this::onDeviceClick, this::onPairClick, devices);
        this.devicesView.setAdapter(devicesAdapter);
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
            DeviceRecord record = database.getDevice(data.getAddress());
            if (record != null) {
                selectionConsumer.accept(record.getAddress());
            }
        }
    }

    private void onPairClick(DeviceData deviceData) {
        BluetoothDevice device = deviceData.getDevice();
        if (device != null && device.getBondState() == BluetoothDevice.BOND_NONE) {
            if (adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }
            device.createBond();
        }
    }

    public boolean saveDevice(BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            String deviceAddress = device.getAddress();

            database.execute(realm -> {
                DeviceRecord record = new DeviceRecord(deviceAddress);
                record.setDeviceName(device.getName());
                record.setLineEnding(LINE_ENDING_TYPE.CRLF);
                realm.insert(record);
            });

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
                devicesAdapter.notifyItemChanged(devices.indexOf(deviceData));
            }
        }
    }


    private void onDeviceFound(Context context, Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device != null) {
            updateOrInsertDevice(device, true);
        }
    }

    private void updateOrInsertDevice(BluetoothDevice device, boolean notifyAdapter) {
        boolean bonded = device.getBondState() == BluetoothDevice.BOND_BONDED;
        DeviceData data = new DeviceData(device.getAddress(), device.getName(), false, bonded);
        data.setDevice(device);
        DeviceData storedData = devicesMap.get(data.getAddress());
        if (storedData == null) {
            devices.add(data);
            devicesMap.put(data.getAddress(), data);

            if (notifyAdapter) {
                devicesAdapter.notifyItemInserted(devices.size() - 1);
                devicesView.scrollToPosition(devices.size() - 1);
            }
        }
        else {
            if (storedData.isSaved() && !Objects.equals(device.getName(), storedData.getName())) {
                DeviceRecord deviceRecord = database.getDevice(storedData.getAddress());
                if (deviceRecord != null) {
                    database.execute(realm -> {
                        deviceRecord.setDeviceName(device.getName());
                    });
                }
            }
            storedData.setName(device.getName());
            storedData.setBonded(bonded);
            storedData.setDevice(device);

            if (notifyAdapter) {
                devicesAdapter.notifyItemChanged(devices.indexOf(storedData));
            }
        }
    }

    public void updateDevices() {
        devices.clear();
        devicesMap.clear();
        List<DeviceRecord> records = database.getAllDevices();
        boolean bluetoothEnabled = adapter.getState() == BluetoothAdapter.STATE_ON;

        for (DeviceRecord record : records) {
            DeviceData data = new DeviceData(record.getAddress(), record.getDeviceName(), true, !bluetoothEnabled);
            if (devicesMap.get(data.getAddress()) == null) {
                devicesMap.put(data.getAddress(), data);
                devices.add(data);
            }
        }


        if (bluetoothEnabled) {
            Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                updateOrInsertDevice(device, false);
            }
        }
        devicesAdapter.notifyDataSetChanged();
    }
}
