package com.axotsoft.wicket.utils;

import android.content.Context;
import android.content.Intent;

@FunctionalInterface
public interface BroadcastConsumer {
    void consumeBroadcast(Context context, Intent intent);
}
