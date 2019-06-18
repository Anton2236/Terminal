package com.axotsoft.blurminal2.widget;

import android.content.Context;
import android.os.SystemClock;

import com.axotsoft.blurminal2.bluetooth.AbstractBluetoothCallbackHandler;
import com.axotsoft.blurminal2.bluetooth.BluetoothConnectionHelper;

public class BluetoothCommandWidgetHandler extends AbstractBluetoothCallbackHandler
{
    private Context context;
    private BluetoothWidgetData widgetData;
    private long disconnectTime = -1;
    private Runnable preOnDisconnect;
    private Runnable onDisconnect;
    private boolean connected;

    private final Object sync = new Object();

    protected BluetoothCommandWidgetHandler(Context context, BluetoothWidgetData widgetData, Runnable preOnDisconnect)
    {
        super(null);
        this.context = context;
        this.widgetData = widgetData;
        this.preOnDisconnect = preOnDisconnect;
        connected = false;
    }

    public void addRunnable(Runnable runnable)
    {
        synchronized (sync)
        {
            if (widgetData != null)
            {
                Runnable oldRunnable = onDisconnect;
                this.onDisconnect = () ->
                {
                    if (oldRunnable != null)
                    {
                        oldRunnable.run();
                    }
                    runnable.run();
                };
            }
            else
            {
                runnable.run();
            }
        }
    }

    @Override
    protected void onError(Exception e)
    {

    }

    @Override
    protected void onDisconnect()
    {
        if (helper != null)
        {
            helper.unbind();
            BluetoothCommandWidgetUtils.switchWidgetState(context, widgetData.getWidgetId(), BluetoothWidgetData.STATE_IDLE);
            this.preOnDisconnect.run();
            synchronized (sync)
            {
                this.onDisconnect.run();
                widgetData = null;
            }
            connected = false;
            helper = null;
        }
    }

    @Override
    protected void processMessage(String msg)
    {
        updateDisconnectTime();
    }

    private void updateDisconnectTime()
    {
        disconnectTime = SystemClock.uptimeMillis() + 15000;
        postAtTime(() ->
        {
            if (SystemClock.uptimeMillis() >= disconnectTime)
            {
                if (helper != null)
                {
                    helper.disconnect();
                }
                connected = false;
            }
        }, disconnectTime);
    }

    @Override
    protected void onConnect()
    {
        BluetoothCommandWidgetUtils.switchWidgetState(context, widgetData.getWidgetId(), BluetoothWidgetData.STATE_CONNECTED);
        connected = true;
        updateDisconnectTime();
        postAtTime(() ->
                helper.sendMessage(widgetData.getCommand(), widgetData.getLineEnding()), SystemClock.uptimeMillis() + 1000);
    }

    public void trySendMessage()
    {
        if (!connected)
        {
            if (this.helper == null)
            {
                this.helper = new BluetoothConnectionHelper(context);
                helper.connect(widgetData.getMacAddress(), this);
            }
        }
        else
        {
            helper.sendMessage(widgetData.getCommand(), widgetData.getLineEnding());
            updateDisconnectTime();
        }
    }
}
