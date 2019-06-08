package com.axotsoft.blurminal.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothConnectionHandler extends Handler
{
    static final int STATUS_OK = 0;
    static final int STATUS_ERROR = 1;
    static final int STATUS_DISCONNECTED = 2;
    static final int STATUS_CONNECTED = 3;


    private static final int COMMAND_SEND = 0;
    private static final int COMMAND_DISCONNECT = 1;

    private String TAG = getClass().getName();

    private BluetoothSocket socket;


    private OutputStream out;
    private Messenger callBack;
    private InputThread inputThread;

    public BluetoothConnectionHandler(String address, Messenger callBack) throws IOException
    {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (device != null)
        {
            this.socket = device.createInsecureRfcommSocketToServiceRecord(UUID.randomUUID());
            this.callBack = callBack;
            socket.connect();
            if (socket.isConnected())
            {
                this.out = socket.getOutputStream();
                this.inputThread = new InputThread(socket.getInputStream());
                this.inputThread.start();
                SendConnected();
            }
        }
    }

    public static Message getSendMessage(String message, LINE_ENDING_TYPE endingType)
    {
        Message msg = Message.obtain();
        msg.arg1 = BluetoothConnectionHandler.COMMAND_SEND;
        msg.arg2 = endingType.getKey();
        msg.obj = message;
        return msg;
    }

    public static Message getDisconnectMessage()
    {
        Message msg = Message.obtain();
        msg.arg1 = BluetoothConnectionHandler.COMMAND_DISCONNECT;
        return msg;
    }

    @Override
    public void handleMessage(Message msg)
    {
        try
        {
            if (msg.arg1 == COMMAND_SEND)
            {
                LINE_ENDING_TYPE ending_type = LINE_ENDING_TYPE.valueOf(msg.arg2);
                String message = (String) msg.obj;

                out.write((message == null ? "" : message + ending_type.getEnding()).getBytes());
                out.flush();
            }
            else if (msg.arg1 == COMMAND_DISCONNECT)
            {
                disconnect();
            }
        } catch (Exception e)
        {
            Log.e(TAG, e.getMessage(), e);
            SendError(e);
        }
    }

    private void disconnect() throws Exception
    {
        out.close();
        inputThread.setStop();
        socket.close();
        SendDisconnect();
    }

    private void SendDisconnect()
    {
        Message message = Message.obtain();
        message.arg1 = STATUS_DISCONNECTED;
        try
        {
            callBack.send(message);
        } catch (RemoteException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void SendConnected()
    {
        Message message = Message.obtain();
        message.arg1 = STATUS_CONNECTED;
        try
        {
            callBack.send(message);
        } catch (RemoteException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void SendError(Exception e)
    {
        Message message = Message.obtain();
        message.obj = e;
        message.arg1 = STATUS_ERROR;
        try
        {
            callBack.send(message);
        } catch (RemoteException e1)
        {
            Log.e(TAG, e1.getMessage(), e1);
        }
    }

    private void SendMessage(String msg)
    {
        Message message = Message.obtain();
        message.obj = msg;
        message.arg1 = STATUS_OK;
        try
        {
            callBack.send(message);
        } catch (RemoteException e1)
        {
            Log.e(TAG, e1.getMessage(), e1);
        }
    }

    private class InputThread extends Thread
    {

        private InputStream inputStream;
        private boolean stop;

        private InputThread(InputStream inputStream)
        {
            this.inputStream = inputStream;
            stop = false;
        }

        public void setStop()
        {
            this.stop = true;
        }

        @Override
        public void run()
        {
            byte[] bytes;
            while (true)
            {
                try
                {
                    bytes = new byte[inputStream.available()];
                    if (inputStream.read(bytes) > 0)
                    {
                        SendMessage(new String(bytes));
                    }
                    if (stop)
                    {
                        inputStream.close();
                        break;
                    }
                } catch (Exception e)
                {
                    Log.e(TAG, e.getMessage(), e);
                    SendError(e);
                    try
                    {
                        disconnect();
                    } catch (Exception e1)
                    {
                        Log.e(TAG, e1.getMessage(), e1);
                    }
                    try
                    {
                        inputStream.close();
                    } catch (Exception e1)
                    {
                        Log.e(TAG, e1.getMessage(), e1);
                    }
                    break;
                }
            }
        }
    }
}
