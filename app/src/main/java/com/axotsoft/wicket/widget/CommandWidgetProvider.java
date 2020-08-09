package com.axotsoft.wicket.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.axotsoft.wicket.R;
import com.axotsoft.wicket.bluetooth.ConnectionRecord;
import com.axotsoft.wicket.devices.DeviceRecord;
import com.axotsoft.wicket.realm.Database;

public class CommandWidgetProvider extends AppWidgetProvider {


    public static void onConnectionStateChanged(Context context, DeviceRecord deviceRecord) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        deviceRecord.getWidgets().forEach(widgetRecord -> {
            CommandWidgetProvider.updateAppWidget(context, appWidgetManager, widgetRecord.getWidgetId());
        });
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       int appWidgetId) {
        Database database = new Database();
        WidgetRecord widgetRecord = database.getWidget(appWidgetId);
        boolean connected = false;
        // Construct the RemoteViews object
        if (widgetRecord != null) {
            ConnectionRecord connectionRecord = database.getConnection();
            if (connectionRecord != null && widgetRecord.getDeviceRecord().equals(connectionRecord.getDeviceRecord())) {
                connected = connectionRecord.isConnected();
            }

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bluetooth_command_widget);
            views.setTextViewText(R.id.appwidget_text, widgetRecord.getWidgetTitle());
            int color = connected ? R.color.colorGreen : R.color.colorBlack;
            views.setTextColor(R.id.appwidget_text, context.getColor(color));
            views.setOnClickPendingIntent(R.id.appwidget_button, WidgetClickReceiver.makeIntent(context, widgetRecord.getWidgetId()));
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Database database = new Database();
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WidgetRecord widgetRecord = database.getWidget(appWidgetId);
            if (widgetRecord != null) {
                database.execute(realm -> widgetRecord.deleteFromRealm());
            }
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        Database database = new Database();
        for (int i = 0; i < oldWidgetIds.length; i++) {
            WidgetRecord widgetRecord = database.getWidget(oldWidgetIds[i]);
            if (newWidgetIds.length > i) {
                if (widgetRecord != null) {
                    final int newId = newWidgetIds[i];
                    database.execute(realm -> widgetRecord.setId(newId));
                }
            }
            else {
                if (widgetRecord != null) {
                    database.execute(realm -> widgetRecord.deleteFromRealm());
                }
            }
        }
    }
}

