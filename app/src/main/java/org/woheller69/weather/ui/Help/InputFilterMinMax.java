package org.woheller69.weather.ui.Help;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {

    private float min, max;

    public InputFilterMinMax(float min, float max) {
        this.min = min;
        this.max = max;
    }


    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String oldString = dest.toString();
            String insertString = source.toString();
            String newString = oldString.substring(0, dstart) + oldString.substring(dend);
            newString = newString.substring(0, dstart) + insertString + newString.substring(dstart);
            float input = Float.parseFloat(newString);

            if (isInRange(min, max, input)) {
                return null;
            } else {
                if (source.equals("") && dest.toString().length() != 1) {
                    //backspace was clicked, do not accept that change, unless user is deleting the last char
                    return dest.subSequence(dstart, dend);
                } else {
                    return "";
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean isInRange(float a, float b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }

}
