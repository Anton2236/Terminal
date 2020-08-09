package com.axotsoft.wicket.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.axotsoft.wicket.bluetooth.BluetoothConnectionHelperService;
import com.axotsoft.wicket.devices.DeviceRecord;
import com.axotsoft.wicket.realm.Database;

public class WidgetClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Database database = new Database();
        int widgetId = intent.getIntExtra("id", AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            WidgetRecord widgetData = database.getWidget(widgetId);
            DeviceRecord deviceRecord = widgetData.getDeviceRecord();
            context.startForegroundService(BluetoothConnectionHelperService.makeMessageCommandIntent(context, deviceRecord.getAddress(), widgetData.getPattern()));
        }
    }

    public static PendingIntent makeIntent(Context context, int widgetId) {
        return PendingIntent.getBroadcast(context, widgetId, new Intent(context, WidgetClickReceiver.class).putExtra("id", widgetId), PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
