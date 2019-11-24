package com.axotsoft.terb.widget;

import com.axotsoft.terb.bluetooth.LINE_ENDING_TYPE;

import java.io.Serializable;

public class BluetoothWidgetData implements Serializable
{

    public final static int STATE_IDLE = 0;
    public final static int STATE_CONNECTED = 1;
    private String command;
    private String macAddress;
    private String widgetTitle;
    private int widgetId;
    private int state;
    private LINE_ENDING_TYPE lineEnding;

    public BluetoothWidgetData(String command, String macAddress, String widgetTitle, int widgetId, LINE_ENDING_TYPE lineEnding)
    {
        this.command = command;
        this.macAddress = macAddress;
        this.widgetTitle = widgetTitle;
        this.widgetId = widgetId;
        this.lineEnding = lineEnding;
        this.state = STATE_IDLE;
    }

    public String getCommand()
    {
        return command;
    }

    public String getMacAddress()
    {
        return macAddress;
    }

    public String getWidgetTitle()
    {
        return widgetTitle;
    }

    public int getWidgetId()
    {
        return widgetId;
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
    }

    public void setId(int id)
    {
        this.widgetId = id;
    }

    public LINE_ENDING_TYPE getLineEnding()
    {
        return lineEnding;
    }

}
