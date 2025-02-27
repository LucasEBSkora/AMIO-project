package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TP1";
    private final Map<String, LampState> motesOn;
    final private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "broadcast received: " + intent.getAction());
            if (!MyService.NEW_DATA.equals(intent.getAction())) {
                return;
            }
            final String resultString = intent.getStringExtra("result");
            TextView lastResultTextView = findViewById(R.id.textView4);
            lastResultTextView.setText(resultString);

            final String dateString = intent.getStringExtra("date");
            TextView timeLastResultTextView = findViewById(R.id.textView6);
            timeLastResultTextView.setText(dateString);

            final String StringMotesOn = intent.getStringExtra("motesOn");
            TextView measurementsView = findViewById(R.id.textView7);
            measurementsView.setText(StringMotesOn);
        }
    };

    public MainActivity() {
        motesOn = new HashMap<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });

        IntentFilter filter = new IntentFilter(MyService.NEW_DATA);
        registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);

        ToggleButton buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TextView view2 = findViewById(R.id.textView2);
            String newText;
            if (isChecked) {
                startService(new Intent(getApplicationContext(), MyService.class));
                newText = getResources().getString(R.string.running);
            } else {
                stopService(new Intent(getApplicationContext(), MyService.class));
                newText = getResources().getString(R.string.stopped);
            }
            view2.setText(newText);
        });

        Switch startAtBoot = findViewById(R.id.startAtBoot);
        startAtBoot.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d("TP1", "start at boot changed to " + isChecked);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("startOnBoot", isChecked);
            edit.apply();
        });


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean startOnBoot = prefs.getBoolean("startOnBoot", false);
        if (startOnBoot) {
            buttonStart.setChecked(true);
            TextView view2 = findViewById(R.id.textView2);
            view2.setText("Running!");
            startAtBoot.setChecked(true);
        }

        Button settingsButton = findViewById(R.id.action_settings);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void updateView(@NonNull SlidingWindow measurements) {
        Map<String, Measurement> firstMeasurements = measurements.getMostRecent();
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

        Map<String, Measurement> oldMeasurements = measurements.get(0);
        for (String mote : firstMeasurements.keySet()) {
            motesOn.put(mote, getLampState(oldMeasurements.get(mote), firstMeasurements.get(mote)));
        }


        StringBuilder measurementsStringBuilder = new StringBuilder();
        for (String mote : motesOn.keySet()) {
            LampState state = motesOn.get(mote);
            measurementsStringBuilder.append(mote).append(": ").append(state).append('\n');
        }

        final String measurementsString = measurementsStringBuilder.toString();

        runOnUiThread(() -> {
            TextView lastResultTextView = findViewById(R.id.textView4);
            lastResultTextView.setText(lastResult);

            TextView timeLastResultTextView = findViewById(R.id.textView6);
            timeLastResultTextView.setText(dateString);

            TextView measurementsView = findViewById(R.id.textView7);
            measurementsView.setText(measurementsString);
        });

    }

    @NonNull
    private LampState getLampState(Measurement old, Measurement measurement) {
        if (old != null) {
            final double delta = measurement.value - old.value;
            if (delta > 50) {
                return LampState.ON;
            } else if (delta < -50) {
                return LampState.OFF;
            }
        } else {
            if (measurement.value < 100) {
                return LampState.OFF;
            } else if (measurement.value > 300) {
                return LampState.ON;
            }
        }
        return LampState.UNKNOWN;
    }


}