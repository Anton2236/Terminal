package com.axotsoft.blurminal.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.axotsoft.blurminal.R;
import com.axotsoft.blurminal.activity.AbstarctDeviceChooserClientActivity;
import com.axotsoft.blurminal.bluetooth.LINE_ENDING_TYPE;
import com.axotsoft.blurminal.utils.UiUtils;

import java.util.List;

import static com.axotsoft.blurminal.widget.BluetoothCommandWidgetUtils.saveWidgetPreferences;

/**
 * The configuration screen for the {@link BluetoothCommandWidget BluetoothCommandWidget} AppWidget.
 */
public class BluetoothCommandWidgetConfigureActivity extends AbstarctDeviceChooserClientActivity
{
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText appWidgetTitleText;
    private EditText appWidgetCommandText;
    private Button addButton;


    public void onAddButtonClick(View v)
    {
        if (deviceRecord != null)
        {
            // When the button is clicked, store the string locally
            String commandText = appWidgetCommandText.getText().toString();
            if (commandText.isEmpty())
            {
                UiUtils.makeToast(this, getResources().getString(R.string.toast_enter_command));
                return;
            }
            String widgetTitleText = appWidgetTitleText.getText().toString();
            LINE_ENDING_TYPE lineEnding = getLineEndingType();
            saveWidgetPreferences(this, new BluetoothWidgetData(commandText, deviceRecord.getMacAddress(), widgetTitleText, appWidgetId, lineEnding));

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            BluetoothCommandWidget.updateAppWidget(this, appWidgetManager, appWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
        else
        {
            UiUtils.makeToast(this, getResources().getString(R.string.toast_select_device));
        }
    }

    private LINE_ENDING_TYPE getLineEndingType()
    {
        return LINE_ENDING_TYPE.CRLF;
    }


    @Override
    protected void updateDeviceData()
    {
        updateCommandsList(deviceRecord.getCommands());
    }

    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null)
        {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
        {
            finish();
            return;
        }

        initViews();

        // Find the widget id from the intent.

    }

    private void initViews()
    {
        setContentView(R.layout.bluetooth_command_widget_configure);
        appWidgetTitleText = findViewById(R.id.appwidget_text);
        appWidgetCommandText = findViewById(R.id.command_text);
        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(this::onAddButtonClick);
    }

    public void OnChooseDeviceButtonClick(View v)
    {
        startChoosingActivity();
    }


    private void updateCommandsList(List<String> commands)
    {

    }
}

