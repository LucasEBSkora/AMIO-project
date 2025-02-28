package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
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
        timer.schedule(task, 0, 30000);
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


        // Vérifier seulement la mesure la plus récente
        if (!measurements.isEmpty()) {
            Measurement mostRecentMeasurement = measurements.get(measurements.size() - 1);

            // Vérifie si la mesure la plus récente dépasse 275 et si l'heure est valide
            if (mostRecentMeasurement.value > 275 && isValidTime()) {
                Log.d("MyService","Mesure inhabituelle dans les heures du soir");

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String emailAddress = prefs.getString("email_address", "default@example.com");

                // Créez un Intent pour le Broadcast
                Intent broadcastIntent = new Intent("com.example.myapplication.SEND_EMAIL");
                broadcastIntent.putExtra("subject", "Alerte : Valeur critique détectée");
                broadcastIntent.putExtra("body", "La mesure suivante a dépassé 275 :\n" +
                        "Label : " + mostRecentMeasurement.label + "\n" +
                        "Valeur : " + mostRecentMeasurement.value + "\n" +
                        "Heure : " + new Date(mostRecentMeasurement.timestamp));
                broadcastIntent.putExtra("recipient", emailAddress);

                // Envoyez l'Intent en Broadcast
                sendBroadcast(broadcastIntent);

            }
        }
    }

    // Vérification de l'heure pour savoir si l'alerte doit être envoyée
    private boolean isValidTime() {
        // Récupérer l'heure et le jour actuel
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Si c'est un week-end (samedi ou dimanche) et entre 19h et 23h
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            return hour >= 19 && hour <= 23;
        }

        // Si c'est un jour de semaine (lundi à vendredi) et entre 23h et 6h
        return hour >= 23 || hour < 6;
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
