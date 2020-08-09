package com.axotsoft.wicket.utils;

import android.view.View;
import android.widget.TextView;

import com.axotsoft.wicket.devices.DeviceRecord;
import com.axotsoft.wicket.realm.Database;

public class LineEndingEditor {
    private TextView lineEndingText;
    private DeviceRecord deviceRecord;
    private Database database;
    private LINE_ENDING_TYPE currentLineEnding;

    public LineEndingEditor(TextView lineEndingText, Database database) {
        this.lineEndingText = lineEndingText;
        this.lineEndingText.setOnClickListener(this::switchLineEnding);
        this.database = database;
    }

    public void updateDeviceRecord(DeviceRecord record) {
        this.deviceRecord = record;
        if (deviceRecord != null) {
            setLineEnding(deviceRecord.getLineEnding());
        }
        else {
            setLineEnding(LINE_ENDING_TYPE.NONE);
        }
    }

    private void setLineEnding(LINE_ENDING_TYPE lineEnding) {
        currentLineEnding = lineEnding;
        if (currentLineEnding == null) {
            currentLineEnding = LINE_ENDING_TYPE.NONE;
        }
        lineEndingText.setText(currentLineEnding.getText());
        if (deviceRecord != null) {
            database.execute(realm -> {
                deviceRecord.setLineEnding(currentLineEnding);
            });
        }
    }

    private void switchLineEnding(View v) {
        setLineEnding(currentLineEnding.getNext());
    }
}
