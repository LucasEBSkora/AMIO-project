package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    boolean alive;
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
        Log.d("TP1", "Création de l'activité");

        ToggleButton buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnCheckedChangeListener((buttonView, isChecked) -> {
            TextView view2 = (TextView) findViewById(R.id.textView2);
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
        startAtBoot.setOnCheckedChangeListener((buttonView,isChecked)->{
            Log.d("TP1", "start at boot changed to " + isChecked);
            SharedPreferences prefs = getSharedPreferences("settingsTP1", MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("startOnBoot", isChecked);
            edit.apply();
        });

        DataManager.getInstance().addListener(this, this::updateView);
    }


    @Override
    protected void onDestroy() {
        DataManager.getInstance().removeListener(this);
        super.onDestroy();
    }

    public void updateView(List<Measurement> measurements) {
        if (measurements == null || measurements.isEmpty())
            return;

        Measurement mostRecent = measurements.get(0);
        for (Measurement measurement : measurements) {
            if (measurement.timestamp > mostRecent.timestamp) {
                mostRecent = measurement;
            }
        }
        TextView lastResultTextView = findViewById(R.id.textView4);
        lastResultTextView.setText(String.format("%s", mostRecent.value));
        TextView timeLastResultTextView = findViewById(R.id.textView6);
        DateFormat formatter = SimpleDateFormat.getDateTimeInstance();
        String dateString = formatter.format(new Date(mostRecent.timestamp));
        timeLastResultTextView.setText(dateString);

        StringBuilder measurementsString = new StringBuilder();
        for (Measurement measurement: measurements) {
            measurementsString.append(measurement.toString()).append('\n');
        }

        TextView measurementsView = findViewById(R.id.textView7);
        measurementsView.setText(measurementsString.toString());

    }
}