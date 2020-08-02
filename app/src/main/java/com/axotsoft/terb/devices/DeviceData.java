package com.axotsoft.terb.devices;

import android.bluetooth.BluetoothDevice;

import java.util.Objects;

public class DeviceData {

    private final String address;
    private String name;
    private boolean bonded;
    private boolean saved;
    private BluetoothDevice device;

    public DeviceData(String address, String name, boolean saved, boolean bonded) {
        this.address = address;
        this.name = name;
        this.saved = saved;
        this.bonded = bonded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isBonded() {
        return bonded;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void setBonded(boolean bonded) {
        this.bonded = bonded;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }
}
