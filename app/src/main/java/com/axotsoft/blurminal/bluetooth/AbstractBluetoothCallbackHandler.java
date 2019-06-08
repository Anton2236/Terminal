package com.axotsoft.blurminal.bluetooth;

import android.os.Handler;
import android.os.Message;

import static com.axotsoft.blurminal.bluetooth.BluetoothConnectionHandler.*;

public abstract class AbstractBluetoothCallbackHandler extends Handler
{
    private BluetoothConnectionHelper helper;

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
                OnDisconnect();
                break;
            }
            case STATUS_ERROR:
            {
                OnError((Exception) msg.obj);
                break;
            }
        }
    }

    protected abstract void OnError(Exception e);

    protected abstract void OnDisconnect();

    protected abstract void processMessage(String msg);

    protected abstract void onConnect();
}
