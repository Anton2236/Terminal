package com.axotsoft.blurminal2.devices;

import com.axotsoft.blurminal2.provider.BluetoothMessageRecord;

import java.util.List;

public interface MessagesConsumer
{
    void accept(List<BluetoothMessageRecord> messageRecords);
}
