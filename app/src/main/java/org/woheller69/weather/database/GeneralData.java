package org.woheller69.weather.database;

import android.content.Context;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * This class represents the database model for current weather data of cities.
 */

public class GeneralData {

    private int id;
    private int city_id;
    private long timestamp;
    private long timeSunrise;
    private long timeSunset;
    private int timeZoneSeconds;

    public GeneralData() {
        this.city_id = Integer.MIN_VALUE;
    }

    public GeneralData(int id, int city_id, long timestamp, int weatherID, float temperatureCurrent, float temperatureMin, float temperatureMax, float humidity, float pressure, float windSpeed, float windDirection, float cloudiness, long timeSunrise, long timeSunset, int timeZoneSeconds) {
        this.id = id;
        this.city_id = city_id;
        this.timestamp = timestamp;
        this.timeSunrise = timeSunrise;
        this.timeSunset = timeSunset;
        this.timeZoneSeconds = timeZoneSeconds;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCity_id() {
        return city_id;
    }

    public void setCity_id(int city_id) {
        this.city_id = city_id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public boolean isDay(Context context){
        Calendar timeStamp = Calendar.getInstance();
        timeStamp.setTimeZone(TimeZone.getTimeZone("GMT"));
        timeStamp.setTimeInMillis((timestamp+timeZoneSeconds)*1000);
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
        if (timeSunrise==0 || timeSunset==0){
            if ((dbHelper.getCityToWatch(city_id).getLatitude())>0){  //northern hemisphere
                return timeStamp.get(Calendar.DAY_OF_YEAR) >= 80 && timeStamp.get(Calendar.DAY_OF_YEAR) <= 265;  //from March 21 to September 22 (incl)
            }else{ //southern hemisphere
                return timeStamp.get(Calendar.DAY_OF_YEAR) < 80 || timeStamp.get(Calendar.DAY_OF_YEAR) > 265;
            }
        }else {
            return timestamp > timeSunrise && timestamp < timeSunset;
        }
    }

    public long getTimeSunrise() { return timeSunrise; }

    public void setTimeSunrise(long timeSunrise) {
        this.timeSunrise = timeSunrise;
    }

    public long getTimeSunset() {
        return timeSunset;
    }

    public void setTimeSunset(long timeSunset) {
        this.timeSunset = timeSunset;
    }

    public int getTimeZoneSeconds() {
        return timeZoneSeconds;
    }

    public void setTimeZoneSeconds(int timeZoneSeconds) {
        this.timeZoneSeconds = timeZoneSeconds;
    }

}
