package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Affiche le fragment de préférences
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    // Fragment pour les préférences
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
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


