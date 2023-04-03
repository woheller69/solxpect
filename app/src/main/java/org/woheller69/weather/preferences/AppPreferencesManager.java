package org.woheller69.weather.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import org.woheller69.weather.BuildConfig;
import org.woheller69.weather.R;

/**
 * This class provides access and methods for relevant preferences.
 */
public class AppPreferencesManager {


    /**
     * Member variables
     */
    SharedPreferences preferences;

    /**
     * Constructor.
     *
     * @param preferences Source for the preferences to use.
     */
    public AppPreferencesManager(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isFirstTimeLaunch(Context context) {
        boolean result = preferences.getBoolean("firstLaunch", true);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("firstLaunch",false);
        editor.apply();
        return result;
    }

    public boolean showStarDialog(Context context) {
        int versionCode = preferences.getInt("versionCode",BuildConfig.VERSION_CODE);
        boolean askForStar=preferences.getBoolean("askForStar",true);

        if (!isFirstTimeLaunch(context) && BuildConfig.VERSION_CODE>versionCode && askForStar){ //not at first start, only after upgrade and only if use has not yet given a star or has declined
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", BuildConfig.VERSION_CODE);
            editor.apply();
         return true;
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("versionCode", BuildConfig.VERSION_CODE);
            editor.apply();
          return false;
        }
    }

    public void setAskForStar(boolean askForStar){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("askForStar", askForStar);
        editor.apply();
    }
}
