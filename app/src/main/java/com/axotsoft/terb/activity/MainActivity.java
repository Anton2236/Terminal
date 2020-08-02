package com.axotsoft.terb.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.R;
import com.axotsoft.terb.bluetooth.BluetoothConnectionHelperService;
import com.axotsoft.terb.bluetooth.ConnectionRecord;
import com.axotsoft.terb.devices.DeviceRecord;
import com.axotsoft.terb.messages.MessageRecord;
import com.axotsoft.terb.messages.MessagesAdapter;
import com.axotsoft.terb.patterns.PatternsManager;
import com.axotsoft.terb.patterns.records.PatternRecord;
import com.axotsoft.terb.utils.AbstractDeviceClientActivity;
import com.axotsoft.terb.utils.ContextAdapter;
import com.axotsoft.terb.utils.LineEndingEditor;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AbstractDeviceClientActivity {
    private static final String PREFERENCE_DEVICE_ADDRESS = "device_address";
    public static final String PREFERENCES_NAME = "activity_main";

    private RecyclerView messagesView;
    private PatternsManager patternsManager;

    private LineEndingEditor lineEndingEditor;

    private TextView deviceNameText;
    private TextView deviceActionText;

    private View deviceContainer;

    private SharedPreferences preferences;
    private ContextAdapter contextAdapter;

    private RealmResults<MessageRecord> messageRecords;
    private OrderedRealmCollectionChangeListener<RealmResults<MessageRecord>> messagesListener = this::onMessageAdded;

    private CountDownTimer disconnectTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
        contextAdapter = new ContextAdapter(this);
        setContentView(R.layout.activity_main);
        initViews();
        initManagers();
        contextAdapter.registerBroadcastConsumer(ConnectionRecord.ACTION_CONNECTION_STATE_CHANGED, this::onConnectionStateChanged);
    }

    private void initViews() {
        deviceNameText = findViewById(R.id.device_name);
        deviceActionText = findViewById(R.id.device_action);
        deviceContainer = findViewById(R.id.device_container);
        messagesView = findViewById(R.id.messages);
    }

    private void initManagers() {
        TextView lineEndingText = findViewById(R.id.line_ending_chooser);
        lineEndingEditor = new LineEndingEditor(lineEndingText, database);

        RecyclerView patternsView = findViewById(R.id.commands_container);
        EditText commandEditText = findViewById(R.id.command_text);
        patternsManager = new PatternsManager(database, patternsView, commandEditText, R.layout.main_command_layout);
    }


    @Override
    protected void updateDeviceData(DeviceRecord deviceRecord) {
        contextAdapter.onResume();
        if (deviceRecord == null) {
            String deviceAddress = preferences.getString(PREFERENCE_DEVICE_ADDRESS, "");
            if (!deviceAddress.isEmpty()) {
                deviceRecord = database.getDevice(deviceAddress);
                this.deviceRecord = deviceRecord;
            }
        }
        updateMessagesView(deviceRecord);
        lineEndingEditor.updateDeviceRecord(deviceRecord);
        patternsManager.updateDeviceRecord(deviceRecord, this::sendPattern);

        SharedPreferences.Editor editor = preferences.edit();
        if (deviceRecord != null) {
            deviceNameText.setText(deviceRecord.getDeviceName());
            deviceContainer.setVisibility(View.VISIBLE);
            updateConnectedState();
            updateDisconnectTimer();
            editor.putString(PREFERENCE_DEVICE_ADDRESS, deviceRecord.getAddress());
            editor.apply();
        }
        else {
            updateDisconnectTimer();
            deviceNameText.setText(R.string.select_device);
            deviceContainer.setVisibility(View.GONE);
            editor.putString(PREFERENCE_DEVICE_ADDRESS, "");
            editor.apply();
        }
    }

    private void sendPattern(PatternRecord patternRecord) {
        if (deviceRecord != null) {
            startForegroundService(BluetoothConnectionHelperService.makeMessageCommandIntent(this, deviceRecord.getAddress(), patternRecord));
        }
    }

    private void onConnectionStateChanged(Context context, Intent intent) {
        updateConnectedState();
        updateDisconnectTimer();
    }

    private void updateConnectedState() {
        ConnectionRecord connectionRecord = getConnectionRecord();
        if (connectionRecord != null) {
            setConnected(connectionRecord.isConnected(), connectionRecord.isConnecting());
        }
        else {
            setConnected(false, false);
        }
    }

    private ConnectionRecord getConnectionRecord() {
        ConnectionRecord connectionRecord = database.getConnection();
        if (connectionRecord != null && connectionRecord.getDeviceRecord().equals(deviceRecord)) {
            return connectionRecord;
        }
        return null;

    }

    public void updateMessagesView(DeviceRecord deviceRecord) {
        clearMessageRecords();
        if (deviceRecord != null) {
            messageRecords = deviceRecord.getMessages().sort(MessageRecord.FIELD_TIME_MILLIS);
            MessagesAdapter adapter = new MessagesAdapter(messageRecords);
            messagesView.setAdapter(adapter);
            messagesView.scrollToPosition(messageRecords.size() - 1);
            messageRecords.addChangeListener(messagesListener);
        }
        else {
            messagesView.setAdapter(null);
        }
    }

    private void onMessageAdded(RealmResults<MessageRecord> records, OrderedCollectionChangeSet orderedCollectionChangeSet) {
        messagesView.scrollToPosition(messageRecords.size() - 1);
        updateDisconnectTimer();
    }

    private void updateDisconnectTimer() {
        if (disconnectTimer != null) {
            disconnectTimer.cancel();
            disconnectTimer = null;
        }
        ConnectionRecord connectionRecord = getConnectionRecord();
        if (connectionRecord != null && connectionRecord.isConnected() && connectionRecord.isShouldDisconnect()) {
            long timeLeft = connectionRecord.getLastCommandTime() - System.currentTimeMillis();
            disconnectTimer = startDisconnectTimer(timeLeft);
            disconnectTimer.start();
        }
    }


    private void clearMessageRecords() {
        if (messageRecords != null) {
            messageRecords.removeChangeListener(messagesListener);
            messageRecords = null;
        }
    }

    public void sendButtonClick(View v) {
        String command = patternsManager.getEnteredCommand();
        PatternRecord patternRecord = patternsManager.createSingletonPattern(command);
        sendPattern(patternRecord);
    }

    private void setConnected(boolean connected, boolean connecting) {
        if (connected) {
            deviceNameText.setTextColor(getResources().getColor(R.color.colorLightBlue, getTheme()));
            deviceActionText.setOnClickListener(this::disconnect);
            deviceActionText.setText(R.string.disconnect);
        }
        else if (connecting) {
            deviceActionText.setText(getString(R.string.connecting));
            deviceActionText.setOnClickListener(this::disconnect);
        }
        else {
            deviceNameText.setTextColor(getResources().getColor(R.color.colorWhite, getTheme()));
            deviceActionText.setOnClickListener(this::connect);
            deviceActionText.setText(R.string.connect);
        }
    }

    private void connect(View view) {
        if (deviceRecord != null) {
            startForegroundService(BluetoothConnectionHelperService.makeConnectCommandIntent(this, deviceRecord.getAddress()));
        }
    }

    private void disconnect(View view) {
        if (deviceRecord != null) {
            startForegroundService(BluetoothConnectionHelperService.makeDisconnectCommandIntent(this, deviceRecord.getAddress()));
        }
    }

    public void onChooseDeviceButtonClick(View view) {
        startChoosingActivity();
    }

    @Override
    protected void onPause() {
        super.onPause();
        contextAdapter.onPause();
        clearMessageRecords();
    }

    public void addCommand(View view) {
        String command = patternsManager.getEnteredCommand();
        PatternRecord patternRecord = patternsManager.createSingletonPattern(command);
        patternsManager.addPattern(patternRecord);
    }

    public void onCreatePatternClick(View view) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        PatternForgeDialog forgeDialog = new PatternForgeDialog(patternsManager::addPattern);
        forgeDialog.show(fragmentManager, PatternForgeDialog.TAG);
    }

    private CountDownTimer startDisconnectTimer(long timeLeft) {
        return new CountDownTimer(timeLeft, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                String text = millisUntilFinished / 1000 + "s";
                deviceActionText.setText(String.format("%s (%s)", getString(R.string.disconnect), text));
            }

            @Override
            public void onFinish() {
                deviceActionText.setText(R.string.disconnect);
            }
        };
    }
}
