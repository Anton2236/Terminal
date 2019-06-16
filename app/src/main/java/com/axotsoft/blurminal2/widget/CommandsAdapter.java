package com.axotsoft.blurminal2.widget;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.axotsoft.blurminal2.R;

import java.util.List;

public class CommandsAdapter extends RecyclerView.Adapter<CommandsAdapter.ViewHolder>
{
    private List<String> commands;
    private CommandConsumer consumer;
    private CommandConsumer removeConsumer;
    private int layoutId;

    public CommandsAdapter(List<String> commands, CommandConsumer consumer, CommandConsumer removeConsumer, @LayoutRes int layoutId)
    {
        this.commands = commands;
        this.consumer = consumer;
        this.removeConsumer = removeConsumer;
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(layoutId, viewGroup, false);
        return new ViewHolder(view, removeConsumer, consumer);
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
        private final CommandConsumer selectConsumer;
        private final CommandConsumer removeConsumer;
        private TextView textView;
        private String command;

        ViewHolder(@NonNull View itemView, CommandConsumer removeConsumer, CommandConsumer selectConsumer)
        {
            super(itemView);
            this.selectConsumer = selectConsumer;
            this.removeConsumer = removeConsumer;
            textView = itemView.findViewById(R.id.command);
            itemView.setOnClickListener(this::onClick);
            if (removeConsumer != null)
            {
                itemView.setOnLongClickListener(this::OnLongClick);
            }
            else
            {
                itemView.setOnLongClickListener(null);
            }
        }

        private boolean OnLongClick(View view)
        {
            removeConsumer.accept(command);
            return true;
        }

        private void onClick(View view)
        {
            if (selectConsumer != null)
            {
                selectConsumer.accept(command);
            }
        }

        void setData(String s)
        {
            command = s;
            textView.setText(command);
        }
    }
}
