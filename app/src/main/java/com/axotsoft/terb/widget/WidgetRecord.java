package com.axotsoft.terb.widget;

import com.axotsoft.terb.devices.DeviceRecord;
import com.axotsoft.terb.patterns.records.PatternRecord;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class WidgetRecord extends RealmObject {

    public static final String FIELD_WIDGET_ID = "widgetId";

    @PrimaryKey
    private int widgetId;
    private PatternRecord pattern;
    private DeviceRecord deviceRecord;
    private String widgetTitle;

    public WidgetRecord() {
    }

    public WidgetRecord(int widgetId, DeviceRecord deviceRecord, PatternRecord pattern, String widgetTitle) {
        this.widgetId = widgetId;
        this.deviceRecord = deviceRecord;
        this.pattern = pattern;
        this.widgetTitle = widgetTitle;
    }

    public String getWidgetTitle() {
        return widgetTitle;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public void setId(int id) {
        this.widgetId = id;
    }


    public PatternRecord getPattern() {
        return pattern;
    }


    public DeviceRecord getDeviceRecord() {
        return deviceRecord;
    }
}
