package com.axotsoft.blurminal.devices;

import com.axotsoft.blurminal.provider.BluetoothMessageRecord;

import java.util.List;

public interface MessagesConsumer
{
    void accept(List<BluetoothMessageRecord> messageRecords);
}
