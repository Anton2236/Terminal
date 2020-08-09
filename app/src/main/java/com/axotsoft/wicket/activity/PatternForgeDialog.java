package com.axotsoft.wicket.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.axotsoft.wicket.R;
import com.axotsoft.wicket.patterns.PatternConsumer;
import com.axotsoft.wicket.patterns.records.CommandRecord;
import com.axotsoft.wicket.patterns.records.PatternRecord;
import com.axotsoft.wicket.utils.CommandEditor;
import com.axotsoft.wicket.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class PatternForgeDialog extends DialogFragment {

    public static final String TAG = "patternForge";

    private EditText patternNameEditText;
    private EditText initialDelayEditText;
    private CheckBox disconnectCheckBox;
    private LinearLayout commandsLayout;
    private List<CommandEditor> commandEditors = new ArrayList<>();

    private PatternConsumer patternConsumer;

    public PatternForgeDialog(PatternConsumer patternConsumer) {
        this.patternConsumer = patternConsumer;
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
        initialDelayEditText.addTextChangedListener(getTextWatcher());
        disconnectCheckBox = view.findViewById(R.id.disconnect_check);
        commandsLayout = view.findViewById(R.id.commands_container);
        TextView createButton = view.findViewById(R.id.create_pattern_button);
        createButton.setOnClickListener(this::onCreateClick);
        ImageView addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v -> createCommandEditor());
        createCommandEditor();
    }

    private TextWatcher getTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    public void onCreateClick(View view) {
        PatternRecord patternRecord = createPattern();
        if (patternRecord != null) {
            patternConsumer.accept(patternRecord);
            dismiss();
        }
    }

    private PatternRecord createPattern() {
        String patternName = patternNameEditText.getText().toString();
        if (patternName.isEmpty()) {
            UiUtils.makeToast(getContext(), getString(R.string.enter_pattern_name_toast));
            return null;
        }
        String initialDelayString = initialDelayEditText.getText().toString();
        if (initialDelayString.isEmpty()) {
            initialDelayString = "0";
        }
        int initialDelaySeconds = -1;
        try {
            initialDelaySeconds = Integer.parseInt(initialDelayString);
        } catch (NumberFormatException ignored) {
        }
        if (initialDelaySeconds < 0 || initialDelaySeconds > 60) {
            UiUtils.makeToast(getContext(), getString(R.string.invalid_initial_delay_toast));
            return null;
        }
        long initialDelay = initialDelaySeconds * 1000L;
        boolean disconnect = disconnectCheckBox.isChecked();
        List<CommandRecord> commandRecords = getCommandRecords();

        if (commandRecords == null) {
            return null;
        }
        if (commandRecords.isEmpty()) {
            UiUtils.makeToast(getContext(), getString(R.string.add_command_toast));
            return null;
        }
        PatternRecord patternRecord = new PatternRecord();
        patternRecord.setName(patternName);
        patternRecord.setInitialDelay(initialDelay);
        patternRecord.setDisconnectAfterCommands(disconnect);

        patternRecord.getCommands().addAll(commandRecords);
        return patternRecord;
    }

    private List<CommandRecord> getCommandRecords() {
        List<CommandRecord> records = new ArrayList<>();
        for (CommandEditor editor : commandEditors) {
            String commandText = editor.getCommandEditText().getText().toString();
            if (commandText.isEmpty()) {
                UiUtils.makeToast(getContext(), getString(R.string.enter_command_text_toast));
                return null;
            }
            String delayString = editor.getDelayEditText().getText().toString();
            if (delayString.isEmpty()) {
                delayString = "0";
            }
            int delaySeconds = -1;
            try {
                delaySeconds = Integer.parseInt(delayString);
            } catch (NumberFormatException ignored) {
            }
            if (delaySeconds < 0 || delaySeconds > 60) {
                UiUtils.makeToast(getContext(), getString(R.string.invalid_delay_toast));
                return null;
            }
            long delay = delaySeconds * 1000L;

            CommandRecord record = new CommandRecord();
            record.setMessage(commandText);
            record.setDelay(delay);
            records.add(record);
        }
        return records;
    }

    private void createCommandEditor() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.command_editor, commandsLayout, false);
        EditText commandEditText = view.findViewById(R.id.command_text);
        EditText delayEditText = view.findViewById(R.id.delay);
        ImageView deleteButton = view.findViewById(R.id.delete_button);
        CommandEditor commandEditor = new CommandEditor(commandEditText, delayEditText, deleteButton);
        deleteButton.setOnClickListener(v -> {
            commandsLayout.removeView(view);
            commandEditors.remove(commandEditor);
        });
        commandEditors.add(commandEditor);
        commandsLayout.addView(view);
    }
}
