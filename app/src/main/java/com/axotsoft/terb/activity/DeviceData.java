package com.axotsoft.terb.activity;

import android.bluetooth.BluetoothDevice;

public class DeviceData
{
    private String name;
    private String address;
    private boolean saved;
    private BluetoothDevice device;

    public DeviceData(String name, String address, boolean saved)
    {
        this.name = name;
        this.address = address;
        this.saved = saved;
    }

    public String getName()
    {
        return name;
    }

    public String getAddress()
    {
        return address;
    }

    public boolean isSaved()
    {
        return saved;
    }


    public BluetoothDevice getDevice()
    {
        return device;
    }

    public void setDevice(BluetoothDevice device)
    {
        this.device = device;
    }

    public void setSaved(boolean saved)
    {
        this.saved = saved;
    }
}
