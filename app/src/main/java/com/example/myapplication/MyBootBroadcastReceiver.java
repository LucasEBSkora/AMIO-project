package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;

public class MyBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Broadcast receiver", "received:" + intent.getAction());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean startOnBoot = prefs.getBoolean("startOnBoot", false);
        Log.d("TP1", "start on boot=" + startOnBoot);
        if (startOnBoot) {
            context.startService(new Intent(context, MyService.class));
        }


    }
}
