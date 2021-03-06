package com.axotsoft.wicket.messages;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.axotsoft.wicket.R;

import java.util.Calendar;
import java.util.Date;

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
        private TextView dateText;
        private TextView statusText;
        private CountDownTimer timer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            dateText = itemView.findViewById(R.id.date_text);
            statusText = itemView.findViewById(R.id.message_status);
        }

        void setData(MessageRecord messageRecord) {
            messageText.setText(messageRecord.getMessage());
            statusText.setVisibility(View.GONE);
            dateText.setVisibility(View.VISIBLE);
            String s = getDateString(messageRecord);
            dateText.setText(s);
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(messageText.getLayoutParams());
            ConstraintLayout.LayoutParams dateParams = new ConstraintLayout.LayoutParams(dateText.getLayoutParams());
            params.startToStart = ConstraintLayout.LayoutParams.UNSET;
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
            dateParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
            dateParams.endToEnd = R.id.message_text;
            dateParams.topToBottom = R.id.message_text;
            switch (messageRecord.getMessageType()) {
                case SENT_MESSAGE:
                    messageText.setBackgroundResource(R.drawable.message_sent);
                    break;
                case PENDING_MESSAGE:
                    statusText.setVisibility(View.VISIBLE);
                    dateText.setVisibility(View.GONE);
                    long timeLeft = messageRecord.getTimeMillis() - System.currentTimeMillis();
                    timer = startTimer(timeLeft);
                    timer.start();

                    messageText.setBackgroundResource(R.drawable.message_pending);
                    break;
                case UNSENT_MESSAGE:
                    messageText.setBackgroundResource(R.drawable.message_error);
                    dateText.setText(String.format("%s - %s", dateText.getContext().getString(R.string.unsent), dateText.getText()));
                    break;
                case RECEIVED_MESSAGE:
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.UNSET;

                    dateParams.startToStart = R.id.message_text;
                    dateParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                    messageText.setBackgroundResource(R.drawable.message_received);
                    break;
                case ERROR:
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.UNSET;

                    dateParams.startToStart = R.id.message_text;
                    dateParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
                    messageText.setBackgroundResource(R.drawable.message_error);
                    break;
            }
            dateText.setLayoutParams(dateParams);
            messageText.setLayoutParams(params);
        }

        private String getDateString(MessageRecord messageRecord) {
            long sentTimeMillis = messageRecord.getTimeMillis();
            Date sentDate = new Date(sentTimeMillis);
            Context context = dateText.getContext();
            java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
            String time = timeFormat.format(sentDate);
            Calendar currentCalendar = Calendar.getInstance(timeFormat.getTimeZone());

            Calendar sentCalendar = Calendar.getInstance(timeFormat.getTimeZone());
            sentCalendar.setTimeInMillis(sentTimeMillis);
            if (sentCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR) || sentCalendar.get(Calendar.DAY_OF_YEAR) != currentCalendar.get(Calendar.DAY_OF_YEAR)) {
                String date = DateFormat.getDateFormat(context).format(sentDate);
                return date + " " + time;
            }
            return time;
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
                    statusText.setVisibility(View.GONE);
                    statusText.setText("");
                }
            };
        }
    }
}
