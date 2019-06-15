package com.axotsoft.blurminal.bluetooth;

import android.os.Handler;
import android.os.Message;

import static com.axotsoft.blurminal.bluetooth.BluetoothConnectionHandler.*;

public abstract class AbstractBluetoothCallbackHandler extends Handler
{
    protected BluetoothConnectionHelper helper;

    protected AbstractBluetoothCallbackHandler(BluetoothConnectionHelper helper)
    {
        this.helper = helper;
    }

    @Override
    public void handleMessage(Message msg)
    {
        int status = msg.arg1;
        switch (status)
        {
            case STATUS_OK:
            {
                processMessage((String) msg.obj);
                break;
            }
            case STATUS_CONNECTED:
            {
                onConnect();
                break;
            }
            case STATUS_DISCONNECTED:
            {
                helper.unbind();
                onDisconnect();
                break;
            }
            case STATUS_ERROR:
            {
                onError((Exception) msg.obj);
                break;
            }
        }
    }

    protected abstract void onError(Exception e);

    protected abstract void onDisconnect();

    protected abstract void processMessage(String msg);

    protected abstract void onConnect();
}
