package com.axotsoft.terb.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class BluetoothConnectionHelper
{
    private Context context;
    private BluetoothAdapter adapter;
    private Messenger bluetoothMessenger;

    private ServiceConnection serviceConnection = null;
    private BroadcastReceiver stateChangedReceiver = null;

    private ServiceConnection getServiceConnection()
    {
        return new ServiceConnection()
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
                serviceConnection = null;
            }
        };
    }


    public BluetoothConnectionHelper(Context context)
    {
        this.context = context;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect(String deviceAddress, Handler callbackHandler)
    {
        checkAdapterEnabled(() ->
        {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            Intent intent = BluetoothConnectionService.makeIntent(context, deviceAddress, callbackHandler);
            serviceConnection = getServiceConnection();
            context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        });
    }

    public boolean sendMessage(String message, LINE_ENDING_TYPE endingType)
    {
        boolean ret = false;
        if (bluetoothMessenger != null)
        {
            send(BluetoothConnectionHandler.getSendMessage(message, endingType));
            ret = true;
        }
        return ret;
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

    public void unbind()
    {
        if (serviceConnection != null)
        {
            context.unbindService(serviceConnection);
            serviceConnection = null;
        }
    }


    private void checkAdapterEnabled(Runnable runnable)
    {
        if (!adapter.isEnabled())
        {
            adapter.enable();
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            stateChangedReceiver = new StateChangedReceiver(runnable);
            context.registerReceiver(stateChangedReceiver, intentFilter);
        }
        else
        {
            runnable.run();
        }
    }

    public void unregisterReceivers()
    {
        if (stateChangedReceiver != null)
        {
            context.unregisterReceiver(stateChangedReceiver);
        }
    }

    private class StateChangedReceiver extends BroadcastReceiver
    {
        private Runnable runnable;

        private StateChangedReceiver(Runnable runnable)
        {
            this.runnable = runnable;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (BluetoothAdapter.getDefaultAdapter().getState() == BluetoothAdapter.STATE_ON)
            {
                runnable.run();
                context.unregisterReceiver(stateChangedReceiver);
                stateChangedReceiver = null;

            }
        }
    }


}
