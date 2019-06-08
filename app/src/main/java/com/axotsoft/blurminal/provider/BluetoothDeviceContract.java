package com.axotsoft.blurminal.provider;

import android.content.ContentUris;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

public final class BluetoothDeviceContract
{

    public static final String CONTENT_AUTHORITY = "com.axotsoft.blurminal.authority";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_DEVICE = DeviceEntry.TABLE_NAME;
    public static final String PATH_MESSAGE = MessageEntry.TABLE_NAME;

    public static final String CONTENT_DEVICE_TYPE = "com.axotsoft.bluetooth.device";
    public static final String CONTENT_DEVICES_TYPE = "com.axotsoft.bluetooth.devices";
    public static final String CONTENT_MESSAGE_TYPE = "com.axotsoft.bluetooth.message";
    public static final String CONTENT_MESSAGES_TYPE = "com.axotsoft.bluetooth.messages";

    public static class DeviceEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "devices";

        public static final String _COMMANDS = "commands";

        public static final String _MAC_ADDRESS = "mac_address";

        private static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();


        public static Uri buildUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);

        }
    }

    public static final class MessageEntry implements BaseColumns
    {

        public static final String TABLE_NAME = "messages";

        public static final String _TIME_MILLIS = "time_millis";
        public static final String _MESSAGE = "message";
        public static final String _FROM_DEVICE = "from_device";

        private static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();


        public static Uri buildUri(long id)
        {
            return ContentUris.withAppendedId(CONTENT_URI, id);

        }
    }
}