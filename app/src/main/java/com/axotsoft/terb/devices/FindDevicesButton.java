package com.axotsoft.terb.devices;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.axotsoft.terb.R;
import com.axotsoft.terb.utils.ContextAdapter;

public class FindDevicesButton {

    private TextView button;
    private ContextAdapter contextAdapter;

    public FindDevicesButton(ContextAdapter contextAdapter, TextView button) {
        this.contextAdapter = contextAdapter;
        this.button = button;
        registerBroadcastConsumers();
    }

    private void registerBroadcastConsumers() {
        contextAdapter.registerBroadcastConsumer(BluetoothAdapter.ACTION_DISCOVERY_STARTED, this::updateState);
        contextAdapter.registerBroadcastConsumer(BluetoothAdapter.ACTION_DISCOVERY_FINISHED, this::updateState);
        contextAdapter.registerBroadcastConsumer(BluetoothAdapter.ACTION_STATE_CHANGED, this::updateState);
    }

    private void updateState(Context context, Intent intent) {
        updateState();
    }

    public void updateState() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Context context = contextAdapter.getContext();
        if (adapter.isEnabled()) {
            if (adapter.isDiscovering()) {
                button.setText(context.getText(R.string.searching));
            }
            else {
                button.setText(context.getText(R.string.find_devices));
            }
        }
        else {
            button.setText(context.getText(R.string.enable_bluetooth));
        }
    }
}
