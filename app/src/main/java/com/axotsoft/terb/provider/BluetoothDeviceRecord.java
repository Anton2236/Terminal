package com.axotsoft.terb.provider;

import com.axotsoft.terb.bluetooth.LINE_ENDING_TYPE;

import java.util.List;

public class BluetoothDeviceRecord
{
    private long id;
    private String macAddress;
    private LINE_ENDING_TYPE lineEnding;
    private List<String> commands;
    private String deviceName;

    BluetoothDeviceRecord(long id, String macAddress, String deviceName, LINE_ENDING_TYPE lineEnding, List<String> commands)
    {
        this.id = id;
        this.macAddress = macAddress;
        this.deviceName = deviceName;
        this.lineEnding = lineEnding;
        this.commands = commands;
    }

    public long getId()
    {
        return id;
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public LINE_ENDING_TYPE getLineEnding()
    {
        return lineEnding;
    }

    public List<String> getCommands()
    {
        return commands;
    }


    public void setLineEnding(LINE_ENDING_TYPE lineEnding)
    {
        this.lineEnding = lineEnding;
    }

    public void setCommands(List<String> commands)
    {
        this.commands = commands;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getDeviceName()
    {
        return deviceName;
    }
}
