package com.axotsoft.terb.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextAdapter {
    private Map<String, List<BroadcastConsumer>> consumers;
    private Map<String, BroadcastReceiver> receivers;
    private Context context;

    public ContextAdapter(Context context) {
        this.context = context;
        this.consumers = new HashMap<>();
    }

    public Context getContext() {
        return context;
    }

    public void registerBroadcastConsumer(String action, BroadcastConsumer consumer) {
        List<BroadcastConsumer> actionConsumers = consumers.get(action);
        if (actionConsumers == null) {
            actionConsumers = new ArrayList<>();
            consumers.put(action, actionConsumers);
        }
        actionConsumers.add(consumer);

        if (receivers != null && receivers.get(action) == null) {
            registerBroadcastReceiver(action, actionConsumers);
        }
    }

    private void registerBroadcastReceiver(String action, List<BroadcastConsumer> actionConsumers) {
        if (receivers != null) {
            BroadcastReceiver receiver = new ActionReceiver(actionConsumers);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(action);
            context.registerReceiver(receiver, intentFilter);

            receivers.put(action, receiver);
        }
    }

    public void onResume() {
        if (receivers == null) {
            receivers = new HashMap<>();
            consumers.forEach(this::registerBroadcastReceiver);
        }
    }

    public void onPause() {
        if (receivers != null) {
            receivers.values().forEach(context::unregisterReceiver);
            receivers = null;
        }
    }

    public void unregisterReceiver(String action, BroadcastConsumer consumer) {
        List<BroadcastConsumer> actionConsumers = consumers.get(action);
        if (actionConsumers == null) {
            return;
        }
        boolean removed = actionConsumers.remove(consumer);
        if (!removed) {
            return;
        }

        if (!actionConsumers.isEmpty()) {
            return;
        }
        consumers.remove(action);

        if (receivers == null) {
            return;
        }

        BroadcastReceiver receiver = receivers.get(action);
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receivers.remove(action);
        }
    }

    protected static class ActionReceiver extends BroadcastReceiver {
        private List<BroadcastConsumer> actionConsumers;

        public ActionReceiver(List<BroadcastConsumer> actionConsumers) {
            this.actionConsumers = actionConsumers;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            actionConsumers.forEach(consumer -> consumer.consumeBroadcast(context, intent));
        }
    }
}
