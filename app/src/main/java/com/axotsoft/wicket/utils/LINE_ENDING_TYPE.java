package com.axotsoft.wicket.utils;

import androidx.annotation.StringRes;

import com.axotsoft.wicket.R;

public enum LINE_ENDING_TYPE {
    NONE(0, "", R.string.empty_line_ending), CR(1, "\n", R.string.cr), LF(2, "\r", R.string.lf), CRLF(3, "\n\r", R.string.crlf);
    private int key;
    private String ending;
    private @StringRes
    int text;


    LINE_ENDING_TYPE(int key, String ending, int text) {
        this.key = key;
        this.ending = ending;
        this.text = text;
    }

    public int getKey() {
        return key;
    }

    public String getEnding() {
        return ending;
    }

    public static LINE_ENDING_TYPE valueOf(int key) {
        LINE_ENDING_TYPE stringEndingType = NONE;
        for (LINE_ENDING_TYPE type : LINE_ENDING_TYPE.values()) {
            if (type.getKey() == key) {
                stringEndingType = type;
                break;
            }
        }
        return stringEndingType;
    }

    public LINE_ENDING_TYPE getNext() {
        switch (this) {
            case CRLF:
                return NONE;
            case NONE:
                return CR;
            case CR:
                return LF;
            case LF:
            default:
                return CRLF;
        }
    }

    public @StringRes
    int getText() {
        return text;
    }
}
