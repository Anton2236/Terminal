package com.axotsoft.wicket.bluetooth;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.axotsoft.wicket.R;
import com.axotsoft.wicket.devices.DeviceRecord;
import com.axotsoft.wicket.patterns.data.PatternData;
import com.axotsoft.wicket.patterns.records.PatternRecord;
import com.axotsoft.wicket.realm.Database;
import com.axotsoft.wicket.utils.ContextAdapter;
import com.axotsoft.wicket.utils.UiUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BluetoothConnectionHelperService extends Service {
    private static final String EXTRA_COMMAND = "command";
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_DEVICE_ADDRESS = "device_address";

    private static final int COMMAND_SEND = 0;
    private static final int COMMAND_CONNECT = 1;
    private static final int COMMAND_DISCONNECT = 2;

    private static final String CHANNEL_ID = "chanel_connection";
    private static final String CHANEL_NAME = "Bluetooth Connection";
    public static final int NOTIFICATION_ID = 1;

    private BluetoothCallbackHandler callbackHandler;
    private ContextAdapter contextAdapter;

    private Database database;

    @Override
    public void onCreate() {
        NotificationManager notificationManager = getNotificationManager();
        NotificationChannel channel = createNotificationChannel();
        notificationManager.createNotificationChannel(channel);

        this.database = new Database();
        this.callbackHandler = new BluetoothCallbackHandler(this, database);
        this.contextAdapter = new ContextAdapter(this);

        contextAdapter.registerBroadcastConsumer(ConnectionRecord.ACTION_CONNECTION_STATE_CHANGED, this::onConnectionChange);

        Notification notification = createNotification("", false, null);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void onConnectionChange(Context context, Intent intent) {
        ConnectionRecord connectionRecord = database.getConnection();
        if (connectionRecord.isConnected()) {
            NotificationManager notificationManager = getNotificationManager();
            DeviceRecord deviceRecord = connectionRecord.getDeviceRecord();
            if (deviceRecord != null) {
                Notification notification = createNotification(deviceRecord.getDeviceName(), true, deviceRecord.getAddress());
                notificationManager.notify(NOTIFICATION_ID, notification);
            }
        }
        else if (connectionRecord.isConnecting()) {
            DeviceRecord deviceRecord = connectionRecord.getDeviceRecord();
            NotificationManager notificationManager = getNotificationManager();
            Notification notification = createNotification(getString(R.string.connecting), true, deviceRecord.getAddress());
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
        else {
            contextAdapter.onPause();
            stopSelf();
        }
    }

    private void onDisconnecting() {
        NotificationManager notificationManager = getNotificationManager();
        Notification notification = createNotification(getString(R.string.disconnecting), false, null);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            contextAdapter.onResume();
            int command = intent.getIntExtra(EXTRA_COMMAND, -1);
            String deviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
            DeviceRecord deviceRecord = database.getDevice(deviceAddress);

            ConnectionRecord connectionRecord = getConnectionRecord(deviceRecord);

            if (connectionRecord == null) {
                return START_NOT_STICKY;
            }

            switch (command) {
                case COMMAND_CONNECT: {
                    callbackHandler.connect(connectionRecord);
                    break;
                }
                case COMMAND_DISCONNECT: {
                    if (callbackHandler.disconnect(connectionRecord)) {
                        onDisconnecting();
                    }
                    break;
                }
                case COMMAND_SEND: {
                    PatternData patternData = getPatternDataExtra(intent);
                    callbackHandler.sendPattern(patternData);
                    callbackHandler.connect(connectionRecord);
                }
            }
        }
        return START_NOT_STICKY;
    }

    private ConnectionRecord getConnectionRecord(DeviceRecord deviceRecord) {
        ConnectionRecord connectionRecord = database.getConnection();
        if (connectionRecord == null) {
            connectionRecord = database.createConnectionRecord(deviceRecord);
        }

        if (!connectionRecord.getDeviceRecord().equals(deviceRecord)) {
            if (connectionRecord.isConnected() || connectionRecord.isConnecting()) {
                UiUtils.makeToast(this, getString(R.string.disconnect_first_toast));
                return null;
            }

            ConnectionRecord finalConnectionRecord = connectionRecord;
            database.execute(realm -> {
                finalConnectionRecord.setDeviceRecord(deviceRecord);
                finalConnectionRecord.setShouldDisconnect(false);
                finalConnectionRecord.setLastCommandTime(-1);
            });
            connectionRecord = finalConnectionRecord;
        }
        return connectionRecord;
    }

    private PatternData getPatternDataExtra(Intent intent) {
        String patternJson = intent.getStringExtra(EXTRA_MESSAGE);
        Gson gson = new Gson();
        TypeToken<PatternData> typeToken = new TypeToken<PatternData>() {
        };
        return gson.fromJson(patternJson, typeToken.getType());
    }

    private Notification createNotification(String text, boolean needsDisconnectButton, String deviceAddress) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle(getString(R.string.bluetooh_connection));
        builder.setContentText(text);
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        if (needsDisconnectButton) {
            Intent disconnectCommandIntent = BluetoothConnectionHelperService.makeDisconnectCommandIntent(this, deviceAddress);
            PendingIntent pendingIntent = PendingIntent.getForegroundService(this, 0, disconnectCommandIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(R.drawable.ic_launcher_foreground, getString(R.string.disconnect), pendingIntent);
            builder.addAction(actionBuilder.build());
        }
        return builder.build();
    }

    public static Intent makeMessageCommandIntent(Context context, String deviceAddress, PatternRecord patternRecord) {
        Intent intent = new Intent(context, BluetoothConnectionHelperService.class);
        intent.putExtra(EXTRA_COMMAND, COMMAND_SEND);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress);
        Gson gson = new Gson();
        if (patternRecord != null) {
            intent.putExtra(EXTRA_MESSAGE, gson.toJson(new PatternData(patternRecord)));
        }
        return intent;
    }

    public static Intent makeConnectCommandIntent(Context context, String deviceAddress) {
        Intent intent = new Intent(context, BluetoothConnectionHelperService.class);
        intent.putExtra(EXTRA_COMMAND, COMMAND_CONNECT);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress);
        return intent;
    }

    public static Intent makeDisconnectCommandIntent(Context context, String deviceAddress) {
        Intent intent = new Intent(context, BluetoothConnectionHelperService.class);
        intent.putExtra(EXTRA_COMMAND, COMMAND_DISCONNECT);
        intent.putExtra(EXTRA_DEVICE_ADDRESS, deviceAddress);
        return intent;
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private NotificationChannel createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableVibration(false);
        channel.setSound(null, null);
        return channel;
    }

}
