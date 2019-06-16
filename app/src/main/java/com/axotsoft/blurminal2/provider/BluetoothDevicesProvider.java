package com.axotsoft.blurminal2.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import androidx.annotation.NonNull;
import android.text.TextUtils;

public class BluetoothDevicesProvider extends ContentProvider
{
    private Context context;
    private BluetoothDevicesSQLiteHelper sqLiteHelper;

    private static final int MESSAGES = 100;
    private static final int MESSAGE = 101;
    private static final int DEVICES = 102;
    private static final int DEVICE = 103;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher()
    {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(BluetoothDeviceContract.CONTENT_AUTHORITY, BluetoothDeviceContract.PATH_DEVICE, DEVICES);
        matcher.addURI(BluetoothDeviceContract.CONTENT_AUTHORITY, BluetoothDeviceContract.PATH_DEVICE + "/#", DEVICE);
        matcher.addURI(BluetoothDeviceContract.CONTENT_AUTHORITY, BluetoothDeviceContract.PATH_MESSAGE, MESSAGES);
        matcher.addURI(BluetoothDeviceContract.CONTENT_AUTHORITY, BluetoothDeviceContract.PATH_MESSAGE + "/#", MESSAGE);
        return matcher;
    }


    @Override
    public String getType(Uri uri)
    {
        switch (uriMatcher.match(uri))
        {
            case DEVICE:
                return BluetoothDeviceContract.CONTENT_DEVICE_TYPE;
            case DEVICES:
                return BluetoothDeviceContract.CONTENT_DEVICES_TYPE;
            case MESSAGE:
                return BluetoothDeviceContract.CONTENT_MESSAGE_TYPE;
            case MESSAGES:
                return BluetoothDeviceContract.CONTENT_MESSAGES_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        Uri ret;
        switch (uriMatcher.match(uri))
        {
            case DEVICES:
                ret = insertDevice(uri, values);
                break;
            case MESSAGES:
                ret = insertMessage(uri, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        context.getContentResolver().notifyChange(uri, null);
        return ret;
    }

    private Uri insertDevice(Uri uri, ContentValues values)
    {
        long id = sqLiteHelper.getWritableDatabase().insert(BluetoothDeviceContract.DeviceEntry.TABLE_NAME, null, values);
        if (id > 0)
            return BluetoothDeviceContract.DeviceEntry.buildUri(id);
        else
            throw new android.database.SQLException
                    ("Failed to insert row into "
                            + uri);

    }

    private Uri insertMessage(Uri uri, ContentValues values)
    {
        long id = sqLiteHelper.getWritableDatabase().insert(BluetoothDeviceContract.MessageEntry.TABLE_NAME, null, values);
        if (id > 0)
            return BluetoothDeviceContract.MessageEntry.buildUri(id);
        else
            throw new android.database.SQLException
                    ("Failed to insert row into "
                            + uri);

    }

    @Override
    public boolean onCreate()
    {

        this.context = getContext();
        sqLiteHelper = new BluetoothDevicesSQLiteHelper(context);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        Cursor cursor;
        switch (uriMatcher.match(uri))
        {
            case DEVICE:
                cursor = queryDevice(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case DEVICES:
                cursor = queryDevices(projection, selection, selectionArgs, sortOrder);
                break;
            case MESSAGE:
                cursor = queryMessage(uri, projection, selection, selectionArgs, sortOrder);
                break;
            case MESSAGES:
                cursor = queryMessages(projection, selection, selectionArgs, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    private Cursor queryDevice(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        selection = addSelectionArgs(selection, selectionArgs, " AND ");
        selection = addKeyIdCheckToWhereStatement(selection, ContentUris.parseId(uri));
        return sqLiteHelper.getReadableDatabase().query(BluetoothDeviceContract.DeviceEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }


    private Cursor queryDevices(String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        selection = addSelectionArgs(selection, selectionArgs, " OR ");
        return sqLiteHelper.getReadableDatabase().query(BluetoothDeviceContract.DeviceEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }


    private Cursor queryMessage(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        selection = addSelectionArgs(selection, selectionArgs, " AND ");
        selection = addKeyIdCheckToWhereStatement(selection, ContentUris.parseId(uri));
        return sqLiteHelper.getReadableDatabase().query(BluetoothDeviceContract.MessageEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }


    private Cursor queryMessages(String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        selection = addSelectionArgs(selection, selectionArgs, " OR ");
        return sqLiteHelper.getReadableDatabase().query(BluetoothDeviceContract.MessageEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {
        int returnCount = updateData(uri, values, selection, selectionArgs);
        if (returnCount > 0)
        {
            context.getContentResolver().notifyChange(uri, null);
        }
        return returnCount;
    }

    public int updateData(Uri uri, ContentValues values, String selection,
                          String[] selectionArgs)
    {
        switch (uriMatcher.match(uri))
        {
            case DEVICE:
                return updateDevice(uri, values, selection, selectionArgs);
            case DEVICES:
                return updateDevices(values, selection, selectionArgs);
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
    }

    private int updateDevices(ContentValues values, String selection, String[] selectionArgs)
    {

        selection = addSelectionArgs(selection, selectionArgs, " OR ");
        return sqLiteHelper.getWritableDatabase().update(BluetoothDeviceContract.DeviceEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    private int updateDevice(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {

        selection = addSelectionArgs(selection, selectionArgs, " OR ");
        return sqLiteHelper.getWritableDatabase().update(BluetoothDeviceContract.DeviceEntry.TABLE_NAME, values, addKeyIdCheckToWhereStatement(selection, ContentUris.parseId(uri)), selectionArgs);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs)
    {
        int returnCount = deleteData(uri, selection, selectionArgs);
        if (returnCount > 0)
        {
            context.getContentResolver().notifyChange(uri, null);
        }
        return returnCount;
    }

    public int deleteData(Uri uri, String selection, String[] selectionArgs)
    {
        switch (uriMatcher.match(uri))
        {
            case DEVICE:
                return deleteDevice(uri, selection, selectionArgs);
            case DEVICES:
                return deleteDevices(selection, selectionArgs);
            case MESSAGE:
                return deleteMessage(uri, selection, selectionArgs);
            case MESSAGES:
                return deleteMessages(selection, selectionArgs);
            default:
                throw new UnsupportedOperationException("Unknown uri:" + uri);
        }
    }

    private int deleteMessages(String selection, String[] selectionArgs)
    {
        selection = addSelectionArgs(selection, selectionArgs, " OR ");
        return sqLiteHelper.getWritableDatabase().delete(BluetoothDeviceContract.MessageEntry.TABLE_NAME, selection, selectionArgs);
    }

    private int deleteMessage(Uri uri, String selection, String[] selectionArgs)
    {
        selection = addSelectionArgs(selection, selectionArgs, " OR ");
        return sqLiteHelper.getWritableDatabase().delete(BluetoothDeviceContract.MessageEntry.TABLE_NAME, addKeyIdCheckToWhereStatement(selection, ContentUris.parseId(uri)), selectionArgs);

    }

    private int deleteDevices(String selection, String[] selectionArgs)
    {
        selection = addSelectionArgs(selection, selectionArgs, " OR ");
        return sqLiteHelper.getWritableDatabase().delete(BluetoothDeviceContract.DeviceEntry.TABLE_NAME, selection, selectionArgs);

    }

    private int deleteDevice(Uri uri, String selection, String[] selectionArgs)
    {
        selection = addSelectionArgs(selection, selectionArgs, " OR ");
        return sqLiteHelper.getWritableDatabase().delete(BluetoothDeviceContract.DeviceEntry.TABLE_NAME, addKeyIdCheckToWhereStatement(selection, ContentUris.parseId(uri)), selectionArgs);
    }


    private String addSelectionArgs(String selection,
                                    String[] selectionArgs,
                                    String operation)
    {
        // Handle the "null" case.
        if (selection == null
                || selectionArgs == null)
            return null;
        else
        {
            String selectionResult = "";

            // Properly add the selection args to the selectionResult.
            for (int i = 0;
                 i < selectionArgs.length - 1;
                 ++i)
                selectionResult += (selection
                        + " = ? "
                        + operation
                        + " ");

            // Handle the final selection case.
            selectionResult += (selection
                    + " = ?");

            return selectionResult;
        }

    }

    private static String addKeyIdCheckToWhereStatement(String whereStatement,
                                                        long id)
    {
        String newWhereStatement;
        if (TextUtils.isEmpty(whereStatement))
            newWhereStatement = "";
        else
            newWhereStatement = whereStatement + " AND ";

        // Append the key id to the end of the WHERE statement.
        return newWhereStatement
                + BaseColumns._ID
                + " = '"
                + id
                + "'";
    }

}
