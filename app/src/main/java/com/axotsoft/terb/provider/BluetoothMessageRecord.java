package com.axotsoft.terb.provider;

public class BluetoothMessageRecord {
    private long id;
    private long deviceId;
    private boolean fromDevice;
    private String message;
    private long timeMillis;

    public BluetoothMessageRecord(long id, long deviceId, boolean fromDevice, String message, long timeMillis) {
        this.id = id;
        this.deviceId = deviceId;
        this.fromDevice = fromDevice;
        this.message = message;
        this.timeMillis = timeMillis;
    }

    public long getTimeMillis() {
        return timeMillis;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFromDevice() {
        return fromDevice;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public long getId() {
        return id;
    }
}
