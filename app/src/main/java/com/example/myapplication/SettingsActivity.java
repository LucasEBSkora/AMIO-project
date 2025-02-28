package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
public class SettingsActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Affiche le fragment de préférences
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        // Active la flèche de retour
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Gérer l'appui sur la flèche pour revenir en arrière
        finish(); // Termine cette activité pour revenir à l'activité précédente
        return true;
    }





    // Fragment pour les préférences
    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
            // Charge le fichier XML de préférences

            addPreferencesFromResource(R.xml.preferences);

            // Récupère les préférences
            SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

            // Met à jour les résumés des préférences avec les valeurs actuelles
            updateSummaries(prefs);
        }

        // Méthode pour mettre à jour les résumés des préférences
        private void updateSummaries(SharedPreferences prefs) {
            // Heure de début semaine
            EditTextPreference weekdayStartPref = (EditTextPreference) findPreference("weekday_start_hour");
            String weekdayStartValue = prefs.getString("weekday_start_hour", "23");
            weekdayStartPref.setSummary(weekdayStartValue);

            // Heure de fin semaine
            EditTextPreference weekdayEndPref = (EditTextPreference) findPreference("weekday_end_hour");
            String weekdayEndValue = prefs.getString("weekday_end_hour", "06");
            weekdayEndPref.setSummary(weekdayEndValue);

            // Heure de début week-end
            EditTextPreference weekendStartPref = (EditTextPreference) findPreference("weekend_start_hour");
            String weekendStartValue = prefs.getString("weekend_start_hour", "19");
            weekendStartPref.setSummary(weekendStartValue);

            // Heure de fin week-end
            EditTextPreference weekendEndPref = (EditTextPreference) findPreference("weekend_end_hour");
            String weekendEndValue = prefs.getString("weekend_end_hour", "23");
            weekendEndPref.setSummary(weekendEndValue);

            // Adresse email
            EditTextPreference emailPref = (EditTextPreference) findPreference("email_address");
            String emailValue = prefs.getString("email_address", "");
            emailPref.setSummary(emailValue.isEmpty() ? "Adresse non définie" : emailValue);
        }
    }
}


