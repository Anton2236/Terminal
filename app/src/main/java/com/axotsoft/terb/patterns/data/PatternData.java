package com.axotsoft.terb.patterns.data;

import com.axotsoft.terb.patterns.records.PatternRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PatternData {
    private final String name;
    private final List<CommandData> commands;
    private final boolean disconnectAfterCommands;
    private final long initialDelay;

    public PatternData() {
        this.name = null;
        this.commands = new ArrayList<>();
        this.disconnectAfterCommands = false;
        this.initialDelay = 0;
    }

    public PatternData(PatternRecord record) {
        this.name = record.getName();
        this.commands = record.getCommands().stream().map(CommandData::new).collect(Collectors.toList());
        this.disconnectAfterCommands = record.isDisconnectAfterCommands();
        this.initialDelay = record.getInitialDelay();
    }

    public String getName() {
        return name;
    }

    public boolean isDisconnectAfterCommands() {
        return disconnectAfterCommands;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public List<CommandData> getCommands() {
        return commands;
    }
}
