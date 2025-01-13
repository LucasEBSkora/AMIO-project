package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TP1";

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
            SharedPreferences prefs = getSharedPreferences("settingsTP1", MODE_PRIVATE);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean("startOnBoot", isChecked);
            edit.apply();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}