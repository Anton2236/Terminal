package com.axotsoft.blurminal2.activity;

import java.util.Comparator;
import java.util.Objects;

public class BluetoothMessageData
{
    private String message;
    private boolean fromDevice;
    private long timeMillis;
    private boolean error;

    public BluetoothMessageData(String message, boolean fromDevice, long timeMillis, boolean error)
    {
        this.message = message;
        this.fromDevice = fromDevice;
        this.timeMillis = timeMillis;
        this.error = error;
    }

    public BluetoothMessageData(String message, boolean fromDevice, long timeMillis)
    {
        this(message, fromDevice, timeMillis, false);
    }

    public String getMessage()
    {
        return message;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BluetoothMessageData that = (BluetoothMessageData) o;
        return fromDevice == that.fromDevice &&
                timeMillis == that.timeMillis &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(message, fromDevice, timeMillis);
    }

    public boolean isFromDevice()
    {
        return fromDevice;
    }

    public long getTimeMillis()
    {
        return timeMillis;
    }

    public boolean isError()
    {
        return error;
    }

    public static class MessageComparator implements Comparator<BluetoothMessageData>
    {
        @Override
        public int compare(BluetoothMessageData o1, BluetoothMessageData o2)
        {
            return (int) (o1.getTimeMillis() - o2.getTimeMillis());
        }
    }
}
