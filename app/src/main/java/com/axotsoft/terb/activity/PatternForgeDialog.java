package com.axotsoft.terb.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.axotsoft.terb.R;
import com.axotsoft.terb.patterns.PatternsManager;
import com.axotsoft.terb.patterns.records.CommandRecord;
import com.axotsoft.terb.patterns.records.PatternRecord;

public class PatternForgeDialog extends DialogFragment {

    public static final String TAG = "patternForge";

    private EditText patternNameEditText;
    private EditText initialDelayEditText;
    private CheckBox disconnectCheckBox;
    private LinearLayout commandsLayout;

    private PatternsManager patternsManager;

    public PatternForgeDialog(PatternsManager patternsManager) {
        this.patternsManager = patternsManager;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.pattern_forge_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        patternNameEditText = view.findViewById(R.id.pattern_name);
        initialDelayEditText = view.findViewById(R.id.initial_delay);
        disconnectCheckBox = view.findViewById(R.id.disconnect_check);
        commandsLayout = view.findViewById(R.id.commands_container);
        TextView createButton = view.findViewById(R.id.create_pattern_button);
        createButton.setOnClickListener(this::onCreateClick);
    }

    public void onCreateClick(View view) {
        PatternRecord patternRecord = createPattern();
        if (patternRecord != null) {
            patternsManager.addPattern(patternRecord);
            dismiss();
        }
    }

    private PatternRecord createPattern() {
        String patternName = patternNameEditText.getText().toString();
        if (patternName.isEmpty()) {
            return null;
        }
        String initialDelayString = initialDelayEditText.getText().toString();
        int initialDelaySeconds = -1;
        try {
            if (initialDelayString.length() > 1) {
                initialDelaySeconds = Integer.parseInt(initialDelayString.substring(0, initialDelayString.length() - 1));
            }
        } catch (NumberFormatException ignored) {
        }
        if (initialDelaySeconds < 0 || initialDelaySeconds > 60) {
            return null;
        }
        long initialDelay = initialDelaySeconds * 1000L;
        boolean disconnect = disconnectCheckBox.isChecked();
        PatternRecord patternRecord = new PatternRecord();
        patternRecord.setName(patternName);
        patternRecord.setInitialDelay(initialDelay);
        patternRecord.setDisconnectAfterCommands(disconnect);

        CommandRecord commandRecord = new CommandRecord();
        commandRecord.setDelay(3000);
        commandRecord.setMessage(patternName);
        patternRecord.getCommands().add(commandRecord);
        commandRecord = new CommandRecord();
        commandRecord.setDelay(3000);
        commandRecord.setMessage(patternName);
        patternRecord.getCommands().add(commandRecord);
        return patternRecord;
    }
}
