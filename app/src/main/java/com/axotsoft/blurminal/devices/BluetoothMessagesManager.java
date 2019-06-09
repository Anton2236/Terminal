package com.axotsoft.blurminal.devices;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.axotsoft.blurminal.provider.BluetoothDevicesDao;
import com.axotsoft.blurminal.provider.BluetoothMessageRecord;

import java.util.List;

public class BluetoothMessagesManager
{
    private BluetoothDevicesDao devicesDao;

    public BluetoothMessagesManager(Context context)
    {
        this.devicesDao = new BluetoothDevicesDao(context);
    }

    public void addMessage(long deviceId, String message, boolean fromDevice)
    {
        new InsertMessageTask(devicesDao).execute(deviceId, message, fromDevice);
    }

    public void clearMessages(long deviceId, Runnable onDelete)
    {
        new DeleteMessagesTask(devicesDao, onDelete).execute(deviceId);
    }


    public void getAllMessagesForDevice(long deviceId, MessagesConsumer consumer)
    {
        new ShowMessagesTask(consumer, devicesDao).execute(deviceId);
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
