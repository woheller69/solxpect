package org.woheller69.weather.weather_api.open_meteo;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.SolarPowerPlant;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.weather_api.IApiToDatabaseConversion;
import org.woheller69.weather.weather_api.IDataExtractor;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a concrete implementation for extracting weather data that was retrieved by
 * Open-Meteo.
 */
public class OMDataExtractor implements IDataExtractor {

    private Context context;
    public OMDataExtractor(Context context) {
        this.context = context;
    }


    @Override
    public List<WeekForecast> extractWeekForecast(String data) {
        try {

            List<WeekForecast> weekforecasts = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray timeArray = jsonData.getJSONArray("time");
            JSONArray weathercodeArray = jsonData.has("weathercode") ? jsonData.getJSONArray("weathercode") : null;
            JSONArray sunriseArray = jsonData.has("sunrise") ? jsonData.getJSONArray("sunrise") : null;
            JSONArray sunsetArray = jsonData.has("sunset") ? jsonData.getJSONArray("sunset") : null;

            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            for (int i = 0; i < timeArray.length(); i++) {
                WeekForecast weekForecast = new WeekForecast();
                weekForecast.setTimestamp(System.currentTimeMillis() / 1000);
                if (timeArray!=null && !timeArray.isNull(i)) weekForecast.setForecastTime((timeArray.getLong(i)+12*3600)*1000L);  //shift to midday
                if (weathercodeArray!=null && !weathercodeArray.isNull(i)) weekForecast.setWeatherID(conversion.convertWeatherCategory(weathercodeArray.getString(i)));
                if (sunriseArray!=null && !sunriseArray.isNull(i)) weekForecast.setTimeSunrise(sunriseArray.getLong(i));
                if (sunsetArray!=null && !sunsetArray.isNull(i)) weekForecast.setTimeSunset(sunsetArray.getLong(i));
                weekforecasts.add(weekForecast);
            }
            return weekforecasts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @see IDataExtractor#extractHourlyForecast(String,int)
     */
    @Override
    public List<HourlyForecast> extractHourlyForecast(String data, int cityID) {
        try {

            List<HourlyForecast> hourlyForecasts = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray timeArray = jsonData.getJSONArray("time");
            JSONArray weathercodeArray = jsonData.has("weathercode") ? jsonData.getJSONArray("weathercode") : null;
            JSONArray directRadiationArray = jsonData.has("direct_normal_irradiance") ? jsonData.getJSONArray("direct_normal_irradiance") : null;
            JSONArray diffuseRadiationArray = jsonData.has("diffuse_radiation") ? jsonData.getJSONArray("diffuse_radiation") : null;

            //TODO get Data for power plant from city to Watch

            SQLiteHelper dbhelper = SQLiteHelper.getInstance(context);
            CityToWatch city = dbhelper.getCityToWatch(cityID);
            SolarPowerPlant spp = new SolarPowerPlant(city.getLatitude(), city.getLongitude(), city.getCellsMaxPower(), city.getCellsArea(), city.getCellsEfficiency(),city.getDiffuseEfficiency(), city.getInverterPowerLimit(), city.getInverterEfficiency(), city.getAzimuthAngle(), city.getTiltAngle());


            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            for (int i = 0; i < timeArray.length(); i++) {
                HourlyForecast hourlyForecast = new HourlyForecast();
                hourlyForecast.setTimestamp(System.currentTimeMillis() / 1000);
                if (timeArray!=null && !timeArray.isNull(i)) hourlyForecast.setForecastTime(timeArray.getLong(i)*1000L);
                if (weathercodeArray!=null && !weathercodeArray.isNull(i)) hourlyForecast.setWeatherID(conversion.convertWeatherCategory(weathercodeArray.getString(i)));
                if (directRadiationArray!=null && !directRadiationArray.isNull(i)) hourlyForecast.setDirectRadiationNormal((float) directRadiationArray.getDouble(i));
                if (diffuseRadiationArray!=null && !diffuseRadiationArray.isNull(i)) hourlyForecast.setDiffuseRadiation((float) diffuseRadiationArray.getDouble(i));
                hourlyForecast.setPower(spp.getPower(hourlyForecast.getDirectRadiationNormal(),hourlyForecast.getDiffuseRadiation(), timeArray.getLong(i)-1800));  //use solar position 1/2h earlier for calculation of average power in preceding hour
                hourlyForecasts.add(hourlyForecast);
            }
            return hourlyForecasts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


}
