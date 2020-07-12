package com.axotsoft.terb.chooser;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.R;
import com.axotsoft.terb.utils.ContextAdapter;

public class DeviceChooserActivity extends AppCompatActivity {
    public static final int REQUEST_DEVICE = 1;

    private static final int REQUEST_LOCATION_TO_START_DISCOVERY = 1;

    private FindDevicesButton findDevicesButton;
    private BluetoothDevicesManager devicesManager;
    private ContextAdapter contextAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_chooser);

        contextAdapter = new ContextAdapter(this);

        RecyclerView devicesView = findViewById(R.id.devices);
        devicesManager = new BluetoothDevicesManager(contextAdapter, devicesView, this::selectDevice);
        TextView findButton = findViewById(R.id.find_button);
        findDevicesButton = new FindDevicesButton(contextAdapter, findButton);
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


    public void onFindClick(View view) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isEnabled()) {
            if (adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }
            else {
                tryStartDiscovery(adapter);
            }
        }
        else {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }
    }

    private void tryStartDiscovery(BluetoothAdapter adapter) {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            adapter.startDiscovery();
        }
        else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_TO_START_DISCOVERY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_TO_START_DISCOVERY) {

        }
    }
}
