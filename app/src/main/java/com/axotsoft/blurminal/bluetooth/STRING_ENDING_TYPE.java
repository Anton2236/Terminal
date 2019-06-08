package com.axotsoft.blurminal.bluetooth;

import java.util.Arrays;

public enum STRING_ENDING_TYPE
{
    NONE(0, ""), CR(1, "\n"), LF(2, "\r"), CRLF(3, "\n\r");
    private int key;
    private String ending;

    STRING_ENDING_TYPE(int key, String ending)
    {
        this.key = key;
        this.ending = ending;
    }

    public int getKey()
    {
        return key;
    }

    public String getEnding()
    {
        return ending;
    }

    public static STRING_ENDING_TYPE valueOf(int key)
    {
        STRING_ENDING_TYPE stringEndingType = NONE;
        for (STRING_ENDING_TYPE type : STRING_ENDING_TYPE.values())
        {
            if (type.getKey() == key)
            {
                stringEndingType = type;
                break;
            }
        }
        return stringEndingType;
    }
}
