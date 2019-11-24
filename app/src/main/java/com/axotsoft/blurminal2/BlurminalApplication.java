package com.axotsoft.blurminal2;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.service.voice.VoiceInteractionService;

import androidx.slice.SliceManager;

import java.util.List;

public class BlurminalApplication extends Application
{
    private static final String SLICE_AUTHORITY = "com.axotsoft.blurminal2.slice";

    @Override
    public void onCreate()
    {
        super.onCreate();
        grantSlicePermissions();
    }

    private void grantSlicePermissions()
    {
        Context context = getApplicationContext();
        Uri sliceProviderUri =
                new Uri.Builder()
                        .scheme(ContentResolver.SCHEME_CONTENT)
                        .authority(SLICE_AUTHORITY)
                        .build();

        String assistantPackage = getAssistantPackage(context);
        if (assistantPackage == null)
        {
            return;
        }
        SliceManager.getInstance(context)
                .grantSlicePermission(assistantPackage, sliceProviderUri);
    }

    private String getAssistantPackage(Context context)
    {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentServices(
                new Intent(VoiceInteractionService.SERVICE_INTERFACE), 0);
        if (resolveInfoList.isEmpty())
        {
            return null;
        }
        return resolveInfoList.get(0).serviceInfo.packageName;
    }
}
