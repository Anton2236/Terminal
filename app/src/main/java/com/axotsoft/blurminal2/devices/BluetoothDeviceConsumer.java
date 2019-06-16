package com.axotsoft.blurminal2.devices;

import android.bluetooth.BluetoothDevice;

public interface BluetoothDeviceConsumer
{
    void accept(BluetoothDevice device);
}
