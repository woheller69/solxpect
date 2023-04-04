package org.woheller69.weather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.app.JobIntentService.enqueueWork;

/**
 * @author Karola Marky, Christopher Beckmann
 * @version 1.0
 * @since 25.01.2018
 * created 02.01.2017
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private Context context;

    private List<City> allCities = new ArrayList<>();

    private static SQLiteHelper instance = null;

    private static final String DATABASE_NAME = "SQLITE.db";

    //Names of tables in the database
    private static final String TABLE_CITIES_TO_WATCH = "CITIES_TO_WATCH";
    private static final String TABLE_HOURLY_FORECAST = "FORECASTS";
    private static final String TABLE_WEEKFORECAST = "WEEKFORECASTS";
    private static final String TABLE_GENERAL_DATA = "GENERAL_DATA";


    //Names of columns in TABLE_CITIES_TO_WATCH
    private static final String CITIES_TO_WATCH_ID = "cities_to_watch_id";
    private static final String CITIES_TO_WATCH_CITY_ID = "city_id";
    private static final String CITIES_TO_WATCH_COLUMN_RANK = "rank";
    private static final String CITIES_TO_WATCH_NAME = "city_name";
    private static final String CITIES_TO_WATCH_LONGITUDE = "longitude";
    private static final String CITIES_TO_WATCH_LATITUDE = "latitude";
    private static final String CITIES_TO_WATCH_CELLS_MAX_POWER = "cells_max_pPower";
    private static final String CITIES_TO_WATCH_CELLS_AREA = "cells_area";
    private static final String CITIES_TO_WATCH_CELLS_EFFICIENCY = "cells_efficiency";
    private static final String CITIES_TO_WATCH_DIFFUSE_EFFICIENCY = "diffuse_efficiency";
    private static final String CITIES_TO_WATCH_INVERTER_POWER_LIMIT = "inverter_power_limit";
    private static final String CITIES_TO_WATCH_INVERTER_EFFICIENCY = "inverter_efficiency";
    private static final String CITIES_TO_WATCH_AZIMUTH_ANGLE = "azimuth_angle";
    private static final String CITIES_TO_WATCH_TILT_ANGLE = "tilt_angle";
    private static final String CITIES_TO_WATCH_SHADING_ELEVATION = "shading_elevation";
    private static final String CITIES_TO_WATCH_SHADING_OPACITY = "shading_opacity";

    //Names of columns in TABLE_FORECAST
    private static final String FORECAST_ID = "forecast_id";
    private static final String FORECAST_CITY_ID = "city_id";
    private static final String FORECAST_COLUMN_TIME_MEASUREMENT = "time_of_measurement";
    private static final String FORECAST_COLUMN_FORECAST_FOR = "forecast_for";
    private static final String FORECAST_COLUMN_WEATHER_ID = "weather_id";
    private static final String FORECAST_COLUMN_DIRECT_RADIATION_NORMAL = "direct_radiation_normal";
    private static final String FORECAST_COLUMN_DIFFUSE_RADIATION = "diffuse_radiation";
    private static final String FORECAST_COLUMN_POWER = "power";


    //Names of columns in TABLE_WEEKFORECAST
    private static final String WEEKFORECAST_ID = "forecast_id";
    private static final String WEEKFORECAST_CITY_ID = "city_id";
    private static final String WEEKFORECAST_COLUMN_TIME_MEASUREMENT = "time_of_measurement";
    private static final String WEEKFORECAST_COLUMN_FORECAST_FOR = "forecast_for";
    private static final String WEEKFORECAST_COLUMN_WEATHER_ID = "weather_id";
    private static final String WEEKFORECAST_COLUMN_ENERGY_DAY = "energy_day";
    private static final String WEEKFORECAST_COLUMN_TIME_SUNRISE = "time_sunrise";
    private static final String WEEKFORECAST_COLUMN_TIME_SUNSET = "time_sunset";


    //Names of columns in TABLE_GENERAL_DATA
    private static final String COLUMN_ID = "current_id";
    private static final String COLUMN_CITY_ID = "city_id";
    private static final String COLUMN_TIME_MEASUREMENT = "time_of_measurement";
    private static final String COLUMN_TIME_SUNRISE = "time_sunrise";
    private static final String COLUMN_TIME_SUNSET = "time_sunset";
    private static final String COLUMN_TIMEZONE_SECONDS = "timezone_seconds";

    /**
     * Create Table statements for all tables
     */
    private static final String CREATE_GENERAL_DATA = "CREATE TABLE " + TABLE_GENERAL_DATA +
            "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_CITY_ID + " INTEGER," +
            COLUMN_TIME_MEASUREMENT + " LONG NOT NULL," +
            COLUMN_TIME_SUNRISE + "  LONG NOT NULL," +
            COLUMN_TIME_SUNSET + "  LONG NOT NULL," +
            COLUMN_TIMEZONE_SECONDS + " INTEGER)";

    private static final String CREATE_TABLE_FORECASTS = "CREATE TABLE " + TABLE_HOURLY_FORECAST +
            "(" +
            FORECAST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            FORECAST_CITY_ID + " INTEGER," +
            FORECAST_COLUMN_TIME_MEASUREMENT + " LONG NOT NULL," +
            FORECAST_COLUMN_FORECAST_FOR + " VARCHAR(200) NOT NULL," +
            FORECAST_COLUMN_WEATHER_ID + " INTEGER," +
            FORECAST_COLUMN_DIRECT_RADIATION_NORMAL + " REAL," +
            FORECAST_COLUMN_DIFFUSE_RADIATION + " REAL," +
            FORECAST_COLUMN_POWER + " REAL)";

    private static final String CREATE_TABLE_WEEKFORECASTS = "CREATE TABLE " + TABLE_WEEKFORECAST +
            "(" +
            WEEKFORECAST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            WEEKFORECAST_CITY_ID + " INTEGER," +
            WEEKFORECAST_COLUMN_TIME_MEASUREMENT + " LONG NOT NULL," +
            WEEKFORECAST_COLUMN_FORECAST_FOR + " VARCHAR(200) NOT NULL," +
            WEEKFORECAST_COLUMN_WEATHER_ID + " INTEGER," +
            WEEKFORECAST_COLUMN_ENERGY_DAY + " REAL," +
            WEEKFORECAST_COLUMN_TIME_SUNRISE + "  LONG NOT NULL," +
            WEEKFORECAST_COLUMN_TIME_SUNSET + "  LONG NOT NULL)";

    private static final String CREATE_TABLE_CITIES_TO_WATCH = "CREATE TABLE " + TABLE_CITIES_TO_WATCH +
            "(" +
            CITIES_TO_WATCH_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            CITIES_TO_WATCH_CITY_ID + " INTEGER," +
            CITIES_TO_WATCH_COLUMN_RANK + " INTEGER," +
            CITIES_TO_WATCH_NAME + " VARCHAR(100) NOT NULL," +
            CITIES_TO_WATCH_LONGITUDE + " REAL NOT NULL," +
            CITIES_TO_WATCH_LATITUDE + " REAL NOT NULL," +
            CITIES_TO_WATCH_CELLS_MAX_POWER + " REAL NOT NULL," +
            CITIES_TO_WATCH_CELLS_AREA + " REAL NOT NULL," +
            CITIES_TO_WATCH_CELLS_EFFICIENCY + " REAL NOT NULL," +
            CITIES_TO_WATCH_DIFFUSE_EFFICIENCY + " REAL NOT NULL," +
            CITIES_TO_WATCH_INVERTER_POWER_LIMIT + " REAL NOT NULL," +
            CITIES_TO_WATCH_INVERTER_EFFICIENCY + " REAL NOT NULL," +
            CITIES_TO_WATCH_AZIMUTH_ANGLE + " REAL NOT NULL," +
            CITIES_TO_WATCH_TILT_ANGLE + " REAL NOT NULL," +
            CITIES_TO_WATCH_SHADING_ELEVATION + " VARCHAR(255) NOT NULL," +
            CITIES_TO_WATCH_SHADING_OPACITY + " VARCHAR(255) NOT NULL)";

    public static SQLiteHelper getInstance(Context context) {
        if (instance == null && context != null) {
            instance = new SQLiteHelper(context.getApplicationContext());
        }
        return instance;
    }

    private SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CITIES_TO_WATCH);
        db.execSQL(CREATE_GENERAL_DATA);
        db.execSQL(CREATE_TABLE_FORECASTS);
        db.execSQL(CREATE_TABLE_WEEKFORECASTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }


    /**
     * Methods for TABLE_CITIES_TO_WATCH
     */
    public synchronized long addCityToWatch(CityToWatch city) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CITIES_TO_WATCH_CITY_ID, city.getCityId());
        values.put(CITIES_TO_WATCH_COLUMN_RANK, city.getRank());
        values.put(CITIES_TO_WATCH_NAME,city.getCityName());
        values.put(CITIES_TO_WATCH_LATITUDE,city.getLatitude());
        values.put(CITIES_TO_WATCH_LONGITUDE,city.getLongitude());
        values.put(CITIES_TO_WATCH_CELLS_MAX_POWER,city.getCellsMaxPower());
        values.put(CITIES_TO_WATCH_CELLS_AREA,city.getCellsArea());
        values.put(CITIES_TO_WATCH_CELLS_EFFICIENCY,city.getCellsEfficiency());
        values.put(CITIES_TO_WATCH_DIFFUSE_EFFICIENCY,city.getDiffuseEfficiency());
        values.put(CITIES_TO_WATCH_INVERTER_POWER_LIMIT,city.getInverterPowerLimit());
        values.put(CITIES_TO_WATCH_INVERTER_EFFICIENCY,city.getInverterEfficiency());
        values.put(CITIES_TO_WATCH_AZIMUTH_ANGLE,city.getAzimuthAngle());
        values.put(CITIES_TO_WATCH_TILT_ANGLE,city.getTiltAngle());
        values.put(CITIES_TO_WATCH_SHADING_ELEVATION,city.getShadingElevationString());
        values.put(CITIES_TO_WATCH_SHADING_OPACITY,city.getShadingOpacityString());

        long id=database.insert(TABLE_CITIES_TO_WATCH, null, values);

        //use id also instead of city id as unique identifier
        values.put(CITIES_TO_WATCH_CITY_ID,id);
        database.update(TABLE_CITIES_TO_WATCH, values, CITIES_TO_WATCH_ID + " = ?",
                new String[]{String.valueOf(id)});

        database.close();
        return id;
    }

    public synchronized CityToWatch getCityToWatch(int id) {
        SQLiteDatabase database = this.getWritableDatabase();

        String[] arguments = {String.valueOf(id)};

        Cursor cursor = database.rawQuery(
                "SELECT " + CITIES_TO_WATCH_ID +
                        ", " + CITIES_TO_WATCH_CITY_ID +
                        ", " + CITIES_TO_WATCH_NAME +
                        ", " + CITIES_TO_WATCH_LONGITUDE +
                        ", " + CITIES_TO_WATCH_LATITUDE +
                        ", " + CITIES_TO_WATCH_CELLS_MAX_POWER +
                        ", " + CITIES_TO_WATCH_CELLS_AREA +
                        ", " + CITIES_TO_WATCH_CELLS_EFFICIENCY +
                        ", " + CITIES_TO_WATCH_DIFFUSE_EFFICIENCY +
                        ", " + CITIES_TO_WATCH_INVERTER_POWER_LIMIT +
                        ", " + CITIES_TO_WATCH_INVERTER_EFFICIENCY +
                        ", " + CITIES_TO_WATCH_AZIMUTH_ANGLE +
                        ", " + CITIES_TO_WATCH_TILT_ANGLE +
                        ", " + CITIES_TO_WATCH_SHADING_ELEVATION +
                        ", " + CITIES_TO_WATCH_SHADING_OPACITY +
                        ", " + CITIES_TO_WATCH_COLUMN_RANK +
                        " FROM " + TABLE_CITIES_TO_WATCH +
                        " WHERE " + CITIES_TO_WATCH_CITY_ID + " = ?", arguments);

        CityToWatch cityToWatch = new CityToWatch();

        if (cursor != null && cursor.moveToFirst()) {
            cityToWatch.setId(Integer.parseInt(cursor.getString(0)));
            cityToWatch.setCityId(Integer.parseInt(cursor.getString(1)));
            cityToWatch.setCityName(cursor.getString(2));
            cityToWatch.setLongitude(Float.parseFloat(cursor.getString(3)));
            cityToWatch.setLatitude(Float.parseFloat(cursor.getString(4)));
            cityToWatch.setCellsMaxPower(Float.parseFloat(cursor.getString(5)));
            cityToWatch.setCellsArea(Float.parseFloat(cursor.getString(6)));
            cityToWatch.setCellsEfficiency(Float.parseFloat(cursor.getString(7)));
            cityToWatch.setDiffuseEfficiency(Float.parseFloat(cursor.getString(8)));
            cityToWatch.setInverterPowerLimit(Float.parseFloat(cursor.getString(9)));
            cityToWatch.setInverterEfficiency(Float.parseFloat(cursor.getString(10)));
            cityToWatch.setAzimuthAngle(Float.parseFloat(cursor.getString(11)));
            cityToWatch.setTiltAngle(Float.parseFloat(cursor.getString(12)));
            cityToWatch.setShadingElevation(cursor.getString(13));
            cityToWatch.setShadingOpacity(cursor.getString(14));
            cityToWatch.setRank(Integer.parseInt(cursor.getString(15)));

            cursor.close();
        }
        database.close();
        return cityToWatch;

    }


    public synchronized List<CityToWatch> getAllCitiesToWatch() {
        List<CityToWatch> cityToWatchList = new ArrayList<>();

        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.rawQuery(
                "SELECT " + CITIES_TO_WATCH_ID +
                        ", " + CITIES_TO_WATCH_CITY_ID +
                        ", " + CITIES_TO_WATCH_NAME +
                        ", " + CITIES_TO_WATCH_LONGITUDE +
                        ", " + CITIES_TO_WATCH_LATITUDE +
                        ", " + CITIES_TO_WATCH_CELLS_MAX_POWER +
                        ", " + CITIES_TO_WATCH_CELLS_AREA +
                        ", " + CITIES_TO_WATCH_CELLS_EFFICIENCY +
                        ", " + CITIES_TO_WATCH_DIFFUSE_EFFICIENCY +
                        ", " + CITIES_TO_WATCH_INVERTER_POWER_LIMIT +
                        ", " + CITIES_TO_WATCH_INVERTER_EFFICIENCY +
                        ", " + CITIES_TO_WATCH_AZIMUTH_ANGLE +
                        ", " + CITIES_TO_WATCH_TILT_ANGLE +
                        ", " + CITIES_TO_WATCH_SHADING_ELEVATION +
                        ", " + CITIES_TO_WATCH_SHADING_OPACITY +
                        ", " + CITIES_TO_WATCH_COLUMN_RANK +
                        " FROM " + TABLE_CITIES_TO_WATCH
                , new String[]{});

        CityToWatch cityToWatch;

        if (cursor.moveToFirst()) {
            do {
                cityToWatch = new CityToWatch();
                cityToWatch.setId(Integer.parseInt(cursor.getString(0)));
                cityToWatch.setCityId(Integer.parseInt(cursor.getString(1)));
                cityToWatch.setCityName(cursor.getString(2));
                cityToWatch.setLongitude(Float.parseFloat(cursor.getString(3)));
                cityToWatch.setLatitude(Float.parseFloat(cursor.getString(4)));
                cityToWatch.setCellsMaxPower(Float.parseFloat(cursor.getString(5)));
                cityToWatch.setCellsArea(Float.parseFloat(cursor.getString(6)));
                cityToWatch.setCellsEfficiency(Float.parseFloat(cursor.getString(7)));
                cityToWatch.setDiffuseEfficiency(Float.parseFloat(cursor.getString(8)));
                cityToWatch.setInverterPowerLimit(Float.parseFloat(cursor.getString(9)));
                cityToWatch.setInverterEfficiency(Float.parseFloat(cursor.getString(10)));
                cityToWatch.setAzimuthAngle(Float.parseFloat(cursor.getString(11)));
                cityToWatch.setTiltAngle(Float.parseFloat(cursor.getString(12)));
                cityToWatch.setShadingElevation(cursor.getString(13));
                cityToWatch.setShadingOpacity(cursor.getString(14));
                cityToWatch.setRank(Integer.parseInt(cursor.getString(15)));

                cityToWatchList.add(cityToWatch);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        return cityToWatchList;
    }

    public synchronized void updateCityToWatch(CityToWatch cityToWatch) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CITIES_TO_WATCH_CITY_ID, cityToWatch.getCityId());
        values.put(CITIES_TO_WATCH_COLUMN_RANK, cityToWatch.getRank());
        values.put(CITIES_TO_WATCH_NAME,cityToWatch.getCityName());
        values.put(CITIES_TO_WATCH_LATITUDE,cityToWatch.getLatitude());
        values.put(CITIES_TO_WATCH_LONGITUDE,cityToWatch.getLongitude());
        values.put(CITIES_TO_WATCH_CELLS_MAX_POWER,cityToWatch.getCellsMaxPower());
        values.put(CITIES_TO_WATCH_CELLS_AREA,cityToWatch.getCellsArea());
        values.put(CITIES_TO_WATCH_CELLS_EFFICIENCY,cityToWatch.getCellsEfficiency());
        values.put(CITIES_TO_WATCH_DIFFUSE_EFFICIENCY,cityToWatch.getDiffuseEfficiency());
        values.put(CITIES_TO_WATCH_INVERTER_POWER_LIMIT,cityToWatch.getInverterPowerLimit());
        values.put(CITIES_TO_WATCH_INVERTER_EFFICIENCY,cityToWatch.getInverterEfficiency());
        values.put(CITIES_TO_WATCH_AZIMUTH_ANGLE,cityToWatch.getAzimuthAngle());
        values.put(CITIES_TO_WATCH_TILT_ANGLE,cityToWatch.getTiltAngle());
        values.put(CITIES_TO_WATCH_SHADING_ELEVATION,cityToWatch.getShadingElevationString());
        values.put(CITIES_TO_WATCH_SHADING_OPACITY,cityToWatch.getShadingOpacityString());

        database.update(TABLE_CITIES_TO_WATCH, values, CITIES_TO_WATCH_ID + " = ?",
                new String[]{String.valueOf(cityToWatch.getId())});
        database.close();
    }

    public synchronized void deleteCityToWatch(CityToWatch cityToWatch) {

        //First delete all weather data for city which is deleted
        deleteGeneralDataByCityId(cityToWatch.getCityId());
        deleteForecastsByCityId(cityToWatch.getCityId());
        deleteWeekForecastsByCityId(cityToWatch.getCityId());

        //Now remove city from CITIES_TO_WATCH
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_CITIES_TO_WATCH, CITIES_TO_WATCH_ID + " = ?",
                new String[]{Integer.toString(cityToWatch.getId())});
        database.close();
    }

    public synchronized int getWatchedCitiesCount() {
        SQLiteDatabase database = this.getWritableDatabase();
        long count = DatabaseUtils.queryNumEntries(database, TABLE_CITIES_TO_WATCH);
        database.close();
        return (int) count;
    }

    public int getMaxRank() {
        List<CityToWatch> cities = getAllCitiesToWatch();
        int maxRank = 0;
        for (CityToWatch ctw : cities) {
            if (ctw.getRank() > maxRank) maxRank = ctw.getRank();
        }
        return maxRank;
    }


    /**
     * Methods for TABLE_FORECAST
     */
    public synchronized void addForecasts(List<HourlyForecast> hourlyForecasts) {
        SQLiteDatabase database = this.getWritableDatabase();
        for (HourlyForecast hourlyForecast: hourlyForecasts) {
            ContentValues values = new ContentValues();
            values.put(FORECAST_CITY_ID, hourlyForecast.getCity_id());
            values.put(FORECAST_COLUMN_TIME_MEASUREMENT, hourlyForecast.getTimestamp());
            values.put(FORECAST_COLUMN_FORECAST_FOR, hourlyForecast.getForecastTime());
            values.put(FORECAST_COLUMN_WEATHER_ID, hourlyForecast.getWeatherID());
            values.put(FORECAST_COLUMN_DIRECT_RADIATION_NORMAL, hourlyForecast.getDirectRadiationNormal());
            values.put(FORECAST_COLUMN_DIFFUSE_RADIATION, hourlyForecast.getDiffuseRadiation());
            values.put(FORECAST_COLUMN_POWER, hourlyForecast.getPower());
            database.insert(TABLE_HOURLY_FORECAST, null, values);
        }
        database.close();
    }

    public synchronized void deleteForecastsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_HOURLY_FORECAST, FORECAST_CITY_ID + " = ?",
                new String[]{Integer.toString(cityId)});
        database.close();
    }


    public synchronized List<HourlyForecast> getForecastsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.query(TABLE_HOURLY_FORECAST,
                new String[]{FORECAST_ID,
                        FORECAST_CITY_ID,
                        FORECAST_COLUMN_TIME_MEASUREMENT,
                        FORECAST_COLUMN_FORECAST_FOR,
                        FORECAST_COLUMN_WEATHER_ID,
                        FORECAST_COLUMN_DIRECT_RADIATION_NORMAL,
                        FORECAST_COLUMN_DIFFUSE_RADIATION,
                        FORECAST_COLUMN_POWER}
                , FORECAST_CITY_ID + "=?",
                new String[]{String.valueOf(cityId)}, null, null, null, null);

        List<HourlyForecast> list = new ArrayList<>();
        HourlyForecast hourlyForecast;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                hourlyForecast = new HourlyForecast();
                hourlyForecast.setId(Integer.parseInt(cursor.getString(0)));
                hourlyForecast.setCity_id(Integer.parseInt(cursor.getString(1)));
                hourlyForecast.setTimestamp(Long.parseLong(cursor.getString(2)));
                hourlyForecast.setForecastTime(Long.parseLong(cursor.getString(3)));
                hourlyForecast.setWeatherID(Integer.parseInt(cursor.getString(4)));
                hourlyForecast.setDirectRadiationNormal(Float.parseFloat(cursor.getString(5)));
                hourlyForecast.setDiffuseRadiation(Float.parseFloat(cursor.getString(6)));
                hourlyForecast.setPower(Float.parseFloat(cursor.getString(7)));
                list.add(hourlyForecast);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }


    /**
     * Methods for TABLE_WEEKFORECAST
     */
    public synchronized void addWeekForecasts(List<WeekForecast> weekForecasts) {
        SQLiteDatabase database = this.getWritableDatabase();
        for (WeekForecast weekForecast: weekForecasts) {
            ContentValues values = new ContentValues();
            values.put(WEEKFORECAST_CITY_ID, weekForecast.getCity_id());
            values.put(WEEKFORECAST_COLUMN_TIME_MEASUREMENT, weekForecast.getTimestamp());
            values.put(WEEKFORECAST_COLUMN_FORECAST_FOR, weekForecast.getForecastTime());
            values.put(WEEKFORECAST_COLUMN_WEATHER_ID, weekForecast.getWeatherID());
            values.put(WEEKFORECAST_COLUMN_ENERGY_DAY, weekForecast.getEnergyDay());
            values.put(WEEKFORECAST_COLUMN_TIME_SUNRISE, weekForecast.getTimeSunrise());
            values.put(WEEKFORECAST_COLUMN_TIME_SUNSET, weekForecast.getTimeSunset());
            database.insert(TABLE_WEEKFORECAST, null, values);
        }
        database.close();
    }

    public synchronized void deleteWeekForecastsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_WEEKFORECAST, WEEKFORECAST_CITY_ID + " = ?",
                new String[]{Integer.toString(cityId)});
        database.close();
    }




    public synchronized List<WeekForecast> getWeekForecastsByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.query(TABLE_WEEKFORECAST,
                new String[]{WEEKFORECAST_ID,
                        WEEKFORECAST_CITY_ID,
                        WEEKFORECAST_COLUMN_TIME_MEASUREMENT,
                        WEEKFORECAST_COLUMN_FORECAST_FOR,
                        WEEKFORECAST_COLUMN_WEATHER_ID,
                        WEEKFORECAST_COLUMN_ENERGY_DAY,
                        WEEKFORECAST_COLUMN_TIME_SUNRISE,
                        WEEKFORECAST_COLUMN_TIME_SUNSET}
                , WEEKFORECAST_CITY_ID + "=?",
                new String[]{String.valueOf(cityId)}, null, null, null, null);

        List<WeekForecast> list = new ArrayList<>();
        WeekForecast weekForecast;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                weekForecast = new WeekForecast();
                weekForecast.setId(Integer.parseInt(cursor.getString(0)));
                weekForecast.setCity_id(Integer.parseInt(cursor.getString(1)));
                weekForecast.setTimestamp(Long.parseLong(cursor.getString(2)));
                weekForecast.setForecastTime(Long.parseLong(cursor.getString(3)));
                weekForecast.setWeatherID(Integer.parseInt(cursor.getString(4)));
                weekForecast.setEnergyDay(Float.parseFloat(cursor.getString(5)));
                weekForecast.setTimeSunrise(Long.parseLong(cursor.getString(6)));
                weekForecast.setTimeSunset(Long.parseLong(cursor.getString(7)));
                list.add(weekForecast);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }

      /**
     * Methods for TABLE_GENERAL_DATA
     */
    public synchronized void addGeneralData(GeneralData generalData) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CITY_ID, generalData.getCity_id());
        values.put(COLUMN_TIME_MEASUREMENT, generalData.getTimestamp());
        values.put(COLUMN_TIME_SUNRISE, generalData.getTimeSunrise());
        values.put(COLUMN_TIME_SUNSET, generalData.getTimeSunset());
        values.put(COLUMN_TIMEZONE_SECONDS, generalData.getTimeZoneSeconds());

        database.insert(TABLE_GENERAL_DATA, null, values);
        database.close();
    }



    public synchronized GeneralData getGeneralDataByCityId(int cityId) {
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(TABLE_GENERAL_DATA,
                new String[]{COLUMN_ID,
                        COLUMN_CITY_ID,
                        COLUMN_TIME_MEASUREMENT,
                        COLUMN_TIME_SUNRISE,
                        COLUMN_TIME_SUNSET,
                        COLUMN_TIMEZONE_SECONDS},
                COLUMN_CITY_ID + " = ?",
                new String[]{String.valueOf(cityId)}, null, null, null, null);

        GeneralData generalData = new GeneralData();

        if (cursor != null && cursor.moveToFirst()) {
            generalData.setId(Integer.parseInt(cursor.getString(0)));
            generalData.setCity_id(Integer.parseInt(cursor.getString(1)));
            generalData.setTimestamp(Long.parseLong(cursor.getString(2)));
            generalData.setTimeSunrise(Long.parseLong(cursor.getString(3)));
            generalData.setTimeSunset(Long.parseLong(cursor.getString(4)));
            generalData.setTimeZoneSeconds(Integer.parseInt(cursor.getString(5)));
            cursor.close();
        }

        return generalData;
    }

    public synchronized void updateGeneralData(GeneralData generalData) {
        SQLiteDatabase database = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CITY_ID, generalData.getCity_id());
        values.put(COLUMN_TIME_MEASUREMENT, generalData.getTimestamp());
        values.put(COLUMN_TIME_SUNRISE, generalData.getTimeSunrise());
        values.put(COLUMN_TIME_SUNSET, generalData.getTimeSunset());
        values.put(COLUMN_TIMEZONE_SECONDS, generalData.getTimeZoneSeconds());

        database.update(TABLE_GENERAL_DATA, values, COLUMN_CITY_ID + " = ?",
                new String[]{String.valueOf(generalData.getCity_id())});
    }

    public synchronized void deleteGeneralDataByCityId(int cityId) {
        SQLiteDatabase database = this.getWritableDatabase();
        database.delete(TABLE_GENERAL_DATA, COLUMN_CITY_ID + " = ?",
                new String[]{Integer.toString(cityId)});
        database.close();
    }

}
