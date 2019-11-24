package com.axotsoft.terb.slice;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import androidx.slice.builders.ListBuilder;
import androidx.slice.builders.ListBuilder.RowBuilder;
import androidx.slice.builders.SliceAction;

import com.axotsoft.terb.R;
import com.axotsoft.terb.widget.WidgetClickReceiver;

public class CommandSliceProvider extends SliceProvider
{
    /**
     * Instantiate any required objects. Return true if the provider was successfully created,
     * false otherwise.
     */
    @Override
    public boolean onCreateSliceProvider()
    {
        return true;
    }

    /**
     * Converts URL to content URI (i.e. content://com.axotsoft.terb.slice...)
     */
    @Override
    @NonNull
    public Uri onMapIntentToUri(@Nullable Intent intent)
    {
        Uri.Builder uriBuilder = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT);
        if (intent == null) return uriBuilder.build();
        Uri data = intent.getData();
        if (data != null && data.getPath() != null)
        {
            String path = data.getPath().replace("/command", "");
            uriBuilder = uriBuilder.path(path);
        }
        Context context = getContext();
        if (context != null)
        {
            uriBuilder = uriBuilder.authority(context.getPackageName());
        }
        return uriBuilder.build();
    }

    /**
     * Construct the Slice and bind data if available.
     */
    public Slice onBindSlice(Uri sliceUri)
    {
        if ("/".equals(sliceUri.getPath()))
        {
            // Path recognized. Customize the Slice using the androidx.slice.builders API.
            // Note: ANRs and strict mode is enforced here so don't do any heavy operations.
            // Only bind data that is currently available in memory.
            return new ListBuilder(getContext(), sliceUri, ListBuilder.INFINITY)
                    .addRow(
                            new RowBuilder()
                                    .setTitle("URI found.")
                    )
                    .build();
        }
        else
        {
            // Error: Path not found.
            return new ListBuilder(getContext(), sliceUri, ListBuilder.INFINITY)
                    .addRow(
                            new RowBuilder()
                                    .setTitle(sliceUri.getQuery()).setPrimaryAction(getSliceAction(getContext()))
                    )
                    .build();
        }
    }

    private SliceAction getSliceAction(Context context)
    {

        return SliceAction.create(WidgetClickReceiver.makeIntent(context, AppWidgetManager.INVALID_APPWIDGET_ID), IconCompat.createWithResource(context, R.mipmap.ic_launcher),
                ListBuilder.SMALL_IMAGE, "Test");
    }

    /**
     * Slice has been pinned to external process. Subscribe to data source if necessary.
     */
    @Override
    public void onSlicePinned(Uri sliceUri)
    {
        // When data is received, call context.contentResolver.notifyChange(sliceUri, null) to
        // trigger CommandSliceProvider#onBindSlice(Uri) again.
    }

    /**
     * Unsubscribe from data source if necessary.
     */
    @Override
    public void onSliceUnpinned(Uri sliceUri)
    {
        // Remove any observers if necessary to avoid memory leaks.
    }
}
