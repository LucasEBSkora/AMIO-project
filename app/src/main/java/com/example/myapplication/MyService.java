package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    public static final String NEW_DATA = "com.example.myapplication.myservice.NEW_DATA";
    final String CHANNEL_ID = "lampStatus";
    Timer timer;
    SlidingWindow measurementsWindow;
    Handler handler;
    Map<String, Measurement> motesOn;

    int notificationIndex = 0;

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

        // Vérifier seulement la mesure la plus récente
        if (!measurements.isEmpty()) {
            updateData(measurements);
        }
    }

    // Vérification de l'heure pour savoir si l'alerte doit être envoyée
    private boolean isValidTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int weekdayStartValue = Integer.parseInt(prefs.getString("weekday_start_hour", "23"));
        int weekdayEndValue = Integer.parseInt(prefs.getString("weekday_end_hour", "06"));

        int weekendStartValue = Integer.parseInt(prefs.getString("weekend_start_hour", "19"));
        int weekendEndValue = Integer.parseInt(prefs.getString("weekend_end_hour", "23"));

        log("weekdayStart: " + weekdayStartValue);
        log("weekdayEndValue: " + weekdayEndValue);
        log("weekendStartValue: " + weekendStartValue);
        log("weekendEndValue: " + weekendEndValue);

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        log("hour: " + hour);
        log("day of week: " + dayOfWeek);
        // Si c'est un week-end (samedi ou dimanche) et entre 19h et 23h
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return hour >= weekendStartValue && hour <= weekendEndValue;
        }

        // Si c'est un jour de semaine (lundi à vendredi) et entre 23h et 6h
        return hour >= weekdayStartValue || hour < weekdayEndValue;
    }

    private void log(String msg) {
        Log.d("AMIO-Project", msg);
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
        Random rand = new Random();
        for (String mote : firstMeasurements.keySet()) {
            Measurement newMeasurement = firstMeasurements.get(mote);
            if (newMeasurement == null) continue;
            newMeasurement.value = rand.nextFloat() * 400;
            calculateLampState(oldMeasurements.get(mote), newMeasurement);
            motesOn.put(mote, newMeasurement);
        }

        StringBuilder measurementsStringBuilder = new StringBuilder();
        for (String mote : motesOn.keySet()) {
            Measurement measurement = motesOn.get(mote);
            if (measurement == null) continue;
            measurementsStringBuilder.append(mote).append(" is ").append(measurement.state).append(" value: ").append(measurement.value).append('\n');
        }

        Intent updateIntent = new Intent(NEW_DATA);
        updateIntent.putExtra("result", lastResult);
        updateIntent.putExtra("date", dateString);
        updateIntent.putExtra("motesOn", measurementsStringBuilder.toString());
        sendBroadcast(updateIntent);
    }

    private void calculateLampState(Measurement old, Measurement measurement) {
        double delta = 0;
        if (old != null) {
            delta = measurement.value - old.value;
        }

        if (delta > 50) {
            measurement.state = LampState.ON;
        } else if (delta < -50) {
            measurement.state = LampState.OFF;
        } else if (old != null && old.state != null && old.state != LampState.UNKNOWN) {
            measurement.state = old.state;
        } else if (measurement.value < 275) {
            measurement.state = LampState.OFF;
        } else {
            measurement.state = LampState.ON;
        }
        log("state:" + measurement.state);

        if (old != null && old.state != measurement.state) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(measurement.state == LampState.ON ? R.drawable.ic_lamp_on : R.drawable.ic_lamp_off)
                    .setContentTitle("Lamp status changed!")
                    .setContentText("lamp at " + measurement.mote + " turned " + measurement.state)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                NotificationManagerCompat.from(this).notify(notificationIndex++, builder.build());
            }

            if (measurement.state == LampState.ON && isValidTime()) {
                log("Mesure inhabituelle dans les heures du soir");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String emailAddress = prefs.getString("email_address", "default@example.com");

                log("message envoye a " + emailAddress);

                // Créez un Intent pour le Broadcast
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setDataAndType(Uri.parse("mailto:"), "text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Alerte : Valeur critique détectée");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "La mesure suivante a dépassé 275 :\n" +
                        "Label : " + measurement.label + "\n" +
                        "Valeur : " + measurement.value + "\n" +
                        "Heure : " + new Date(measurement.timestamp));

                // Envoyez l'Intent en Broadcast
                emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(emailIntent, "Choisir client e-mail:"));

            }
        }
    }
}
