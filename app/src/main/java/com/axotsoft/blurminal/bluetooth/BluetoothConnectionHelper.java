package com.axotsoft.blurminal.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
    }

    public void connect(String deviceAddress, Handler callbackHandler)
    {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        checkAdapterEnabled();
        Intent intent = BluetoothConnectionService.makeIntent(context, deviceAddress, callbackHandler);
        context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    public void sendMessage(String message, LINE_ENDING_TYPE endingType)
    {
        if (bluetoothMessenger != null)
        {
            send(BluetoothConnectionHandler.getSendMessage(message, endingType));
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

    void unbind()
    {
        context.unbindService(serviceConnection);
    }


    private void checkAdapterEnabled()
    {
        if (!adapter.isEnabled())
        {
            adapter.enable();
        }
    }


}
