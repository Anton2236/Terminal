package com.axotsoft.blurminal.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.axotsoft.blurminal.R;
import com.axotsoft.blurminal.widget.BluetoothCommandWidgetConfigureActivity;

public class DeviceChooserActivity extends AppCompatActivity
{
    public static final int REQUEST_DEVICE = 1;
    private BluetoothStateChangedReceiver stateChangedReceiver;

    @Override
    protected void onResume()
    {
        super.onResume();
        stateChangedReceiver = new BluetoothStateChangedReceiver(this);
        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(stateChangedReceiver, intentFilter);
        onBluetoothStateChanged(BluetoothAdapter.getDefaultAdapter().getState());
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (stateChangedReceiver != null)
        {
            unregisterReceiver(stateChangedReceiver);
        }
    }

    private void onBluetoothStateChanged(int state)
    {

    }


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

    private static class BluetoothStateChangedReceiver extends BroadcastReceiver
    {
        private DeviceChooserActivity activity;

        private BluetoothStateChangedReceiver(DeviceChooserActivity activity)
        {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_DISCONNECTED);
            activity.onBluetoothStateChanged(state);
        }
    }


}
