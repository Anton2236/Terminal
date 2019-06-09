package com.axotsoft.blurminal.devices;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.axotsoft.blurminal.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal.provider.BluetoothDevicesDao;
import com.axotsoft.blurminal.provider.BluetoothMessageRecord;

import java.util.List;

public class BluetoothMessagesManager
{
    private BluetoothDevicesDao devicesDao;
    private BluetoothDeviceRecord deviceRecord;

    public BluetoothMessagesManager(BluetoothDevicesDao devicesDao, BluetoothDeviceRecord deviceRecord)
    {
        this.devicesDao = devicesDao;
        this.deviceRecord = deviceRecord;
    }

    public void addMessage(String message, boolean fromDevice)
    {
        new InsertMessageTask(devicesDao).execute(deviceRecord.getId(), message, fromDevice);
    }

    public void clearMessages(Runnable onDelete)
    {
        new DeleteMessagesTask(devicesDao, onDelete).execute(deviceRecord.getId());
    }


    public void getAllMessagesForDevice(MessagesConsumer consumer)
    {
        new ShowMessagesTask(consumer, devicesDao).execute(deviceRecord.getId());
    }

    public BluetoothDeviceRecord getDeviceRecord()
    {
        return deviceRecord;
    }


    private static class ShowMessagesTask extends AsyncTask<Object, Void, List<BluetoothMessageRecord>>
    {
        private MessagesConsumer consumer;
        private BluetoothDevicesDao devicesDao;

        private ShowMessagesTask(MessagesConsumer consumer, BluetoothDevicesDao devicesDao)
        {
            this.consumer = consumer;
            this.devicesDao = devicesDao;
        }

        @Override
        protected List<BluetoothMessageRecord> doInBackground(Object... objects)
        {
            return devicesDao.getAllMessagesForDevice((long) objects[0]);
        }

        @Override
        protected void onPostExecute(List<BluetoothMessageRecord> messageRecords)
        {
            consumer.accept(messageRecords);
        }
    }

    private static class InsertMessageTask extends AsyncTask<Object, Void, Uri>
    {
        private BluetoothDevicesDao devicesDao;

        private InsertMessageTask(BluetoothDevicesDao devicesDao)
        {
            this.devicesDao = devicesDao;
        }

        @Override
        protected Uri doInBackground(Object... objects)
        {

            return devicesDao.insertMessage((long) objects[0], (String) objects[1], (boolean) objects[2]);
        }
    }

    private static class DeleteMessagesTask extends AsyncTask<Object, Void, Integer>
    {
        private BluetoothDevicesDao devicesDao;
        private Runnable onDelete;

        private DeleteMessagesTask(BluetoothDevicesDao devicesDao, Runnable onDelete)
        {
            this.devicesDao = devicesDao;
            this.onDelete = onDelete;
        }

        @Override
        protected Integer doInBackground(Object... objects)
        {
            return devicesDao.deleteAllMessgesForDevice((long) objects[0]);
        }

        @Override
        protected void onPostExecute(Integer integer)
        {
            onDelete.run();
        }
    }
}
