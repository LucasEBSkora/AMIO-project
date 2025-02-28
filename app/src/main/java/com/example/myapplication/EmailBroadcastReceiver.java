package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EmailBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BroadcastReceiver", "Intent reçu pour envoyer un e-mail");

        // Extraire les données de l'Intent
        String subject = intent.getStringExtra("subject");
        String body = intent.getStringExtra("body");
        String recipient = intent.getStringExtra("recipient");

        // Créer un Intent pour ouvrir une application d'e-mail
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        // Lancer l'application d'e-mail avec startActivity
        context.startActivity(Intent.createChooser(emailIntent, "Envoyer un e-mail..."));
    }
}
