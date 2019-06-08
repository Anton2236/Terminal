package com.axotsoft.blurminal.bluetooth;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;

public class BluetoothConnectionService extends Service
{
    private final static String EXTRA_MESSENGER = "MESSENGER";

    @Override
    public IBinder onBind(Intent intent)
    {
        BluetoothConnectionHandler handler = new BluetoothConnectionHandler(getApplicationContext(), intent.getDataString(), intent.getParcelableExtra(EXTRA_MESSENGER));
        return new Messenger(handler).getBinder();
    }

    public static Intent makeIntent(Context context, String deviceAddress, Handler callBackHandler)
    {
        return new Intent(context, BluetoothConnectionService.class).setData(Uri.parse(deviceAddress)).putExtra(EXTRA_MESSENGER, new Messenger(callBackHandler));
    }
}
