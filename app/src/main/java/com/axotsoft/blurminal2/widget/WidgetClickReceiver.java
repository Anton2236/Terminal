package com.axotsoft.blurminal2.widget;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class WidgetClickReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        context.startForegroundService(CommandWidgetService.makeIntent(context, intent.getIntExtra("id", -1)));
    }

    public static PendingIntent makeIntent(Context context, int widgetId)
    {
        return PendingIntent.getBroadcast(context, 0, new Intent(context, WidgetClickReceiver.class).putExtra("id", widgetId), PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
