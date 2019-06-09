package com.axotsoft.blurminal.utils;

import android.content.Context;
import android.widget.Toast;

public final class UiUtils
{
    public static void makeToast(Context context, String text)
    {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void makeLongToast(Context context, String text)
    {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
