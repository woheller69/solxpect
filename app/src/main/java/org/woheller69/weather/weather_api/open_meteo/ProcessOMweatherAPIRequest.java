package org.woheller69.weather.weather_api.open_meteo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

import androidx.preference.PreferenceManager;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.R;
import org.woheller69.weather.activities.NavigationActivity;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.GeneralData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.weather_api.IDataExtractor;
import org.woheller69.weather.weather_api.IProcessHttpRequest;

import org.woheller69.weather.weather_api.IApiToDatabaseConversion.WeatherCategories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class processes the HTTP requests that are made to the Open-Meteo API requesting the
 * current weather for all stored cities.
 */
public class ProcessOMweatherAPIRequest implements IProcessHttpRequest {

    /**
     * Constants
     */
    private final String DEBUG_TAG = "process_forecast";

    /**
     * Member variables
     */
    private Context context;
    private SQLiteHelper dbHelper;

    /**
     * Constructor.
     *
     * @param context The context of the HTTP request.
     */
    public ProcessOMweatherAPIRequest(Context context) {
        this.context = context;
        this.dbHelper = SQLiteHelper.getInstance(context);
    }

    /**
     * Converts the response to JSON and updates the database. Note that for this method no
     * UI-related operations are performed.
     *
     * @param response The response of the HTTP request.
     */
    @Override
    public void processSuccessScenario(String response, int cityId) {

        IDataExtractor extractor = new OMDataExtractor(context);


        ArrayList<Integer> CityIDList = new ArrayList<Integer>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean("pref_summarize",false)){
            List<CityToWatch> citiesToWatch = dbHelper.getAllCitiesToWatch();
            CityToWatch requestedCity = dbHelper.getCityToWatch(cityId);
            for (int i = 0; i < citiesToWatch.size(); i++) {
                CityToWatch city = citiesToWatch.get(i);
                if (city.getCityId()!=requestedCity.getCityId() && city.getLatitude() == requestedCity.getLatitude() && city.getLongitude() == requestedCity.getLongitude()) {
                    CityIDList.add(city.getCityId());
                }
            }
        }

        CityIDList.add(cityId);  //add requested city at end of list. Call Viewupdater if last (requested) city is updated

        for (int c=0; c<CityIDList.size();c++) {
            cityId = CityIDList.get(c);

            try {
                JSONObject json = new JSONObject(response);

                //Extract daily weather
                dbHelper.deleteWeekForecastsByCityId(cityId);
                List<WeekForecast> weekforecasts = new ArrayList<>();
                weekforecasts = extractor.extractWeekForecast(json.getString("daily"));

                if (weekforecasts != null && !weekforecasts.isEmpty()) {
                    for (WeekForecast weekForecast : weekforecasts) {
                        weekForecast.setCity_id(cityId);
                    }
                } else {
                    final String ERROR_MSG = context.getResources().getString(R.string.error_convert_to_json);
                    if (NavigationActivity.isVisible)
                        Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
                    return;
                }

                //Extract current weather
                GeneralData generalData = new GeneralData();
                generalData.setTimestamp(System.currentTimeMillis() / 1000);
                generalData.setCity_id(cityId);
                generalData.setTimeSunrise(weekforecasts.get(0).getTimeSunrise());
                generalData.setTimeSunset(weekforecasts.get(0).getTimeSunset());
                generalData.setTimeZoneSeconds(json.getInt("utc_offset_seconds"));
                GeneralData current = dbHelper.getGeneralDataByCityId(cityId);
                if (current != null && current.getCity_id() == cityId) {
                    dbHelper.updateGeneralData(generalData);
                } else {
                    dbHelper.addGeneralData(generalData);
                }

                //Extract hourly weather
                dbHelper.deleteForecastsByCityId(cityId);
                List<HourlyForecast> hourlyforecasts = new ArrayList<>();
                hourlyforecasts = extractor.extractHourlyForecast(json.getString("hourly"), cityId);

                if (hourlyforecasts != null && !hourlyforecasts.isEmpty()) {
                    for (HourlyForecast hourlyForecast : hourlyforecasts) {
                        hourlyForecast.setCity_id(cityId);
                    }
                } else {
                    final String ERROR_MSG = context.getResources().getString(R.string.error_convert_to_json);
                    if (NavigationActivity.isVisible)
                        Toast.makeText(context, ERROR_MSG, Toast.LENGTH_LONG).show();
                    return;
                }
                dbHelper.addForecasts(hourlyforecasts);

                weekforecasts = reanalyzeWeekIDs(weekforecasts, hourlyforecasts);

                dbHelper.addWeekForecasts(weekforecasts);

                if (c == CityIDList.size()-1) ViewUpdater.updateGeneralDataData(generalData); // Call Viewupdater if last (requested) city is updated
                if (c == CityIDList.size()-1) ViewUpdater.updateWeekForecasts(weekforecasts);
                if (c == CityIDList.size()-1) ViewUpdater.updateForecasts(hourlyforecasts);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reanalyze weekforecasts and improve weather codes which are not representative for the day
     * @param weekforecasts
     * @param hourlyforecasts
     * @return
     */
    private List<WeekForecast> reanalyzeWeekIDs(List<WeekForecast> weekforecasts, List<HourlyForecast> hourlyforecasts) {

        Map<Integer,Integer> mappingTable = new HashMap<>();
        mappingTable.put(WeatherCategories.OVERCAST_CLOUDS.getNumVal(),WeatherCategories.SCATTERED_CLOUDS.getNumVal());
        mappingTable.put(WeatherCategories.MIST.getNumVal(),WeatherCategories.SCATTERED_CLOUDS.getNumVal());
        mappingTable.put(WeatherCategories.DRIZZLE_RAIN.getNumVal(),WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.FREEZING_DRIZZLE_RAIN.getNumVal(),WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.LIGHT_RAIN.getNumVal(),WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.LIGHT_FREEZING_RAIN.getNumVal(),WeatherCategories.LIGHT_SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.MODERATE_RAIN.getNumVal(),WeatherCategories.SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.HEAVY_RAIN.getNumVal(),WeatherCategories.SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.FREEZING_RAIN.getNumVal(),WeatherCategories.SHOWER_RAIN.getNumVal());
        mappingTable.put(WeatherCategories.LIGHT_SNOW.getNumVal(),WeatherCategories.LIGHT_SHOWER_SNOW.getNumVal());
        mappingTable.put(WeatherCategories.MODERATE_SNOW.getNumVal(),WeatherCategories.SHOWER_SNOW.getNumVal());
        mappingTable.put(WeatherCategories.HEAVY_SNOW.getNumVal(),WeatherCategories.SHOWER_SNOW.getNumVal());

        Map<Integer,Integer> sunTable = new HashMap<>();
        sunTable.put(WeatherCategories.CLEAR_SKY.getNumVal(), 0);
        sunTable.put(WeatherCategories.FEW_CLOUDS.getNumVal(), 0);
        sunTable.put(WeatherCategories.SCATTERED_CLOUDS.getNumVal(), 0);

        for (WeekForecast weekForecast: weekforecasts){
            Integer ID = weekForecast.getWeatherID();
            if (mappingTable.containsKey(ID)){
                int totalCount = 0;
                int sunCount = 0;
                long sunrise = weekForecast.getTimeSunrise()*1000L;
                long sunset = weekForecast.getTimeSunset()*1000L;
                for (HourlyForecast hourlyForecast: hourlyforecasts){
                    if(hourlyForecast.getForecastTime() >= sunrise && hourlyForecast.getForecastTime() <= sunset){
                        totalCount++;
                        if(sunTable.containsKey(hourlyForecast.getWeatherID())) sunCount++;
                    }
                }
                if (totalCount>0 && (float)sunCount/totalCount>0.2f)  weekForecast.setWeatherID(mappingTable.get(ID));
            }
        }

        for (WeekForecast weekForecast: weekforecasts){
            float totalEnergy = 0;
            Long timeNoon = weekForecast.getForecastTime();
            for (HourlyForecast hourlyForecast: hourlyforecasts){
                if ((hourlyForecast.getForecastTime()>=timeNoon-11*3600*1000L) && (hourlyForecast.getForecastTime()< timeNoon + 13*3600*1000L)){ //values are for preceding hour!
                   totalEnergy+=hourlyForecast.getPower();
                }
            }
            weekForecast.setEnergyDay(totalEnergy/1000);
        }

        return weekforecasts;
    }

    /**
     * Shows an error that the data could not be retrieved.
     *
     * @param error The error that occurred while executing the HTTP request.
     */
    @Override
    public void processFailScenario(final VolleyError error) {
        Handler h = new Handler(this.context.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                if (NavigationActivity.isVisible) Toast.makeText(context, context.getResources().getString(R.string.error_fetch_forecast), Toast.LENGTH_LONG).show();
            }
        });
    }

}
