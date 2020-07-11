package com.axotsoft.terb.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.R;
import com.axotsoft.terb.devices.BluetoothDevicesManager;
import com.axotsoft.terb.utils.ContextAdapter;

public class DeviceChooserActivity extends AppCompatActivity {
    public static final int REQUEST_DEVICE = 1;

    private FindDevicesButton findDevicesButton;
    private BluetoothDevicesManager devicesManager;
    private ContextAdapter contextAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_chooser);

        contextAdapter = new ContextAdapter(this);
        devicesManager = new BluetoothDevicesManager(contextAdapter, this::selectDevice);
        DevicesAdapter devicesAdapter = devicesManager.getDevicesAdapter();
        TextView findButton = findViewById(R.id.find_button);
        findDevicesButton = new FindDevicesButton(contextAdapter, findButton);
        RecyclerView devicesView = findViewById(R.id.devices);
        devicesView.setAdapter(devicesAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        contextAdapter.onResume();
        devicesManager.updateDevices();
        findDevicesButton.updateState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        contextAdapter.onPause();
    }

    private void selectDevice(Uri uri) {
        Intent intent = new Intent();
        intent.setData(uri);
        setResult(RESULT_OK, intent);
        finish();
    }


    public static Intent makeIntent(Context context) {
        return new Intent(context, DeviceChooserActivity.class);
    }


    public void onDiscoverClick(View view) {
        findDevicesButton.onClick(view);
    }
}
