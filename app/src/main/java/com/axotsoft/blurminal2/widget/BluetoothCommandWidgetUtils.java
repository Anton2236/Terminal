package com.axotsoft.blurminal2.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import static android.content.Context.MODE_PRIVATE;

public class BluetoothCommandWidgetUtils
{

    private static final String PREFS_NAME = "com.axotsoft.blurminal.widget.BluetoothCommandWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    static void saveWidgetPreferences(Context context, BluetoothWidgetData data)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        Gson gson = new Gson();

        String json = gson.toJson(data, new TypeToken<BluetoothWidgetData>()
        {
        }.getType());
        prefs.putString(PREF_PREFIX_KEY + data.getWidgetId(), json);
        prefs.apply();
    }

    static void switchWidgetState(Context context, int appWidgetId, int state)
    {
        BluetoothWidgetData data = getWidgetPreferences(context, appWidgetId);
        if (data != null)
        {
            data.setState(state);
            saveWidgetPreferences(context, data);
        }
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        BluetoothCommandWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static BluetoothWidgetData getWidgetPreferences(Context context, int appWidgetId)
    {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String json = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);

        if (json != null)
        {
            Gson gson = new Gson();
            return gson.fromJson(json, new TypeToken<BluetoothWidgetData>()
            {
            }.getType());
        }
        else
        {
            return null;
        }
    }

    static void deleteWidgetPreferences(Context context, int appWidgetId)
    {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }
}
