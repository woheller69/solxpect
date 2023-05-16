package org.woheller69.weather.ui.Help;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import androidx.preference.PreferenceManager;

import androidx.core.content.res.ResourcesCompat;
import org.woheller69.weather.R;
import org.woheller69.weather.preferences.AppPreferencesManager;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static java.lang.Boolean.TRUE;

public final class StringFormatUtils {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private static final DecimalFormat intFormat = new DecimalFormat("0");

    public static String formatDecimal(float decimal) {
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
        return removeMinusIfZerosOnly(decimalFormat.format(decimal));
    }

    public static String formatEnergyCum(Context context, float energyCum) {
        if (energyCum < 10000.0f) return formatInt(energyCum, context.getString(R.string.units_Wh));
        else if (energyCum < 100000.0f) return formatDecimal(energyCum/1000,context.getString(R.string.units_kWh));
        else return formatInt(energyCum/1000,context.getString(R.string.units_kWh));
    }

    public static String formatInt(float decimal) {
        intFormat.setRoundingMode(RoundingMode.HALF_UP);
        return removeMinusIfZerosOnly(intFormat.format(decimal));
    }

    public static String formatInt(float decimal, String appendix) {
        return String.format("%s\u200a%s", removeMinusIfZerosOnly(formatInt(decimal)), appendix); //\u200a adds tiny space
    }

    public static String formatDecimal(float decimal, String appendix) {
        return String.format("%s\u200a%s", removeMinusIfZerosOnly(formatDecimal(decimal)), appendix);
    }

    public static String formatTimeWithoutZone(Context context, long time) {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(context);
        SimpleDateFormat df;
        if (android.text.format.DateFormat.is24HourFormat(context) || sharedPreferences.getBoolean("pref_TimeFormat", true)==TRUE){
            df = new SimpleDateFormat("HH:mm", Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
        }else {
            df = new SimpleDateFormat("hh:mm aa", Locale.getDefault());
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return df.format(time);
    }

    public static Integer getDayShort(int day){

        switch(day)    {
            case Calendar.MONDAY:
                day = R.string.abbreviation_monday;
                break;
            case Calendar.TUESDAY:
                day = R.string.abbreviation_tuesday;
                break;
            case Calendar.WEDNESDAY:
                day = R.string.abbreviation_wednesday;
                break;
            case Calendar.THURSDAY:
                day = R.string.abbreviation_thursday;
                break;
            case Calendar.FRIDAY:
                day = R.string.abbreviation_friday;
                break;
            case Calendar.SATURDAY:
                day = R.string.abbreviation_saturday;
                break;
            case Calendar.SUNDAY:
                day = R.string.abbreviation_sunday;
                break;
            default:
                day = R.string.abbreviation_monday;
        }
        return day;
    }

    public static Integer getDayLong(int day){

        switch(day)    {
            case Calendar.MONDAY:
                day = R.string.monday;
                break;
            case Calendar.TUESDAY:
                day = R.string.tuesday;
                break;
            case Calendar.WEDNESDAY:
                day = R.string.wednesday;
                break;
            case Calendar.THURSDAY:
                day = R.string.thursday;
                break;
            case Calendar.FRIDAY:
                day = R.string.friday;
                break;
            case Calendar.SATURDAY:
                day = R.string.saturday;
                break;
            case Calendar.SUNDAY:
                day = R.string.sunday;
                break;
            default:
                day = R.string.monday;
        }
        return day;
    }

    public static String removeMinusIfZerosOnly(String string){
        // It removes (replaces with "") the minus sign if it's followed by 0-n characters of "0.00000...",
        // so this will work for any similar result such as "-0", "-0." or "-0.000000000"
        // https://newbedev.com/negative-sign-in-case-of-zero-in-java
        return string.replaceAll("^-(?=0(\\.0*)?$)", "");
    }
}
