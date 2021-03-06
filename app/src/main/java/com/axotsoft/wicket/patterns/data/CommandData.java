package com.axotsoft.wicket.patterns.data;

import com.axotsoft.wicket.patterns.records.CommandRecord;

public class CommandData {
    private final String message;
    private final long delay;

    public CommandData(CommandRecord record) {
        this.message = record.getMessage();
        this.delay = record.getDelay();
    }

    public String getMessage() {
        return message;
    }
    
    public long getDelay() {
        return delay;
    }
}
