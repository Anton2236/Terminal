package com.axotsoft.terb.messages;

import com.axotsoft.terb.messages.MessageType;

import io.realm.RealmObject;

public class MessageRecord extends RealmObject {
    public static final String FIELD_TIME_MILLIS = "timeMillis";
    public static final String FIELD_MESSAGE_TYPE = "messageType";
    private String message;
    private String messageType;
    private long timeMillis;

    public long getTimeMillis() {
        return timeMillis;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimeMillis(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    public MessageType getMessageType() {
        return MessageType.valueOf(messageType);
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType.toString();
    }
}
