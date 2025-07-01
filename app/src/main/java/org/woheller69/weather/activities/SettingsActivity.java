package org.woheller69.weather.activities;


import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowInsetsController;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import org.woheller69.weather.R;
import org.woheller69.weather.database.SQLiteHelper;

public class SettingsActivity extends NavigationActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    protected void onRestart() {
        super.onRestart();

        recreate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            );
        }
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_settings;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
            public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
                setPreferencesFromResource(R.xml.pref_general, rootKey);
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_number_days")){
                SeekBarPreference numberDays = findPreference("pref_number_days");
                if (numberDays.getValue()<3) numberDays.setValue(3);
            } else if (key.equals("pref_summarize")){
                if (sharedPreferences.getBoolean("pref_summarize",false)) {
                    SQLiteHelper database = SQLiteHelper.getInstance(getActivity());
                    database.deleteAllForecasts();
                }
            }
        }
    }
}
