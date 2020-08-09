package com.axotsoft.wicket.patterns.records;

import java.io.Serializable;

import io.realm.RealmObject;

public class CommandRecord extends RealmObject implements Serializable {
    private String message;
    private long delay;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}
