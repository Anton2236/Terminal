package com.axotsoft.blurminal2.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.axotsoft.blurminal2.bluetooth.LINE_ENDING_TYPE;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDevicesDao
{
    private ContentResolver contentResolver;


    public BluetoothDevicesDao(Context context)
    {
        this.contentResolver = context.getContentResolver();
    }

    public BluetoothDeviceRecord getDeviceById(long id)
    {
        Uri uri = BluetoothDeviceContract.DeviceEntry.buildUri(id);
        return getDeviceByUri(uri);
    }

    public BluetoothDeviceRecord getDeviceByUri(Uri uri)
    {
        Cursor cursor = contentResolver.query(uri, BluetoothDeviceContract.DeviceEntry.projection, null, null, null);

        BluetoothDeviceRecord deviceRecord = null;
        if (cursor != null && cursor.moveToFirst())
        {
            deviceRecord = getDeviceRecordFromCursor(cursor);
        }
        return deviceRecord;
    }

    public BluetoothDeviceRecord getDeviceByMacAddress(String macAddress)
    {
        Uri uri = BluetoothDeviceContract.DeviceEntry.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, BluetoothDeviceContract.DeviceEntry.projection, BluetoothDeviceContract.DeviceEntry._MAC_ADDRESS, new String[]{macAddress}, null);

        BluetoothDeviceRecord deviceRecord = null;
        if (cursor != null && cursor.moveToFirst())
        {
            deviceRecord = getDeviceRecordFromCursor(cursor);
        }
        return deviceRecord;
    }

    public List<BluetoothDeviceRecord> getAllDevices()
    {
        Uri uri = BluetoothDeviceContract.DeviceEntry.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, BluetoothDeviceContract.DeviceEntry.projection, null, null, null);

        List<BluetoothDeviceRecord> deviceRecords = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst())
        {
            do
            {
                deviceRecords.add(getDeviceRecordFromCursor(cursor));
            }
            while (cursor.moveToNext());
        }
        return deviceRecords;
    }

    public List<BluetoothMessageRecord> getAllMessagesForDevice(long deviceId)
    {
        Uri uri = BluetoothDeviceContract.MessageEntry.CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, BluetoothDeviceContract.MessageEntry.projection, BluetoothDeviceContract.MessageEntry._DEVICE_ID, new String[]{String.valueOf(deviceId)}, null);

        List<BluetoothMessageRecord> deviceRecords = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst())
        {
            do
            {
                deviceRecords.add(getMessageRecordFromCursor(cursor));
            }
            while (cursor.moveToNext());
        }
        return deviceRecords;
    }

    public int deleteAllMessgesForDevice(long deviceId)
    {
        Uri uri = BluetoothDeviceContract.MessageEntry.CONTENT_URI;

        return contentResolver.delete(uri, BluetoothDeviceContract.MessageEntry._DEVICE_ID, new String[]{String.valueOf(deviceId)});
    }

    public int deleteAllMessges()
    {
        Uri uri = BluetoothDeviceContract.MessageEntry.CONTENT_URI;
        return contentResolver.delete(uri, null, null);
    }

    public int deleteAllDevices()
    {
        Uri uri = BluetoothDeviceContract.DeviceEntry.CONTENT_URI;
        return contentResolver.delete(uri, null, null);
    }

    public void updateDevice(BluetoothDeviceRecord deviceRecord)
    {
        Uri uri = BluetoothDeviceContract.DeviceEntry.buildUri(deviceRecord.getId());
        contentResolver.update(uri, getValuesForDeviceUpdate(deviceRecord), null, null);
    }


    private ContentValues getValuesForDeviceUpdate(BluetoothDeviceRecord deviceRecord)
    {
        ContentValues values = new ContentValues();
        values.put(BluetoothDeviceContract.DeviceEntry._LINE_ENDING, deviceRecord.getLineEnding().getKey());
        values.put(BluetoothDeviceContract.DeviceEntry._DEVICE_NAME, deviceRecord.getDeviceName());
        List<String> commands = deviceRecord.getCommands();
        Gson gson = new Gson();
        String commandsJson = "";
        if (commands != null)
        {
            commandsJson = gson.toJson(commands, new TypeToken<List<String>>()
            {
            }.getType());
        }
        values.put(BluetoothDeviceContract.DeviceEntry._COMMANDS, commandsJson.getBytes());
        return values;
    }


    public Uri insertDevice(String macAddress)
    {
        return contentResolver.insert(BluetoothDeviceContract.DeviceEntry.CONTENT_URI, getValuesForDeviceInsert(macAddress));
    }

    public Uri insertMessage(long deviceId, String message, boolean fromDevice)
    {
        return contentResolver.insert(BluetoothDeviceContract.MessageEntry.CONTENT_URI, getValuesForMessageInsert(deviceId, message, fromDevice));
    }

    private ContentValues getValuesForMessageInsert(long deviceId, String message, boolean fromDevice)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(BluetoothDeviceContract.MessageEntry._DEVICE_ID, deviceId);
        contentValues.put(BluetoothDeviceContract.MessageEntry._MESSAGE, message);
        contentValues.put(BluetoothDeviceContract.MessageEntry._FROM_DEVICE, fromDevice);
        contentValues.put(BluetoothDeviceContract.MessageEntry._TIME_MILLIS, System.currentTimeMillis());
        return contentValues;
    }

    private ContentValues getValuesForDeviceInsert(String macAddress)
    {
        ContentValues values = new ContentValues();
        values.put(BluetoothDeviceContract.DeviceEntry._MAC_ADDRESS, macAddress);
        return values;
    }

    private BluetoothMessageRecord getMessageRecordFromCursor(Cursor cursor)
    {

        int idInd = cursor.getColumnIndex(BluetoothDeviceContract.MessageEntry._ID);
        int deviceIdInd = cursor.getColumnIndex(BluetoothDeviceContract.MessageEntry._DEVICE_ID);
        int messageInd = cursor.getColumnIndex(BluetoothDeviceContract.MessageEntry._MESSAGE);
        int fromDeviceInd = cursor.getColumnIndex(BluetoothDeviceContract.MessageEntry._FROM_DEVICE);
        int millisInd = cursor.getColumnIndex(BluetoothDeviceContract.MessageEntry._TIME_MILLIS);


        long id = cursor.getLong(idInd);
        long deviceId = cursor.getLong(deviceIdInd);
        String message = cursor.getString(messageInd);
        boolean fromDevice = cursor.getInt(fromDeviceInd) == 1;
        long millis = cursor.getLong(millisInd);
        return new BluetoothMessageRecord(id, deviceId, fromDevice, message, millis);

    }

    private BluetoothDeviceRecord getDeviceRecordFromCursor(Cursor cursor)
    {
        int idInd = cursor.getColumnIndex(BluetoothDeviceContract.DeviceEntry._ID);
        int macInd = cursor.getColumnIndex(BluetoothDeviceContract.DeviceEntry._MAC_ADDRESS);
        int nameInd = cursor.getColumnIndex(BluetoothDeviceContract.DeviceEntry._DEVICE_NAME);
        int lineEndingInd = cursor.getColumnIndex(BluetoothDeviceContract.DeviceEntry._LINE_ENDING);
        int commandsInd = cursor.getColumnIndex(BluetoothDeviceContract.DeviceEntry._COMMANDS);
        long id = cursor.getLong(idInd);
        String macAddress = cursor.getString(macInd);
        String deviceName = cursor.getString(nameInd);
        int lineEndingId = cursor.getInt(lineEndingInd);
        byte[] commandBytes = cursor.getBlob(commandsInd);
        Gson gson = new Gson();
        String json = "";
        if (commandBytes != null)
        {
            json = new String(commandBytes);
        }
        List<String> commands = null;
        if (!json.isEmpty())
        {
            try
            {
                commands = gson.fromJson(json, new TypeToken<List<String>>()
                {
                }.getType());
            } catch (Exception e)
            {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
        }
        return new BluetoothDeviceRecord(id, macAddress, deviceName, LINE_ENDING_TYPE.valueOf(lineEndingId), commands);
    }
}
