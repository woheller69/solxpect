package org.woheller69.weather.ui.Help;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {

    private int min, max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }


    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String oldString = dest.toString();
        String insertString = source.toString();
        String newString = new StringBuilder(oldString).insert(dstart,insertString).toString();
        float input = Float.parseFloat(newString);
        if (isInRange(min, max, input))
            return null;
        else
            return "";
    }

    private boolean isInRange(int a, int b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

}
