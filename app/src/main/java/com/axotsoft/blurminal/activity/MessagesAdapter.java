package com.axotsoft.blurminal.activity;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axotsoft.blurminal.R;

import org.w3c.dom.Text;

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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
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

        public void setData(BluetoothMessageData bluetoothMessageData)
        {
            messageText.setText(bluetoothMessageData.getMessage());
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(messageText.getLayoutParams());
            if (bluetoothMessageData.isFromDevice())
            {
                params.startToStart = ConstraintLayout.LayoutParams.UNSET;
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                messageText.setBackgroundResource(R.drawable.device_message);
            }
            else
            {
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                messageText.setBackgroundResource(R.drawable.my_message);
            }
            messageText.setLayoutParams(params);
        }
    }
}
