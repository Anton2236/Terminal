package com.axotsoft.wicket.utils;

import android.widget.EditText;
import android.widget.ImageView;

public class CommandEditor {
    private EditText commandEditText;
    private EditText delayEditText;
    private ImageView deleteButton;

    public CommandEditor(EditText commandEditText, EditText delayEditText, ImageView deleteButton) {
        this.commandEditText = commandEditText;
        this.delayEditText = delayEditText;
        this.deleteButton = deleteButton;
    }


    public EditText getCommandEditText() {
        return commandEditText;
    }

    public EditText getDelayEditText() {
        return delayEditText;
    }
}
