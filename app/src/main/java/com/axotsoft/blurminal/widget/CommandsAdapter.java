package com.axotsoft.blurminal.widget;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axotsoft.blurminal.R;

import java.util.List;

public class CommandsAdapter extends RecyclerView.Adapter<CommandsAdapter.ViewHolder>
{
    private List<String> commands;
    private CommandConsumer consumer;
    private int layoutId;

    public CommandsAdapter(List<String> commands, CommandConsumer consumer, @LayoutRes int layoutId)
    {
        this.commands = commands;
        this.consumer = consumer;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(layoutId, viewGroup, false);
        return new ViewHolder(view, consumer);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i)
    {
        viewHolder.setData(commands.get(i));
    }

    @Override
    public int getItemCount()
    {
        return commands.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        private final CommandConsumer consumer;
        private TextView textView;
        private String command;

        ViewHolder(@NonNull View itemView, CommandConsumer consumer)
        {
            super(itemView);
            this.consumer = consumer;
            textView = itemView.findViewById(R.id.command);
            itemView.setOnClickListener(this::onClick);
        }

        private void onClick(View view)
        {
            if (consumer != null)
            {
                consumer.accept(command);
            }
        }

        void setData(String s)
        {
            command = s;
            textView.setText(command);
        }
    }
}
