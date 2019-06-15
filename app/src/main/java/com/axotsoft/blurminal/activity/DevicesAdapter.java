package com.axotsoft.blurminal.activity;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axotsoft.blurminal.R;

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
        private View view;
        private View plusView;
        private TextView deviceNameText;
        private TextView deviceAddressText;

        public ViewHolder(@NonNull View itemView, DeviceActionConsumer deviceActionConsumer)
        {
            super(itemView);
            view = itemView;
            plusView = itemView.findViewById(R.id.add_button);
            deviceNameText = itemView.findViewById(R.id.device_name);
            deviceAddressText = itemView.findViewById(R.id.device_address);
            this.deviceActionConsumer = deviceActionConsumer;
        }

        public void setData(DeviceData deviceData)
        {
            this.data = deviceData;
            if (data.isSaved())
            {
                view.setOnClickListener(this::onClick);
                view.setActivated(true);
                plusView.setVisibility(View.GONE);
            }
            else
            {
                view.setOnClickListener(null);
                view.setActivated(false);
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
