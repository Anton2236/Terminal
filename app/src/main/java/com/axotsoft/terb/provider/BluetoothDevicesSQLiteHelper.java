package com.axotsoft.terb.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class BluetoothDevicesSQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "axotsoft_bluetooth_devices_db";
    private static final int VERSION = 1;

    private final String SQL_CREATE_DEVICES_TABLE =
            "CREATE TABLE "
                    + BluetoothDeviceContract.DeviceEntry.TABLE_NAME + " ("
                    + BluetoothDeviceContract.DeviceEntry._ID + " INTEGER PRIMARY KEY, "
                    + BluetoothDeviceContract.DeviceEntry._DEVICE_NAME + " TEXT, "
                    + BluetoothDeviceContract.DeviceEntry._MAC_ADDRESS + " TEXT, "
                    + BluetoothDeviceContract.DeviceEntry._LINE_ENDING + " INTEGER, "
                    + BluetoothDeviceContract.DeviceEntry._COMMANDS + " BLOB "
                    + " );";

    private final String SQL_CREATE_MESSAGES_TABLE =
            "CREATE TABLE "
                    + BluetoothDeviceContract.MessageEntry.TABLE_NAME + " ("
                    + BluetoothDeviceContract.MessageEntry._ID + " INTEGER PRIMARY KEY, "
                    + BluetoothDeviceContract.MessageEntry._DEVICE_ID + " INTEGER, "
                    + BluetoothDeviceContract.MessageEntry._TIME_MILLIS + " INTEGER, "
                    + BluetoothDeviceContract.MessageEntry._MESSAGE + " TEXT, "
                    + BluetoothDeviceContract.MessageEntry._FROM_DEVICE + " INTEGER "
                    + " );";


    BluetoothDevicesSQLiteHelper(Context context) {
        super(context, context.getCacheDir() + File.separator + DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_DEVICES_TABLE);
        db.execSQL(SQL_CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BluetoothDeviceContract.DeviceEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + BluetoothDeviceContract.MessageEntry.TABLE_NAME);
        onCreate(db);
    }
}
