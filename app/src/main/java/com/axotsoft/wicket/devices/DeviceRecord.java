package com.axotsoft.wicket.devices;

import com.axotsoft.wicket.messages.MessageRecord;
import com.axotsoft.wicket.patterns.records.PatternRecord;
import com.axotsoft.wicket.utils.LINE_ENDING_TYPE;
import com.axotsoft.wicket.widget.WidgetRecord;

import java.util.List;
import java.util.Objects;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;

public class DeviceRecord extends RealmObject {
    public static final String FIELD_ADDRESS = "address";
    @PrimaryKey
    private String address;
    private String deviceName;

    private int lineEnding;
    private RealmList<PatternRecord> patterns;

    private RealmList<MessageRecord> messages;

    @LinkingObjects("deviceRecord")
    private final RealmResults<WidgetRecord> widgets = null;

    public DeviceRecord() {
        this(null);
    }

    public DeviceRecord(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public LINE_ENDING_TYPE getLineEnding() {
        return LINE_ENDING_TYPE.valueOf(lineEnding);
    }

    public RealmList<PatternRecord> getPatterns() {
        return patterns;
    }


    public void setLineEnding(LINE_ENDING_TYPE lineEnding) {
        this.lineEnding = lineEnding.getKey();
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public RealmList<MessageRecord> getMessages() {
        return messages;
    }

    public List<WidgetRecord> getWidgets() {
        return widgets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceRecord that = (DeviceRecord) o;
        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
