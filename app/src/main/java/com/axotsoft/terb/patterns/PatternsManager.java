package com.axotsoft.terb.patterns;

import android.widget.EditText;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.devices.DeviceRecord;
import com.axotsoft.terb.patterns.records.CommandRecord;
import com.axotsoft.terb.patterns.records.PatternRecord;
import com.axotsoft.terb.realm.Database;

import io.realm.RealmList;

public class PatternsManager {

    private Database database;
    private RecyclerView patternsView;
    private EditText commandEditText;
    private DeviceRecord deviceRecord;
    @LayoutRes
    private final int layoutId;

    public PatternsManager(Database database, RecyclerView patternsView, EditText commandEditText, @LayoutRes int layoutId) {
        this.database = database;
        this.patternsView = patternsView;
        this.commandEditText = commandEditText;
        this.layoutId = layoutId;
    }

    public void updateDeviceRecord(DeviceRecord deviceRecord, PatternConsumer patternConsumer) {
        this.deviceRecord = deviceRecord;
        if (deviceRecord != null) {
            RealmList<PatternRecord> patterns = deviceRecord.getPatterns();
            PatternsAdapter patternsAdapter = new PatternsAdapter(patterns, patternConsumer, this::removePattern);
            patternsAdapter.setLayout(layoutId);
            this.patternsView.setAdapter(patternsAdapter);

            if (!patterns.isEmpty()) {
                patternsView.scrollToPosition(patterns.size() - 1);
            }
        }
        else {
            patternsView.setAdapter(null);
        }

    }

    public PatternRecord createSingletonPattern(String message) {
        if (message.isEmpty()) {
            return null;
        }
        PatternRecord patternRecord = new PatternRecord();

        patternRecord.setName(message);
        patternRecord.setDisconnectAfterCommands(false);
        patternRecord.setInitialDelay(0);

        CommandRecord commandRecord = new CommandRecord();
        commandRecord.setMessage(message);
        commandRecord.setDelay(100);
        patternRecord.getCommands().add(commandRecord);
        return patternRecord;
    }

    public void addPattern(PatternRecord patternRecord) {
        if (deviceRecord == null || patternRecord == null) {
            return;
        }
        String deviceAddress = deviceRecord.getAddress();
        database.executeAsync(realm -> {
            Database database = new Database(realm);
            PatternRecord record = realm.copyToRealm(patternRecord);
            DeviceRecord deviceRecord = database.getDevice(deviceAddress);
            deviceRecord.getPatterns().add(record);
        }, () -> {
            patternsView.scrollToPosition(deviceRecord.getPatterns().size() - 1);
        });
    }

    public void removePattern(PatternRecord pattern) {
        database.execute(realm -> {
            pattern.deleteFromRealm();
        });
        patternsView.scrollToPosition(deviceRecord.getPatterns().size() - 1);

    }

    public String getEnteredCommand() {
        return commandEditText.getText().toString();
    }
}
