package com.axotsoft.blurminal.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.axotsoft.blurminal.R;
import com.axotsoft.blurminal.bluetooth.AbstractBluetoothCallbackHandler;
import com.axotsoft.blurminal.bluetooth.BluetoothConnectionHelper;
import com.axotsoft.blurminal.bluetooth.LINE_ENDING_TYPE;
import com.axotsoft.blurminal.devices.BluetoothMessagesManager;

public class MainActivity extends AbstractDeviceChooserClientActivity
{
    private BluetoothMessagesManager messagesManager;
    private AbstractBluetoothCallbackHandler handler;
    private BluetoothConnectionHelper helper;
    private EditText commandEditText;
    private RecyclerView messagesView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MessagesAdapter adapter = new MessagesAdapter(null);
        messagesView.setAdapter(adapter);
    }


    @Override
    protected void updateDeviceData()
    {
        messagesManager = new BluetoothMessagesManager(devicesDao, deviceRecord);
        helper = new BluetoothConnectionHelper(this);
        handler = new BluetoothCallbackHandler(helper, this);
    }

    public void sendButtonClick(View v)
    {
        if (helper != null)
        {
            String command = commandEditText.getText().toString();
            if (!command.isEmpty())
            {
                helper.sendMessage(command, getLineEndingType());
                addMessage(command, false);
            }
        }
    }

    private void addMessage(String msg, boolean fromDevice)
    {
        messagesManager.addMessage(msg, fromDevice);

    }


    private LINE_ENDING_TYPE getLineEndingType()
    {
        return LINE_ENDING_TYPE.CRLF;
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
        protected void OnError(Exception e)
        {

        }

        @Override
        protected void OnDisconnect()
        {

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

        }
    }


}
