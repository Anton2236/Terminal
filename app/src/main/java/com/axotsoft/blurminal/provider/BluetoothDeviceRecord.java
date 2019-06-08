package com.axotsoft.blurminal.provider;

import com.axotsoft.blurminal.bluetooth.LINE_ENDING_TYPE;

import java.util.List;

class BluetoothDeviceRecord
{
    private long id;
    private String macAddress;
    private LINE_ENDING_TYPE lineEnding;
    private List<String> commands;

    BluetoothDeviceRecord(long id, String macAddress, LINE_ENDING_TYPE lineEnding, List<String> commands)
    {
        this.id = id;
        this.macAddress = macAddress;
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
}
