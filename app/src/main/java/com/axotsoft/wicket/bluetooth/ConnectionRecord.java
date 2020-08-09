package com.axotsoft.wicket.bluetooth;

import com.axotsoft.wicket.devices.DeviceRecord;

import java.util.Objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ConnectionRecord extends RealmObject {
    public static final String ACTION_CONNECTION_STATE_CHANGED = "com.axotsoft.wicket.action.ACTION_CONNECTION_STATE_CHANGED";


    public static final String FIELD_ID = "id";
    public static final String FIELD_CONNECTED = "connected";
    public static final String FIELD_CONNECTING = "connecting";

    @PrimaryKey
    private int id;

    private DeviceRecord deviceRecord;

    private boolean connected = false;
    private boolean connecting = false;
    private long lastCommandTime = -1;
    private boolean shouldDisconnect = false;

    public ConnectionRecord() {
    }

    public ConnectionRecord(int id, DeviceRecord deviceRecord) {
        this.id = id;
        this.deviceRecord = deviceRecord;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public DeviceRecord getDeviceRecord() {
        return deviceRecord;
    }

    public void setDeviceRecord(DeviceRecord deviceRecord) {
        this.deviceRecord = deviceRecord;
    }

    public int getId() {
        return id;
    }

    public boolean isConnecting() {
        return connecting;
    }

    public void setConnecting(boolean connecting) {
        this.connecting = connecting;
    }

    public long getLastCommandTime() {
        return lastCommandTime;
    }

    public void setLastCommandTime(long lastCommandTime) {
        this.lastCommandTime = lastCommandTime;
    }

    public boolean isShouldDisconnect() {
        return shouldDisconnect;
    }

    public void setShouldDisconnect(boolean shouldDisconnect) {
        this.shouldDisconnect = shouldDisconnect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionRecord that = (ConnectionRecord) o;
        return id == that.id &&
                deviceRecord.equals(that.deviceRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceRecord);
    }
}
