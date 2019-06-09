package com.axotsoft.blurminal.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.axotsoft.blurminal.R;

import static com.axotsoft.blurminal.widget.BluetoothCommandWidgetUtils.deleteWidgetPreferences;
import static com.axotsoft.blurminal.widget.BluetoothCommandWidgetUtils.getWidgetPreferences;
import static com.axotsoft.blurminal.widget.BluetoothCommandWidgetUtils.saveWidgetPreferences;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link BluetoothCommandWidgetConfigureActivity BluetoothCommandWidgetConfigureActivity}
 */
public class BluetoothCommandWidget extends AppWidgetProvider
{

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {
        BluetoothWidgetData widgetData = getWidgetPreferences(context, appWidgetId);
        // Construct the RemoteViews object
        if (widgetData != null)
        {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bluetooth_command_widget);
            views.setTextViewText(R.id.appwidget_text, widgetData.getWidgetTitle());
            views.setTextColor(R.id.appwidget_text, widgetData.getState() == BluetoothWidgetData.STATE_CONNECTED ? Color.BLUE : Color.WHITE);
            views.setOnClickFillInIntent(R.id.appwidget_button, CommandWidgetService.makeIntent(context, widgetData));
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds)
        {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds)
        {
            deleteWidgetPreferences(context, appWidgetId);
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds)
    {
        for (int i = 0; i < oldWidgetIds.length; i++)
        {
            if (newWidgetIds.length > i)
            {
                BluetoothWidgetData widgetPreferences = getWidgetPreferences(context, oldWidgetIds[i]);
                deleteWidgetPreferences(context, oldWidgetIds[i]);
                if (widgetPreferences != null)
                {
                    widgetPreferences.setId(newWidgetIds[i]);
                    saveWidgetPreferences(context, widgetPreferences);
                }
            }
            else
            {
                deleteWidgetPreferences(context, oldWidgetIds[i]);
            }
        }
    }
}
