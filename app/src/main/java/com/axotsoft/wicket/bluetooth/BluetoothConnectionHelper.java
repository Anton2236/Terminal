package com.axotsoft.wicket.bluetooth;

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

public class BluetoothConnectionHelper {
    private Context context;
    private BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    private Messenger bluetoothMessenger;

    private ServiceConnection serviceConnection = null;
    private BroadcastReceiver stateChangedReceiver = null;
    private int latestStartId = 0;

    public BluetoothConnectionHelper(Context context) {
        this.context = context;
    }

    private ServiceConnection getServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bluetoothMessenger = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                bluetoothMessenger = null;
                serviceConnection = null;
            }
        };
    }

    public void connect(String deviceAddress, Handler callbackHandler) {
        runOnEnabledAdapter(() ->
        {
            if (adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }
            Intent intent = BluetoothConnectionService.makeIntent(context, deviceAddress, callbackHandler);
            serviceConnection = getServiceConnection();
            context.bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        });
    }

    public boolean send(Message msg) {
        boolean success = false;
        try {
            if (bluetoothMessenger != null) {
                bluetoothMessenger.send(msg);
                success = true;
            }
        } catch (RemoteException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        return success;
    }

    public void unbind() {
        if (serviceConnection != null) {
            context.unbindService(serviceConnection);
            serviceConnection = null;
        }
    }

    private void runOnEnabledAdapter(Runnable runnable) {
        if (adapter.isEnabled()) {
            runnable.run();
        }
        else {
            IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            stateChangedReceiver = new StateChangedReceiver(runnable);
            context.registerReceiver(stateChangedReceiver, intentFilter);
            adapter.enable();
        }
    }

    public void unregisterStateChangedReceiver() {
        if (stateChangedReceiver != null) {
            context.unregisterReceiver(stateChangedReceiver);
            stateChangedReceiver = null;
        }
    }


    private class StateChangedReceiver extends BroadcastReceiver {
        private Runnable runnable;

        private StateChangedReceiver(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            if (state == BluetoothAdapter.STATE_ON) {
                runnable.run();
                unregisterStateChangedReceiver();
            }
        }
    }


}
