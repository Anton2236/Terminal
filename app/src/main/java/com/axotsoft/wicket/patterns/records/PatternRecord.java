package com.axotsoft.wicket.patterns.records;

import java.io.Serializable;

import io.realm.RealmList;
import io.realm.RealmObject;

public class PatternRecord extends RealmObject implements Serializable {
    public static final long MINIMAL_DELAY = 50L;

    private String name;
    private RealmList<CommandRecord> commands = new RealmList<>();
    private boolean disconnectAfterCommands;
    private long initialDelay;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RealmList<CommandRecord> getCommands() {
        return commands;
    }

    public boolean isDisconnectAfterCommands() {
        return disconnectAfterCommands;
    }

    public void setDisconnectAfterCommands(boolean disconnectAfterCommands) {
        this.disconnectAfterCommands = disconnectAfterCommands;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }
}
