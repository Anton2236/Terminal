package com.axotsoft.blurminal.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothWidgetClickReceiver extends BroadcastReceiver
{

    private static final String EXTRA_ADDRESS = "EXTRA_ADDRESS";
    private static final String EXTRA_COMMAND = "EXTRA_COMMAND";
    private static final String EXTRA_WIDGET_ID = "EXTRA_WIDGET_ID";

    @Override
    public void onReceive(Context context, Intent intent)
    {

    }

    public static Intent makeIntent(Context context, BluetoothWidgetData widgetData)
    {
        Intent intent = new Intent(context, BluetoothWidgetClickReceiver.class);
        intent.putExtra(EXTRA_ADDRESS, widgetData.getMacAddress());
        intent.putExtra(EXTRA_COMMAND, widgetData.getCommand());
        intent.putExtra(EXTRA_WIDGET_ID, widgetData.getWidgetId());
        return intent;
    }
}
