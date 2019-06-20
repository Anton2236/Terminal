package com.axotsoft.blurminal2.activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axotsoft.blurminal2.R;

import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder>
{
    private DeviceActionConsumer deviceActionConsumer;
    private List<DeviceData> devices;

    public DevicesAdapter(DeviceActionConsumer deviceActionConsumer, List<DeviceData> devices)
    {
        this.deviceActionConsumer = deviceActionConsumer;
        this.devices = devices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.device_layout, viewGroup, false);
        return new ViewHolder(view, deviceActionConsumer);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i)
    {
        viewHolder.setData(devices.get(i));
    }

    @Override
    public int getItemCount()
    {
        return devices.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private DeviceData data;
        private DeviceActionConsumer deviceActionConsumer;
        private View plusView;
        private View container;
        private TextView deviceNameText;
        private TextView deviceAddressText;

        public ViewHolder(@NonNull View itemView, DeviceActionConsumer deviceActionConsumer)
        {
            super(itemView);
            plusView = itemView.findViewById(R.id.add_button);
            container = itemView.findViewById(R.id.device_button);
            deviceNameText = itemView.findViewById(R.id.device_name);
            deviceAddressText = itemView.findViewById(R.id.device_address);
            this.deviceActionConsumer = deviceActionConsumer;
        }

        public void setData(DeviceData deviceData)
        {
            this.data = deviceData;
            if (data.isSaved())
            {
                container.setOnClickListener(this::onClick);
                container.setBackgroundResource(R.drawable.simple_button_selector);
                plusView.setVisibility(View.GONE);
            }
            else
            {
                container.setOnClickListener(null);
                container.setBackgroundResource(R.drawable.simple_button_inactive);
                plusView.setOnClickListener(this::onClick);
                plusView.setVisibility(View.VISIBLE);
            }
            deviceNameText.setText(deviceData.getName());
            deviceAddressText.setText(deviceData.getAddress());
        }

        public void onClick(View v)
        {
            deviceActionConsumer.accept(data);
        }
    }
}
