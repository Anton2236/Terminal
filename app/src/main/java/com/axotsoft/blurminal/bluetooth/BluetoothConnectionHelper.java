package com.axotsoft.blurminal.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.Set;

public class BluetoothConnectionHelper
{
    private final static String PREFERENCES_NAME = "TERMINAL_PREFERENCES";
    private final static String STRING_ENDING_TYPE_PREFERENCE = "STRING_ENDING_TYPE_PREFERENCE";

    private Context context;
    private BluetoothAdapter adapter;
    private Messenger bluetoothMessenger;
    private STRING_ENDING_TYPE stringEndingType;
    private SharedPreferences preferences;

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            bluetoothMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            bluetoothMessenger = null;
        }
    };

    public BluetoothConnectionHelper(Context context)
    {
        this.context = context;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this::OnPreferencesChanged);
        initStringEnding(preferences, STRING_ENDING_TYPE_PREFERENCE);
    }

    private void OnPreferencesChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals(STRING_ENDING_TYPE_PREFERENCE))
        {
            initStringEnding(sharedPreferences, key);
        }
    }

    private void initStringEnding(SharedPreferences sharedPreferences, String key)
    {
        int typeId = sharedPreferences.getInt(key, STRING_ENDING_TYPE.CRLF.getKey());
        stringEndingType = STRING_ENDING_TYPE.valueOf(typeId);
    }


    public void connect(String deviceAddress, Handler callbackHandler)
    {
        checkAdapterEnabled();
        Intent intent = BluetoothConnectionService.makeIntent(context, deviceAddress, callbackHandler);
        context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    public void sendMessage(String message)
    {
        if (bluetoothMessenger != null)
        {
            send(BluetoothConnectionHandler.getSendMessage(message, stringEndingType));
        }
    }

    private void send(Message msg)
    {
        try
        {
            bluetoothMessenger.send(msg);
        } catch (RemoteException e)
        {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
    }

    public void disconnect()
    {
        if (bluetoothMessenger != null)
        {
            send(BluetoothConnectionHandler.getDisconnectMessage());
        }
    }

    public Set<BluetoothDevice> getBondedDevices()
    {
        checkAdapterEnabled();
        return adapter.getBondedDevices();
    }

    private void checkAdapterEnabled()
    {
        if (!adapter.isEnabled())
        {
            adapter.enable();
        }
    }


}
