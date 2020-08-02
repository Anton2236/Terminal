package com.axotsoft.terb.messages;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.axotsoft.terb.devices.DeviceRecord;
import com.axotsoft.terb.realm.Database;

public class MessagesManager {
    private Context context;
    private Database database;
    private DeviceRecord deviceRecord;

    public MessagesManager(Context context, Database database) {
        this.context = context;
        this.database = database;
    }

    public void setDeviceRecord(DeviceRecord deviceRecord) {
        this.deviceRecord = deviceRecord;
    }

    public void addMessage(String message, MessageType messageType, long timeMillis) {
        if (deviceRecord == null) {
            return;
        }
        String deviceAddress = deviceRecord.getAddress();
        database.executeAsync(realm -> {
            Database database = new Database(realm);
            DeviceRecord deviceRecord = database.getDevice(deviceAddress);
            MessageRecord record = realm.createObject(MessageRecord.class);
            record.setMessage(message);
            record.setMessageType(messageType);
            record.setTimeMillis(timeMillis);
            deviceRecord.getMessages().add(record);
        }, () -> {
            if (messageType == MessageType.RECEIVED_MESSAGE) {
                getVibrator().vibrate(VibrationEffect.createOneShot(10, 255));
            }
        });
    }

    private Vibrator getVibrator() {
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
}
