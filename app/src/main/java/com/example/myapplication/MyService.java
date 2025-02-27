package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    public static final String NEW_DATA = "com.example.myapplication.myservice.NEW_DATA";
    final String CHANNEL_ID = "lampStatus";
    Timer timer;
    SlidingWindow measurementsWindow;
    Handler handler;
    Map<String, LampState> motesOn;

    @Override
    public void onCreate() {
        super.onCreate();
        timer = new Timer();
        handler = new Handler();
        measurementsWindow = new SlidingWindow(2);
        motesOn = new HashMap<>();
        log("Service Created!");

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        CharSequence name = getString(R.string.channel_name);
        String description = getString(R.string.channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        log("Service stopped!");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
        timer.schedule(task, 0, 1000);
        return START_STICKY;
    }

    private void callback() {

        URL url;
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

        updateData(measurements);
    }

    private void log(String msg) {
        Log.d("TP1", msg);
    }

    public void updateData(List<Measurement> measurements) {
        measurementsWindow.addMeasurements(measurements);
        if (!measurementsWindow.windowFull()) return;
        Map<String, Measurement> firstMeasurements = measurementsWindow.getMostRecent();
        if (firstMeasurements == null || firstMeasurements.isEmpty())
            return;

        Optional<Measurement> mostRecentOpt = firstMeasurements.values().stream().reduce((a, b) -> a.timestamp > b.timestamp ? a : b);

        if (mostRecentOpt.isEmpty()) {
            return;
        }
        Measurement mostRecent = mostRecentOpt.get();

        DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
        final String dateString = formatter.format(new Date(mostRecent.timestamp));
        final String lastResult = String.format("%s", mostRecent.value);

        Map<String, Measurement> oldMeasurements = measurementsWindow.get(0);
        for (String mote : firstMeasurements.keySet()) {
            motesOn.put(mote, getLampState(oldMeasurements.get(mote), firstMeasurements.get(mote)));
        }

        StringBuilder measurementsStringBuilder = new StringBuilder();
        for (String mote : motesOn.keySet()) {
            measurementsStringBuilder.append(mote).append(": ").append(motesOn.get(mote)).append('\n');
        }

        Intent updateIntent = new Intent(NEW_DATA);
        updateIntent.putExtra("result", lastResult);
        updateIntent.putExtra("date", dateString);
        updateIntent.putExtra("motesOn", measurementsStringBuilder.toString());
        sendBroadcast(updateIntent);
    }

    @NonNull
    private LampState getLampState(Measurement old, Measurement measurement) {
        double delta = 0;
        if (old != null) {
            delta = measurement.value - old.value;
        }

        if (delta > 50) {
            measurement.state = LampState.ON;
        } else if (delta < -50) {
            measurement.state = LampState.OFF;
        } else if (old != null && old.state != LampState.UNKNOWN) {
            measurement.state = old.state;
        } else if (measurement.value < 150) {
            measurement.state = LampState.OFF;
        } else {
            measurement.state = LampState.ON;
        }

        if (old != null && old.state != measurement.state) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(measurement.state == LampState.ON ? R.drawable.ic_lamp_on : R.drawable.ic_lamp_off)
                    .setContentTitle("Lamp status changed!")
                    .setContentText("lamp at " + measurement.mote + " turned " + measurement.state)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(0, builder.build());
            }
        }

        return measurement.state;
    }
}
