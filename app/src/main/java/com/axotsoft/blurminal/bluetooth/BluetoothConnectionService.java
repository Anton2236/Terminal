package com.axotsoft.blurminal.bluetooth;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import java.io.IOException;

public class BluetoothConnectionService extends Service
{
    private final static String EXTRA_MESSENGER = "MESSENGER";

    @Override
    public IBinder onBind(Intent intent)
    {
        IBinder binder = null;
        try
        {
            BluetoothConnectionHandler handler = new BluetoothConnectionHandler(intent.getDataString(), (Messenger) intent.getParcelableExtra(EXTRA_MESSENGER));
            binder = new Messenger(handler).getBinder();
        } catch (IOException e)
        {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        return binder;
    }

    public static Intent makeIntent(Context context, String deviceAddress, Handler callBackHandler)
    {
        return new Intent(context, BluetoothConnectionService.class).setData(Uri.parse(deviceAddress)).putExtra(EXTRA_MESSENGER, new Messenger(callBackHandler));
    }
}
