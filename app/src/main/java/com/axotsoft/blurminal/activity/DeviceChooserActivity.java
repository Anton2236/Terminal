package com.axotsoft.blurminal.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.axotsoft.blurminal.R;
import com.axotsoft.blurminal.widget.BluetoothCommandWidgetConfigureActivity;

public class DeviceChooserActivity extends AppCompatActivity
{


    public static final int REQUEST_DEVICE = 1;

    public static Intent makeIntent(Context context)
    {
        return new Intent(context, DeviceChooserActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_chooser);
    }
}
