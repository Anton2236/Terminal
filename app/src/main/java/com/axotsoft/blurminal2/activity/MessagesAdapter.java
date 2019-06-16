package com.axotsoft.blurminal2.activity;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axotsoft.blurminal2.R;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder>
{
    private List<BluetoothMessageData> records;

    public MessagesAdapter(List<BluetoothMessageData> records)
    {
        this.records = records;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.message_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i)
    {
        viewHolder.setData(records.get(i));
    }

    @Override
    public int getItemCount()
    {
        return records.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView messageText;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);

        }

        void setData(BluetoothMessageData bluetoothMessageData)
        {
            messageText.setText(bluetoothMessageData.getMessage());
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(messageText.getLayoutParams());
            if (!bluetoothMessageData.isFromDevice())
            {
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                messageText.setBackgroundResource(R.drawable.my_message);
            }
            else if (!bluetoothMessageData.isError())
            {
                params.startToStart = ConstraintLayout.LayoutParams.UNSET;
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                messageText.setBackgroundResource(R.drawable.device_message);
            }
            else
            {
                params.startToStart = ConstraintLayout.LayoutParams.UNSET;
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                messageText.setBackgroundResource(R.drawable.device_message_error);
            }
            messageText.setLayoutParams(params);
        }
    }
}
