package org.woheller69.weather.database;

import android.content.Context;

/**
 * This class is the database model for the forecasts table.
 */
public class WeekForecast {

    public static final float NO_RAIN_VALUE = 0;
    private int id;
    private int city_id;
    private long timestamp;
    private long forecastFor;
    private int weatherID;
    private float energyDay;
    private long timeSunrise;
    private long timeSunset;

    public WeekForecast() {
    }

    public WeekForecast(int id, int city_id, long timestamp, long forecastFor, int weatherID, float temperature, float temperature_min, float temperature_max, float humidity, float pressure, float energyDay, float wind_speed, float wind_direction, float uv_index) {
        this.id = id;
        this.city_id = city_id;
        this.timestamp = timestamp;
        this.forecastFor = forecastFor;
        this.weatherID = weatherID;
        this.energyDay = energyDay;
    }


    /**
     * @return Returns the ID of the record (which uniquely identifies the record).
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return Returns the date and time for the forecast.
     */
    public long getForecastTime() {
        return forecastFor;
    }

    /**
     * @return Returns the local time for the forecast in UTC epoch
     */
    public long getLocalForecastTime(Context context) {
        SQLiteHelper dbhelper = SQLiteHelper.getInstance(context);
        int timezoneseconds = dbhelper.getGeneralDataByCityId(city_id).getTimeZoneSeconds();
        return forecastFor + timezoneseconds * 1000L;
    }

    /**
     * @param forecastFor The point of time for the forecast.
     */
    public void setForecastTime(long forecastFor) {
        this.forecastFor = forecastFor;
    }

    /**
     * @return Returns the point of time when the data was inserted into the database in Unix, UTC.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp The point of time to set when the data was inserted into the database in
     *                  Unix, UTC.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCity_id() {
        return city_id;
    }

    public void setCity_id(int city_id) {
        this.city_id = city_id;
    }

    /**
     * @return Returns the weather condition ID.
     */
    public int getWeatherID() {
        return weatherID;
    }

    /**
     * @param weatherID The weather condition ID to set.
     */
    public void setWeatherID(int weatherID) {
        this.weatherID = weatherID;
    }

    public float getEnergyDay() {return energyDay;}

    public void setEnergyDay(float energyDay) {this.energyDay =energyDay;}

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

}
