package com.axotsoft.blurminal.activity;

public class BluetoothMessageData
{
    private String message;
    private boolean fromDevice;
    private long timeMillis;

    public BluetoothMessageData(String message, boolean fromDevice, long timeMillis)
    {
        this.message = message;
        this.fromDevice = fromDevice;
        this.timeMillis = timeMillis;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isFromDevice()
    {
        return fromDevice;
    }

    public long getTimeMillis()
    {
        return timeMillis;
    }
}
