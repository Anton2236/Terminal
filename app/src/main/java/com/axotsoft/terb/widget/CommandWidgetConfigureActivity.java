package com.axotsoft.terb.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.R;
import com.axotsoft.terb.devices.DeviceRecord;
import com.axotsoft.terb.patterns.records.PatternRecord;
import com.axotsoft.terb.patterns.PatternsManager;
import com.axotsoft.terb.utils.AbstractDeviceClientActivity;
import com.axotsoft.terb.utils.LineEndingEditor;
import com.axotsoft.terb.utils.UiUtils;

/**
 * The configuration screen for the {@link CommandWidgetProvider BluetoothCommandWidget} AppWidget.
 */
public class CommandWidgetConfigureActivity extends AbstractDeviceClientActivity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText appWidgetTitleText;
    private EditText appWidgetCommandText;
    private TextView deviceText;
    private ConstraintLayout configurationContainer;
    private PatternsManager patternsManager;
    private LineEndingEditor lineEndingEditor;
    private PatternRecord selectedPattern;

    public void onAddButtonClick(View v) {
        if (deviceRecord != null) {

            if (selectedPattern == null) {
                // When the button is clicked, store the string locally
                String commandText = appWidgetCommandText.getText().toString();
                if (commandText.isEmpty()) {
                    UiUtils.makeToast(this, getResources().getString(R.string.toast_enter_command));
                    return;
                }
                selectedPattern = patternsManager.createSingletonPattern(commandText);
            }
            String widgetTitleText = appWidgetTitleText.getText().toString();

            database.execute(realm -> realm.insert(new WidgetRecord(appWidgetId, deviceRecord, selectedPattern, widgetTitleText)));

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            CommandWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
        else {
            UiUtils.makeToast(this, getResources().getString(R.string.toast_select_device));
        }
    }

    @Override
    protected void updateDeviceData(DeviceRecord deviceRecord) {
        if (deviceRecord == null) {
            deviceText.setText(R.string.select_device);
            configurationContainer.setVisibility(View.GONE);
        }
        else {
            deviceText.setText(deviceRecord.getDeviceName());
            configurationContainer.setVisibility(View.VISIBLE);
            appWidgetCommandText.setText("");
            appWidgetTitleText.setText("");
            patternsManager.updateDeviceRecord(deviceRecord, this::selectPattern);
            lineEndingEditor.updateDeviceRecord(deviceRecord);
            selectedPattern = null;
        }
    }

    private void selectPattern(PatternRecord patternRecord) {
        appWidgetCommandText.setText(patternRecord.getName());
        selectedPattern = patternRecord;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        initViews();
        // Find the widget id from the intent.

    }

    private void initViews() {
        setContentView(R.layout.bluetooth_command_widget_configure);
        deviceText = findViewById(R.id.configure_device_text);
        appWidgetTitleText = findViewById(R.id.appwidget_text);
        appWidgetCommandText = findViewById(R.id.command_text);
        TextView lineEndingText = findViewById(R.id.line_ending_chooser);
        lineEndingEditor = new LineEndingEditor(lineEndingText, database);
        RecyclerView commandsContainer = findViewById(R.id.commands_container);
        patternsManager = new PatternsManager(database, commandsContainer, appWidgetCommandText, R.layout.command_layout);
        configurationContainer = findViewById(R.id.widget_configurator);
        appWidgetCommandText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                selectedPattern = null;
            }
        });
    }

    public void onChooseDeviceButtonClick(View v) {
        startChoosingActivity();
    }
}

