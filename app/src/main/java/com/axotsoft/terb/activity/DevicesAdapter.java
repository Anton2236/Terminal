package com.axotsoft.terb.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.R;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {
    public static final int DEVICE_NAME_MAX_LENGTH = 20;
    private DeviceActionConsumer deviceActionConsumer;
    private List<DeviceData> devices;

    public DevicesAdapter(DeviceActionConsumer deviceActionConsumer, List<DeviceData> devices) {
        this.deviceActionConsumer = deviceActionConsumer;
        this.devices = devices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.device_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.setData(devices.get(i));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private DeviceData data;
        private View bindView;
        private View container;
        private TextView deviceNameText;
        private TextView deviceAddressText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bindView = itemView.findViewById(R.id.bind_button);
            container = itemView.findViewById(R.id.device_button);
            deviceNameText = itemView.findViewById(R.id.device_name);
            deviceAddressText = itemView.findViewById(R.id.device_address);
        }

        public void setData(DeviceData deviceData) {
            this.data = deviceData;
            if (data.isBonded()) {
                container.setOnClickListener(this::onClick);
                bindView.setVisibility(View.GONE);
            }
            else {
                container.setOnClickListener(null);
                bindView.setOnClickListener(this::onClick);
                bindView.setVisibility(View.VISIBLE);
            }

            if (data.isSaved()) {
                container.setBackgroundResource(R.drawable.simple_button_selector);
            }
            else {
                container.setBackgroundResource(R.drawable.simple_button_inactive);
            }

            String deviceName = deviceData.getName();
            if (deviceName != null && deviceName.length() > DEVICE_NAME_MAX_LENGTH) {
                deviceName = deviceName.substring(0, DEVICE_NAME_MAX_LENGTH) + "...";
            }
            deviceNameText.setText(deviceName);
            deviceAddressText.setText(deviceData.getAddress());
        }

        public void onClick(View v) {
            if (deviceActionConsumer != null) {
                deviceActionConsumer.accept(data);
            }
        }
    }
}
