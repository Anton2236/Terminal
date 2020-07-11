package com.axotsoft.terb.devices;

import android.bluetooth.BluetoothDevice;

public interface BluetoothDeviceConsumer {
    void accept(BluetoothDevice device);
}
