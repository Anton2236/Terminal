package com.axotsoft.terb.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.axotsoft.terb.devices.DeviceRecord;
import com.axotsoft.terb.messages.MessageRecord;
import com.axotsoft.terb.messages.MessageType;
import com.axotsoft.terb.messages.MessagesManager;
import com.axotsoft.terb.patterns.data.CommandData;
import com.axotsoft.terb.patterns.data.PatternData;
import com.axotsoft.terb.patterns.records.PatternRecord;
import com.axotsoft.terb.realm.Database;
import com.axotsoft.terb.widget.CommandWidgetProvider;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

import static com.axotsoft.terb.bluetooth.BluetoothConnectionHandler.STATUS_CONNECTED;
import static com.axotsoft.terb.bluetooth.BluetoothConnectionHandler.STATUS_DISCONNECTED;
import static com.axotsoft.terb.bluetooth.BluetoothConnectionHandler.STATUS_ERROR;
import static com.axotsoft.terb.bluetooth.BluetoothConnectionHandler.STATUS_OK;

public class BluetoothCallbackHandler extends Handler {
    private Context context;
    private BluetoothConnectionHelper helper;
    private Database database;
    private MessagesManager messagesManager;
    private List<PatternData> pendingPatterns;

    protected BluetoothCallbackHandler(Context context, Database database) {
        this.context = context;
        this.helper = new BluetoothConnectionHelper(context);
        this.database = database;
        this.messagesManager = new MessagesManager(context, database);
        this.pendingPatterns = new ArrayList<>();
    }

    @Override
    public void handleMessage(Message msg) {
        int status = msg.arg1;
        final ConnectionRecord connectionRecord = database.getConnection();
        if (connectionRecord == null) {
            return;
        }
        switch (status) {
            case STATUS_OK: {
                processMessage((String) msg.obj);
                break;
            }
            case STATUS_CONNECTED: {
                database.execute((realm) -> {
                    connectionRecord.setConnected(true);
                    connectionRecord.setConnecting(false);
                });
                pendingPatterns.forEach(this::sendPattern);
                pendingPatterns.clear();
                sendConnectionStateChanged(connectionRecord);
                break;
            }
            case STATUS_DISCONNECTED: {
                cancelMessages();
                pendingPatterns.clear();
                messagesManager.setDeviceRecord(null);
                helper.unbind();
                database.execute((realm) -> {
                    connectionRecord.setConnected(false);
                    connectionRecord.setConnecting(false);
                    connectionRecord.setLastCommandTime(-1);
                    connectionRecord.setShouldDisconnect(false);
                });
                sendConnectionStateChanged(connectionRecord);
                break;
            }
            case STATUS_ERROR: {
                onError((Exception) msg.obj);
                break;
            }
        }
    }

    private void cancelMessages() {
        final ConnectionRecord connectionRecord = database.getConnection();
        if (connectionRecord == null) {
            return;
        }
        String deviceAddress = connectionRecord.getDeviceRecord().getAddress();
        database.executeAsync(realm -> {
            DeviceRecord deviceRecord = new Database(realm).getDevice(deviceAddress);
            List<MessageRecord> pendingMessages = getPendingMessages(deviceRecord);
            pendingMessages.forEach(m -> m.setMessageType(MessageType.UNSENT_MESSAGE));
        });
    }

    private RealmResults<MessageRecord> getPendingMessages(DeviceRecord deviceRecord) {
        return deviceRecord.getMessages().where().equalTo(MessageRecord.FIELD_MESSAGE_TYPE, MessageType.PENDING_MESSAGE.toString()).sort(MessageRecord.FIELD_TIME_MILLIS).findAll();
    }

    public void sendPattern(PatternData pattern) {
        final ConnectionRecord connectionRecord = database.getConnection();
        if (connectionRecord == null) {
            return;
        }

        if (!connectionRecord.isConnected()) {
            pendingPatterns.add(pattern);
            return;
        }
        long lastCommandTime = System.currentTimeMillis();


        long initialDelay = pattern.getInitialDelay();
        if (initialDelay < PatternRecord.MINIMAL_DELAY) {
            initialDelay = PatternRecord.MINIMAL_DELAY;
        }
        lastCommandTime += initialDelay;

        List<CommandData> commands = pattern.getCommands();
        for (CommandData command : commands) {
            String msg = command.getMessage();
            messagesManager.addMessage(msg, MessageType.PENDING_MESSAGE, lastCommandTime);
            updateAtTime(lastCommandTime);
            long delay = command.getDelay();
            if (delay < PatternRecord.MINIMAL_DELAY) {
                delay = PatternRecord.MINIMAL_DELAY;
            }
            lastCommandTime += delay;
        }

        final long lastTime = lastCommandTime;
        database.execute(realm -> {
            if (connectionRecord.getLastCommandTime() < lastTime) {
                connectionRecord.setLastCommandTime(lastTime);
            }
            if (pattern.isDisconnectAfterCommands()) {
                connectionRecord.setShouldDisconnect(true);
            }
        });
        if (connectionRecord.isShouldDisconnect()) {
            updateAtTime(lastCommandTime);
        }
    }

    private void updateAtTime(long systemTime) {
        if (systemTime > System.currentTimeMillis()) {
            postDelayed(this::update, (systemTime - System.currentTimeMillis()) + 10);
        }
    }

    private void update() {
        final ConnectionRecord connectionRecord = database.getConnection();
        if (connectionRecord == null) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (connectionRecord.isConnected()) {
            String deviceAddress = connectionRecord.getDeviceRecord().getAddress();

            List<Message> toSend = new ArrayList<>();
            database.executeAsync(realm -> {
                Database database = new Database(realm);
                DeviceRecord deviceRecord = database.getDevice(deviceAddress);
                List<MessageRecord> pendingMessages = getPendingMessages(deviceRecord);
                for (MessageRecord messageRecord : pendingMessages) {
                    if (messageRecord.getTimeMillis() <= currentTimeMillis) {
                        messageRecord.setMessageType(MessageType.SENT_MESSAGE);
                        Message message = BluetoothConnectionHandler.getSendMessage(messageRecord.getMessage(), deviceRecord.getLineEnding());
                        toSend.add(message);
                    }
                }

            }, () -> {
                toSend.forEach(helper::send);
            });

            if (connectionRecord.getLastCommandTime() <= System.currentTimeMillis() && connectionRecord.isShouldDisconnect()) {
                disconnect(connectionRecord);
            }
        }
    }

    public void connect(ConnectionRecord connectionRecord) {
        if (!connectionRecord.isConnected() && !connectionRecord.isConnecting()) {
            DeviceRecord deviceRecord = connectionRecord.getDeviceRecord();
            messagesManager.setDeviceRecord(deviceRecord);
            database.execute(realm -> {
                connectionRecord.setConnected(false);
                connectionRecord.setConnecting(true);
            });
            sendConnectionStateChanged(connectionRecord);
            helper.connect(deviceRecord.getAddress(), this);
        }
    }

    protected void onError(Exception e) {
        messagesManager.addMessage(e.getLocalizedMessage(), MessageType.ERROR, System.currentTimeMillis());
    }

    protected void processMessage(String msg) {
        messagesManager.addMessage(msg, MessageType.RECEIVED_MESSAGE, System.currentTimeMillis());
    }

    public boolean disconnect(ConnectionRecord connectionRecord) {
        boolean success = false;

        Message message = BluetoothConnectionHandler.getDisconnectMessage();
        if (connectionRecord.isConnecting() || connectionRecord.isConnected()) {
            if (helper.send(message)) {
                success = true;
            }
            else {
                database.execute(realm -> {
                            connectionRecord.setConnected(false);
                            connectionRecord.setConnecting(false);
                            connectionRecord.setLastCommandTime(-1);
                            connectionRecord.setShouldDisconnect(false);
                        }
                );
                sendConnectionStateChanged(connectionRecord);
            }
        }
        return success;
    }

    public void clearPendingPatterns() {
        pendingPatterns.clear();
    }

    private void sendConnectionStateChanged(ConnectionRecord connectionRecord) {
        context.sendBroadcast(new Intent(ConnectionRecord.ACTION_CONNECTION_STATE_CHANGED));
        CommandWidgetProvider.onConnectionStateChanged(context, connectionRecord.getDeviceRecord());
    }
}
