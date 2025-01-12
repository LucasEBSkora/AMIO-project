package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    Timer timer;
    Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        handler = new Handler();
        log("Service Created!");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        log("Service stopped!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("Service Started!");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                callback();
            }
        };
        timer.schedule(task, 0, 3000);
        return START_STICKY;
    }

    private void callback() {

        URL url = null;
        try {
            url = new URL(getResources().getString(R.string.serverURL));
        } catch (MalformedURLException e) {
            log("malformed URL: " + e.getMessage());
            return;
        }
        HttpURLConnection connection;
        List<Measurement> measurements;
        try {
            connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                handler.post(() -> Toast.makeText(this, "Problem reaching server - response code " + responseCode, Toast.LENGTH_SHORT).show());
                return;
            }
            log(Integer.toString(responseCode));

            JSONParser parser = new JSONParser(connection.getInputStream());
            measurements = parser.getMeasurements();

        } catch (IOException e) {
            log("IO Exception: " + e.getMessage());
            return;
        }

        DataManager.getInstance().updateData(measurements);
    }

    private void log(String msg) {
        Log.d("TP1", msg);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
