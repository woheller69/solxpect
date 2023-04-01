package org.woheller69.weather.weather_api.open_meteo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.woheller69.weather.SolarPowerPlant;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
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
    public CurrentWeatherData extractCurrentWeather(String data) {
        try {
            JSONObject jsonData = new JSONObject(data);
            CurrentWeatherData weatherData = new CurrentWeatherData();
            weatherData.setTimestamp(System.currentTimeMillis() / 1000);
            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            if (jsonData.has("weathercode")) weatherData.setWeatherID(conversion.convertWeatherCategory(jsonData.getString("weathercode")));
            if (jsonData.has("temperature")) weatherData.setTemperatureCurrent((float) jsonData.getDouble("temperature"));
            if (jsonData.has("windspeed")) weatherData.setWindSpeed((float) jsonData.getDouble("windspeed"));
            if (jsonData.has("winddirection")) weatherData.setWindDirection((float) jsonData.getDouble("winddirection"));
            weatherData.setTimeSunrise(0L);
            weatherData.setTimeSunset(0L);
            weatherData.setHumidity(0);
            weatherData.setPressure(0);
            weatherData.setCloudiness(0);

            return weatherData;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public List<WeekForecast> extractWeekForecast(String data) {
        try {
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);

            List<WeekForecast> weekforecasts = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray timeArray = jsonData.getJSONArray("time");
            JSONArray weathercodeArray = jsonData.has("weathercode") ? jsonData.getJSONArray("weathercode") : null;
            JSONArray tempMaxArray = jsonData.has("temperature_2m_max") ? jsonData.getJSONArray("temperature_2m_max") : null;
            JSONArray tempMinArray = jsonData.has("temperature_2m_min") ? jsonData.getJSONArray("temperature_2m_min") : null;
            JSONArray sunriseArray = jsonData.has("sunrise") ? jsonData.getJSONArray("sunrise") : null;
            JSONArray sunsetArray = jsonData.has("sunset") ? jsonData.getJSONArray("sunset") : null;
            JSONArray uvIndexArray = jsonData.has("uv_index_max") ? jsonData.getJSONArray("uv_index_max") : null;
            JSONArray precipitationArray = jsonData.has("precipitation_sum") ? jsonData.getJSONArray("precipitation_sum") : null;
            JSONArray windSpeedArray = jsonData.has("windspeed_10m_max") ? jsonData.getJSONArray("windspeed_10m_max") : null;
            JSONArray snowfallArray = jsonData.has("snowfall_sum") ? jsonData.getJSONArray("snowfall_sum") : null;
            JSONArray showersArray = jsonData.has("showers_sum") ? jsonData.getJSONArray("showers_sum") : null;
            JSONArray rainArray = jsonData.has("rain_sum") ? jsonData.getJSONArray("rain_sum") : null;

            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            for (int i = 0; i < timeArray.length(); i++) {
                WeekForecast weekForecast = new WeekForecast();
                weekForecast.setTimestamp(System.currentTimeMillis() / 1000);
                if (!timeArray.isNull(i)) weekForecast.setForecastTime((timeArray.getLong(i)+12*3600)*1000L);  //shift to midday
                if (!weathercodeArray.isNull(i)) weekForecast.setWeatherID(conversion.convertWeatherCategory(weathercodeArray.getString(i)));
                if (!tempMaxArray.isNull(i)) weekForecast.setMaxTemperature((float) tempMaxArray.getDouble(i));
                if (!tempMinArray.isNull(i)) weekForecast.setMinTemperature((float) tempMinArray.getDouble(i));
                if (!sunriseArray.isNull(i)) weekForecast.setTimeSunrise(sunriseArray.getLong(i));
                if (!sunsetArray.isNull(i)) weekForecast.setTimeSunset(sunsetArray.getLong(i));
                if (!uvIndexArray.isNull(i)) {
                    weekForecast.setUv_index((float) uvIndexArray.getDouble(i));
                } else weekForecast.setUv_index(-1);
                if (!windSpeedArray.isNull(i)) weekForecast.setWind_speed((float) windSpeedArray.getDouble(i));
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
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);

            List<HourlyForecast> hourlyForecasts = new ArrayList<>();
            JSONObject jsonData = new JSONObject(data);
            JSONArray timeArray = jsonData.getJSONArray("time");
            JSONArray weathercodeArray = jsonData.has("weathercode") ? jsonData.getJSONArray("weathercode") : null;
            JSONArray directRadiationArray = jsonData.has("direct_normal_irradiance") ? jsonData.getJSONArray("direct_normal_irradiance") : null;
            JSONArray diffuseRadiationArray = jsonData.has("diffuse_radiation") ? jsonData.getJSONArray("diffuse_radiation") : null;

            //TODO get Data for power plant from city to Watch

            SQLiteHelper dbhelper = SQLiteHelper.getInstance(context);
            CityToWatch city = dbhelper.getCityToWatch(cityID);
            SolarPowerPlant spp = new SolarPowerPlant(city.getLatitude(), city.getLongitude(), city.getCellsMaxPower(), city.getCellsArea(), city.getCellsEfficiency(),city.getDiffuseEfficiency(), city.getConverterPowerLimit(), city.getConverterEfficiency(), city.getAzimuthAngle(), city.getElevationAngle());


            IApiToDatabaseConversion conversion = new OMToDatabaseConversion();
            for (int i = 0; i < timeArray.length(); i++) {
                HourlyForecast hourlyForecast = new HourlyForecast();
                hourlyForecast.setTimestamp(System.currentTimeMillis() / 1000);
                if (timeArray!=null) hourlyForecast.setForecastTime(timeArray.getLong(i)*1000L);
                if (weathercodeArray!=null) hourlyForecast.setWeatherID(conversion.convertWeatherCategory(weathercodeArray.getString(i)));
                if (directRadiationArray!=null) hourlyForecast.setDirectRadiationNormal((float) directRadiationArray.getDouble(i));
                if (diffuseRadiationArray!=null) hourlyForecast.setDiffuseRadiation((float) diffuseRadiationArray.getDouble(i));
                hourlyForecast.setPower(spp.getPower(hourlyForecast.getDirectRadiationNormal(),hourlyForecast.getDiffuseRadiation(), timeArray.getLong(i)-1800));  //use solar position 1/2h earlier for calculation of average power in preceding hour
                hourlyForecasts.add(hourlyForecast);
            }
            return hourlyForecasts;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @see IDataExtractor#extractRain60min(String, String, String, String, String)
     */
    @Override
    public String extractRain60min(String data0,String data1, String data2, String data3, String data4) {
        try {

            String rain = "";
            JSONObject jsonData0 = new JSONObject(data0);
            JSONObject jsonData1 = new JSONObject(data1);
            JSONObject jsonData2 = new JSONObject(data2);
            JSONObject jsonData3 = new JSONObject(data3);
            JSONObject jsonData4 = new JSONObject(data4);
            double rain5min=jsonData0.getDouble("precipitation")+jsonData1.getDouble("precipitation")+jsonData2.getDouble("precipitation")+jsonData3.getDouble("precipitation")+jsonData4.getDouble("precipitation");
            if (rain5min==0){
                rain ="\u25a1";
            } else if (rain5min<2.5){  // very light rain equals <0.5mm/h (2.5 = 5 x 0.5)
                rain ="\u25a4";
            }else if (rain5min<12.5){  //light rain equals <2.5mm/h (12.5 = 5 x 2.5)
                rain ="\u25a6";
            } else{
                rain ="\u25a0";
            }

            return rain;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * @param data The data that contains the information to retrieve the ID of the city.
     *             If data for a single city were requested, the response string can be
     *             passed as an argument.
     *             If data for multiple cities were requested, make sure to pass only one item
     *             of the response list at a time!
     * @return Returns the ID of the city or Integer#MIN_VALUE in case the data is not well-formed
     * and the information could not be extracted.
     */


}
