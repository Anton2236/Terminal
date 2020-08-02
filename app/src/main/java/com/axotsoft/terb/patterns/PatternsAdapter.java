package com.axotsoft.terb.patterns;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.R;
import com.axotsoft.terb.patterns.records.PatternRecord;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class PatternsAdapter extends RealmRecyclerViewAdapter<PatternRecord, PatternsAdapter.PatternViewHolder> {
    private PatternConsumer selectConsumer;
    private PatternConsumer removeConsumer;
    @LayoutRes
    private int layoutId;

    public PatternsAdapter(OrderedRealmCollection<PatternRecord> patterns, PatternConsumer selectConsumer, PatternConsumer removeConsumer) {
        super(patterns, true);
        this.selectConsumer = selectConsumer;
        this.removeConsumer = removeConsumer;
    }

    public void setLayout(@LayoutRes int layoutId) {
        this.layoutId = layoutId;
    }

    @NonNull
    @Override
    public PatternViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(layoutId, viewGroup, false);
        return new PatternViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatternViewHolder patternViewHolder, int i) {
        PatternRecord patternRecord = getItem(i);
        patternViewHolder.setData(patternRecord);
    }

    class PatternViewHolder extends RecyclerView.ViewHolder {
        protected TextView textView;
        private PatternRecord pattern;

        PatternViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.command);
            itemView.setOnClickListener(this::onClick);
            if (removeConsumer != null) {
                itemView.setOnLongClickListener(this::OnLongClick);
            }
            else {
                itemView.setOnLongClickListener(null);
            }
        }

        private boolean OnLongClick(View view) {
            removeConsumer.accept(pattern);
            return true;
        }

        private void onClick(View view) {
            if (selectConsumer != null) {
                selectConsumer.accept(pattern);
            }
        }

        void setData(PatternRecord s) {
            pattern = s;
            textView.setText(pattern.getName());
        }
    }
}
