package com.axotsoft.blurminal.devices;

import android.bluetooth.BluetoothDevice;

public interface BluetoothDeviceConsumer
{
    void accept(BluetoothDevice device);
}
