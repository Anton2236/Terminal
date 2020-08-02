package com.axotsoft.terb.messages;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.terb.R;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class MessagesAdapter extends RealmRecyclerViewAdapter<MessageRecord, MessagesAdapter.ViewHolder> {

    public MessagesAdapter(OrderedRealmCollection<MessageRecord> records) {
        super(records, true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.message_layout, viewGroup, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        MessageRecord messageRecord = getItem(i);
        if (messageRecord != null) {
            viewHolder.setData(messageRecord);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private TextView statusText;
        private CountDownTimer timer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            statusText = itemView.findViewById(R.id.message_status);
        }

        void setData(MessageRecord messageRecord) {
            messageText.setText(messageRecord.getMessage());
            statusText.setVisibility(View.GONE);
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(messageText.getLayoutParams());
            switch (messageRecord.getMessageType()) {
                case SENT_MESSAGE:
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                    messageText.setBackgroundResource(R.drawable.message_sent);
                    break;
                case UNSENT_MESSAGE:
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                    messageText.setBackgroundResource(R.drawable.message_error);
                    break;
                case PENDING_MESSAGE:
                    statusText.setVisibility(View.VISIBLE);
                    long timeLeft = messageRecord.getTimeMillis() - System.currentTimeMillis();
                    timer = startTimer(timeLeft);
                    timer.start();

                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                    messageText.setBackgroundResource(R.drawable.message_pending);
                    break;
                case RECEIVED_MESSAGE:
                    params.startToStart = ConstraintLayout.LayoutParams.UNSET;
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    messageText.setBackgroundResource(R.drawable.message_received);
                    break;
                case ERROR:
                    params.startToStart = ConstraintLayout.LayoutParams.UNSET;
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    messageText.setBackgroundResource(R.drawable.message_error);
                    break;
            }

            messageText.setLayoutParams(params);
        }

        private CountDownTimer startTimer(long timeLeft) {
            return new CountDownTimer(timeLeft, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    String text = millisUntilFinished / 1000 + "s";
                    statusText.setText(text);
                }

                @Override
                public void onFinish() {
                    statusText.setText("");
                }
            };
        }
    }
}
