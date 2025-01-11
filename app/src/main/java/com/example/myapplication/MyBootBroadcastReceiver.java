package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class MyBootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Broadcast receiver", "received:" + intent.getAction());
        SharedPreferences prefs = context.getSharedPreferences("settingsTP1",Context.MODE_PRIVATE);
        boolean startOnBoot  =prefs.getBoolean("startOnBoot", false);
        Log.d("TP1", "start on boot=" + startOnBoot);
        if(startOnBoot) {
            context.startService(new Intent(context, MyService.class));
        }
    }
}
