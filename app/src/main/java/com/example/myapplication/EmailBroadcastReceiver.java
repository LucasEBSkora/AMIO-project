package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
        Intent emailIntent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        // Utilisez un chooser pour afficher l'option d'envoi de l'email
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(emailIntent, "Envoyer un mail..."));
    }
}
