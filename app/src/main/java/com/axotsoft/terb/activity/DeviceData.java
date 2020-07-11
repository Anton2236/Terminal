package com.axotsoft.terb.activity;

import android.bluetooth.BluetoothDevice;

import java.util.Objects;

public class DeviceData {
    private String name;
    private String address;
    private boolean bonded;
    private boolean saved;
    private BluetoothDevice device;

    public DeviceData(String name, String address, boolean saved, boolean bonded) {
        this.name = name;
        this.address = address;
        this.saved = saved;
        this.bonded = bonded;
    }

    public String getName() {
        return name;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceData that = (DeviceData) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
