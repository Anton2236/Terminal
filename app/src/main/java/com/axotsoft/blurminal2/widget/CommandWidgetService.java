package com.axotsoft.blurminal2.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.Context;
import android.os.IBinder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandWidgetService extends Service
{

    private static final String EXTRA_WIDGET_ID = "EXTRA_WIDGET_ID";


    private Map<Integer, BluetoothCommandWidgetHandler> handlerMap;

    @Override
    public void onCreate()
    {
        super.onCreate();
        handlerMap = new ConcurrentHashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            BluetoothCommandWidgetHandler handler;
            BluetoothWidgetData widgetData = BluetoothCommandWidgetUtils.getWidgetPreferences(getApplicationContext(), widgetId);
            handler = handlerMap.get(widgetId);
            if (handler == null)
            {
                handler = new BluetoothCommandWidgetHandler(getApplicationContext(), widgetData, () ->
                {
                    handlerMap.remove(widgetId);
                    stopSelf(startId);
                });
                handlerMap.put(widgetId, handler);
            }
            handler.trySendMessage();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    public static PendingIntent makeIntent(Context context, int widgetId)
    {
        Intent intent = new Intent(context, CommandWidgetService.class);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

}
