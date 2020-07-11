package com.axotsoft.terb.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.R;
import com.axotsoft.terb.activity.AbstractDeviceChooserClientActivity;
import com.axotsoft.terb.bluetooth.LINE_ENDING_TYPE;
import com.axotsoft.terb.provider.BluetoothDeviceRecord;
import com.axotsoft.terb.utils.UiUtils;

import static com.axotsoft.terb.widget.BluetoothCommandWidgetUtils.saveWidgetPreferences;

/**
 * The configuration screen for the {@link BluetoothCommandWidget BluetoothCommandWidget} AppWidget.
 */
public class BluetoothCommandWidgetConfigureActivity extends AbstractDeviceChooserClientActivity {
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText appWidgetTitleText;
    private EditText appWidgetCommandText;
    private Button addButton;
    private TextView deviceText;
    private ConstraintLayout configurationContainer;
    private TextView lineEndingText;
    private RecyclerView commandsContainer;
    private LINE_ENDING_TYPE currentLineEnding;

    public void onAddButtonClick(View v) {
        if (deviceRecord != null) {
            // When the button is clicked, store the string locally
            String commandText = appWidgetCommandText.getText().toString();
            if (commandText.isEmpty()) {
                UiUtils.makeToast(this, getResources().getString(R.string.toast_enter_command));
                return;
            }
            String widgetTitleText = appWidgetTitleText.getText().toString();
            saveWidgetPreferences(this, new BluetoothWidgetData(commandText, deviceRecord.getMacAddress(), widgetTitleText, appWidgetId, currentLineEnding));

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            BluetoothCommandWidget.updateAppWidget(this, appWidgetManager, appWidgetId);

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
    protected void updateDeviceData(BluetoothDeviceRecord deviceRecord) {
        if (deviceRecord == null) {
            deviceText.setText(R.string.select_device);
            configurationContainer.setVisibility(View.GONE);
        }
        else {
            deviceText.setText(deviceRecord.getDeviceName());
            configurationContainer.setVisibility(View.VISIBLE);
            appWidgetCommandText.setText("");
            appWidgetTitleText.setText("");
            if (deviceRecord.getCommands() != null && deviceRecord.getCommands().size() > 0) {
                commandsContainer.setVisibility(View.VISIBLE);
                commandsContainer.setAdapter(new CommandsAdapter(deviceRecord.getCommands(), appWidgetCommandText::setText, null, R.layout.command_layout));
            }
            else {
                commandsContainer.setVisibility(View.GONE);
            }
            setLineEnding(deviceRecord.getLineEnding());
        }
    }

    private void setLineEnding(LINE_ENDING_TYPE lineEnding) {
        currentLineEnding = lineEnding;
        if (currentLineEnding == null) {
            currentLineEnding = LINE_ENDING_TYPE.NONE;
        }
        lineEndingText.setText(currentLineEnding.getText());
    }

    public void switchLineEnding(View v) {
        setLineEnding(currentLineEnding.getNext());
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
        lineEndingText = findViewById(R.id.line_ending_chooser);
        commandsContainer = findViewById(R.id.commands_container);
        addButton = findViewById(R.id.add_button);
        configurationContainer = findViewById(R.id.widget_configurator);
        addButton.setOnClickListener(this::onAddButtonClick);
    }

    public void onChooseDeviceButtonClick(View v) {
        startChoosingActivity();
    }
}

