package com.axotsoft.blurminal2.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
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

    BluetoothConnectionHandler(Looper looper, Context context, String address, Messenger callBack)
    {
        super(looper);
        this.callBack = callBack;
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (device != null)
        {
            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
            {
                connect(device);
            }
            else
            {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                BroadcastReceiver bondedReceiver = getBondedReceiver(address);
                context.registerReceiver(bondedReceiver, intentFilter);
                device.createBond();
            }
        }
        else
        {
            disconnect();
        }
    }

    private BroadcastReceiver getBondedReceiver(final String address)
    {
        return new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                if (device.getAddress().equals(address))
                {
                    if (bondState == BluetoothDevice.BOND_BONDED)
                    {
                        context.unregisterReceiver(this);
                        connect(device);
                    }
                    else if (bondState != BluetoothDevice.BOND_NONE)
                    {
                        context.unregisterReceiver(this);
                        disconnect();
                    }
                }
            }
        };
    }


    private void connect(BluetoothDevice device)
    {
        try
        {
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});

            socket = (BluetoothSocket) m.invoke(device, 1);
            if (socket != null)
            {
                socket.connect();
                if (socket.isConnected())
                {
                    this.out = socket.getOutputStream();
                    this.inputThread = new InputThread(socket.getInputStream());
                    this.inputThread.start();
                    sendConnected();
                }
            }
        } catch (Exception e)
        {
            sendError(e);
            sendDisconnect();
            Log.e(TAG, e.getMessage(), e);
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
            sendError(e);
        }
    }

    private void disconnect()
    {
        try
        {
            if (out != null)
            {
                out.close();
            }
        } catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        if (inputThread != null)
        {
            inputThread.setStop();
        }
        try
        {
            if (socket != null)
            {
                socket.close();
            }
        } catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
        }
        sendDisconnect();
    }

    private void sendDisconnect()
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

    private void sendConnected()
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

    private void sendError(Exception e)
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

    private void sendMessage(String msg)
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
        private final Object sync = new Object();

        private InputThread(InputStream inputStream)
        {
            this.inputStream = inputStream;
            stop = false;
        }


        void setStop()
        {
            synchronized (sync)
            {
                this.stop = true;
            }
        }

        @Override
        public void run()
        {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            byte[] bytes;
            while (true)
            {
                try
                {
                    if (!socket.isConnected() || adapter.getState() != BluetoothAdapter.STATE_ON)
                    {
                        disconnect();
                        break;
                    }
                    bytes = new byte[inputStream.available()];
                    if (inputStream.read(bytes) > 0)
                    {
                        sendMessage(new String(bytes));
                    }
                    synchronized (sync)
                    {
                        if (stop)
                        {
                            inputStream.close();
                            break;
                        }
                    }
                } catch (Exception e)
                {
                    Log.e(TAG, e.getMessage(), e);
                    try
                    {
                        inputStream.close();
                    } catch (Exception e1)
                    {
                        Log.e(TAG, e1.getMessage(), e1);
                    }
                    sendError(e);
                    disconnect();

                    break;
                }
            }
        }
    }
}
