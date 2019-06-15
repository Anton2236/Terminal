package com.axotsoft.blurminal.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.axotsoft.blurminal.R;
import com.axotsoft.blurminal.bluetooth.AbstractBluetoothCallbackHandler;
import com.axotsoft.blurminal.bluetooth.BluetoothConnectionHelper;
import com.axotsoft.blurminal.bluetooth.LINE_ENDING_TYPE;
import com.axotsoft.blurminal.devices.BluetoothMessagesManager;
import com.axotsoft.blurminal.provider.BluetoothDeviceRecord;
import com.axotsoft.blurminal.provider.BluetoothMessageRecord;
import com.axotsoft.blurminal.widget.CommandsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AbstractDeviceChooserClientActivity
{
    private AbstractBluetoothCallbackHandler handler;
    private BluetoothConnectionHelper helper;

    private BluetoothMessagesManager messagesManager;
    private List<BluetoothMessageData> messages;
    private MessagesAdapter messagesAdapter;
    private RecyclerView messagesView;

    private EditText commandEditText;
    private RecyclerView commandsView;
    private CommandsAdapter commandsAdapter;
    private List<String> availableCommands;

    private LINE_ENDING_TYPE currentLineEnding;
    private TextView lineEndingText;

    private TextView deviceNameText;
    private TextView deviceActionText;

    private View deviceContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        helper = new BluetoothConnectionHelper(this);
        handler = new BluetoothCallbackHandler(helper, this);
        initViews();
        initAdapters();

    }

    private void initViews()
    {
        messagesView = findViewById(R.id.messages);
        commandEditText = findViewById(R.id.command_text);
        commandsView = findViewById(R.id.commands_container);
        lineEndingText = findViewById(R.id.line_ending_chooser);
        deviceNameText = findViewById(R.id.device_name);
        deviceActionText = findViewById(R.id.device_action);
        deviceContainer = findViewById(R.id.device_container);
    }

    private void initAdapters()
    {
        messages = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(messages);
        messagesView.setAdapter(messagesAdapter);
        availableCommands = new ArrayList<>();
        commandsAdapter = new CommandsAdapter(availableCommands, commandEditText::setText, R.layout.main_command_layout);
        commandsView.setAdapter(commandsAdapter);
    }


    @Override
    protected void updateDeviceData(BluetoothDeviceRecord deviceRecord)
    {
        helper.disconnect();
        if (deviceRecord != null)
        {
            messagesManager = new BluetoothMessagesManager(devicesDao, this.deviceRecord);
            messagesManager.getAllMessagesForDevice(this::initMessages);
            initCommands(deviceRecord);
            deviceNameText.setText(deviceRecord.getDeviceName());
            deviceContainer.setVisibility(View.VISIBLE);
            onDisconnect();
        }
        else
        {
            messagesManager = null;
            messages.clear();
            messagesAdapter.notifyDataSetChanged();
            deviceContainer.setVisibility(View.GONE);
            deviceNameText.setText(R.string.select_device);
        }
    }

    private void initCommands(BluetoothDeviceRecord deviceRecord)
    {
        availableCommands.clear();
        availableCommands.addAll(deviceRecord.getCommands());
        commandsAdapter.notifyDataSetChanged();
        if (availableCommands.isEmpty())
        {
            commandsView.setVisibility(View.GONE);
        }
        else
        {
            commandsView.setVisibility(View.VISIBLE);
        }
    }

    private void initMessages(List<BluetoothMessageRecord> messageRecords)
    {
        messages.clear();
        for (BluetoothMessageRecord record : messageRecords)
        {
            messages.add(new BluetoothMessageData(record.getMessage(), record.isFromDevice(), record.getTimeMillis()));
        }
        Collections.sort(messages, new BluetoothMessageData.MessageComparator());
        messagesAdapter.notifyDataSetChanged();
    }

    public void sendButtonClick(View v)
    {
        String command = commandEditText.getText().toString();
        if (!command.isEmpty())
        {
            helper.sendMessage(command, currentLineEnding);
            addMessage(command, false);
        }
    }

    public void addCommand(View v)
    {
        String command = commandEditText.getText().toString();
        if (!command.isEmpty() && deviceRecord != null)
        {
            if (!deviceRecord.getCommands().contains(command))
            {
                deviceRecord.getCommands().add(command);
                devicesDao.updateDevice(deviceRecord);
                availableCommands.add(command);
                commandsAdapter.notifyItemInserted(availableCommands.size() - 1);
                commandsView.setVisibility(View.VISIBLE);
            }
        }
    }


    private void addMessage(String msg, boolean fromDevice)
    {
        if (messagesManager != null)
        {
            messagesManager.addMessage(msg, fromDevice);
            messages.add(new BluetoothMessageData(msg, fromDevice, System.currentTimeMillis()));
            messagesAdapter.notifyItemInserted(messages.size() - 1);
        }
    }

    private void addError(String msg)
    {
        messages.add(new BluetoothMessageData(msg, true, System.currentTimeMillis(), true));
        messagesAdapter.notifyItemInserted(messages.size() - 1);
    }


    private void setLineEnding(LINE_ENDING_TYPE lineEnding)
    {
        currentLineEnding = lineEnding;
        if (currentLineEnding == null)
        {
            currentLineEnding = LINE_ENDING_TYPE.NONE;
        }
        lineEndingText.setText(currentLineEnding.getText());
        deviceRecord.setLineEnding(currentLineEnding);
        devicesDao.updateDevice(deviceRecord);
    }

    public void switchLineEnding(View v)
    {
        setLineEnding(currentLineEnding.getNext());
    }


    private void onConnect()
    {
        deviceNameText.setTextColor(getResources().getColor(R.color.colorBlue));
        deviceActionText.setOnClickListener(this::disconnect);
        deviceActionText.setText(R.string.disconnect);
    }

    private void disconnect(View view)
    {
        deviceActionText.setOnClickListener(null);
        helper.disconnect();
    }

    private void onDisconnect()
    {
        deviceNameText.setTextColor(getResources().getColor(R.color.colorWhite));
        deviceActionText.setOnClickListener(this::connect);
        deviceActionText.setText(R.string.connect);
    }

    private void connect(View view)
    {
        if (deviceRecord != null)
        {
            deviceActionText.setOnClickListener(null);
            helper.connect(deviceRecord.getMacAddress(), handler);
        }
    }

    public void onChooseDeviceButtonClick(View view)
    {
        startChoosingActivity();
    }


    private static class BluetoothCallbackHandler extends AbstractBluetoothCallbackHandler
    {
        private MainActivity activity;

        protected BluetoothCallbackHandler(BluetoothConnectionHelper helper, MainActivity activity)
        {
            super(helper);
            this.activity = activity;
        }

        @Override
        protected void onError(Exception e)
        {
            activity.runOnUiThread(() ->
                    this.activity.addError(e.getLocalizedMessage()));
        }

        @Override
        protected void onDisconnect()
        {
            activity.runOnUiThread(() ->
                    activity.onDisconnect());
        }

        @Override
        protected void processMessage(String msg)
        {
            activity.runOnUiThread(() ->
                    activity.addMessage(msg, true));
        }

        @Override
        protected void onConnect()
        {
            activity.runOnUiThread(() ->
                    this.activity.onConnect());
        }
    }


}
