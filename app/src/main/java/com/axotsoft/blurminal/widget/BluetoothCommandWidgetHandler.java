package com.axotsoft.blurminal.widget;

import android.content.Context;

import com.axotsoft.blurminal.bluetooth.AbstractBluetoothCallbackHandler;
import com.axotsoft.blurminal.bluetooth.BluetoothConnectionHelper;

public class BluetoothCommandWidgetHandler extends AbstractBluetoothCallbackHandler
{
    private Context context;
    private BluetoothWidgetData widgetData;
    private long disconnectTime = -1;
    private Runnable onDisconnect;
    private boolean connected;

    protected BluetoothCommandWidgetHandler(Context context, BluetoothWidgetData widgetData, Runnable onDisconnect)
    {
        super(null);
        this.context = context;
        this.widgetData = widgetData;
        this.onDisconnect = onDisconnect;
        connected = false;
    }

    @Override
    protected void OnError(Exception e)
    {

    }

    @Override
    protected void OnDisconnect()
    {
        BluetoothCommandWidgetUtils.switchWidgetState(context, widgetData.getWidgetId(), BluetoothWidgetData.STATE_IDLE);
        this.onDisconnect.run();
        connected = false;
    }

    @Override
    protected void processMessage(String msg)
    {
        disconnectTime = System.currentTimeMillis() + 20000;
        postAtTime(() ->
        {
            if (System.currentTimeMillis() >= disconnectTime)
            {
                helper.disconnect();
            }
        }, disconnectTime);
    }

    @Override
    protected void onConnect()
    {
        BluetoothCommandWidgetUtils.switchWidgetState(context, widgetData.getWidgetId(), BluetoothWidgetData.STATE_CONNECTED);
        helper.sendMessage(widgetData.getCommand(), widgetData.getLineEnding());
        connected = true;
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
        }
    }
}
