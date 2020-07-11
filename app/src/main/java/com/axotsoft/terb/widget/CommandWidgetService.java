package com.axotsoft.terb.widget;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandWidgetService extends Service {

    private static final String EXTRA_WIDGET_ID = "EXTRA_WIDGET_ID";


    private Map<Integer, BluetoothCommandWidgetHandler> handlerMap;

    @Override
    public void onCreate() {
        super.onCreate();
        handlerMap = new ConcurrentHashMap<>();
        String CHANNEL_ID = "chanel_widget";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Widget Chanel",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableVibration(false);
        channel.setSound(null, null);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Blurminal")
                .setContentText("Bluetooth connection")
                .build();
        startForeground(1, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            BluetoothCommandWidgetHandler handler;
            BluetoothWidgetData widgetData = BluetoothCommandWidgetUtils.getWidgetPreferences(getApplicationContext(), widgetId);
            handler = handlerMap.get(widgetId);
            if (handler == null) {
                handler = new BluetoothCommandWidgetHandler(getApplicationContext(), widgetData, () -> handlerMap.remove(widgetId));
                handlerMap.put(widgetId, handler);
            }
            handler.addRunnable(() -> stopSelf(startId));
            handler.trySendMessage();
        }
        else {
            stopSelf(startId);
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static Intent makeIntent(Context context, int widgetId) {
        Intent intent = new Intent(context, CommandWidgetService.class);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        return intent;
    }

}
