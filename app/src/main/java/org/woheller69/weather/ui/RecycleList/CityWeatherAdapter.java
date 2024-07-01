package org.woheller69.weather.ui.RecycleList;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.model.ChartSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.BarChartView;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.GeneralData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.ui.Help.StringFormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class CityWeatherAdapter extends RecyclerView.Adapter<CityWeatherAdapter.ViewHolder> {

    private int[] dataSetTypes;
    private List<HourlyForecast> courseDayList;
    private List<WeekForecast> weekForecastList;

    private Context context;
    private ViewGroup mParent;
    private RecyclerView mCourseOfDay;
    private RecyclerView mWeekWeather;

    private GeneralData generalDataList;

    public static final int OVERVIEW = 0;
    public static final int DETAILS = 1;
    public static final int WEEK = 2;
    public static final int DAY = 3;
    public static final int CHART = 4;
    public static final int EMPTY = 5;

    public CityWeatherAdapter(GeneralData generalDataList, int[] dataSetTypes, Context context) {
        this.generalDataList = generalDataList;
        this.dataSetTypes = dataSetTypes;
        this.context = context;

        SQLiteHelper database = SQLiteHelper.getInstance(context.getApplicationContext());

        List<HourlyForecast> hourlyForecasts = database.getForecastsByCityId(generalDataList.getCity_id());
        List<WeekForecast> weekforecasts = database.getWeekForecastsByCityId(generalDataList.getCity_id());

        updateForecastData(hourlyForecasts);
        updateWeekForecastData(weekforecasts);

    }

    // function update 3-hour or 1-hour forecast list
    public void updateForecastData(List<HourlyForecast> hourlyForecasts) {
        if (hourlyForecasts.isEmpty()) return;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        if (sp.getBoolean("pref_summarize",false)){
            int cityId = hourlyForecasts.get(0).getCity_id();
            ArrayList<Integer> CityIDList = new ArrayList<Integer>();
            SQLiteHelper dbHelper = SQLiteHelper.getInstance(context.getApplicationContext());
            hourlyForecasts = dbHelper.getForecastsByCityId(cityId); //get fresh values from database to make sure we do add new values to sum values from last update
            List<CityToWatch> citiesToWatch = dbHelper.getAllCitiesToWatch();
            CityToWatch requestedCity = dbHelper.getCityToWatch(cityId);
            for (int i = 0; i < citiesToWatch.size(); i++) {
                CityToWatch city = citiesToWatch.get(i);
                if (city.getCityId()!=requestedCity.getCityId() && city.getLatitude() == requestedCity.getLatitude() && city.getLongitude() == requestedCity.getLongitude()) {
                    CityIDList.add(city.getCityId());
                }
            }
            if (CityIDList.size()>0){
                for (int c=0; c<CityIDList.size();c++) {
                    int iteratorCityId = CityIDList.get(c);
                    List<HourlyForecast> hfc = dbHelper.getForecastsByCityId(iteratorCityId);
                    if (hfc.size()!=hourlyForecasts.size()) break; //maybe something went wrong during update or city is not yet updated
                    for (int i=0;i<hfc.size();i++){
                        hourlyForecasts.get(i).setPower(hourlyForecasts.get(i).getPower()+hfc.get(i).getPower());
                    }
                }
            }
        }

        courseDayList = new ArrayList<>();

        float energyCumulated=0;
        boolean isDebugMode = sp.getBoolean("pref_debug", false);
        int stepCounter = 0; // Counter to track the number of steps taken in the loop

        for (HourlyForecast f : hourlyForecasts) {
            float power = f.getPower();
            if (stepCounter > 0) energyCumulated += power;  //Ignore first value because power values are for preceding hour
            f.setEnergyCum(energyCumulated);

            // In debug mode show all values, otherwise only future values
            if (isDebugMode || f.getForecastTime() >= System.currentTimeMillis()) {
                courseDayList.add(f);
            }

            stepCounter++;
            //   if not in debug mode: Reset energyCumulated after every 24 hours if next step is 01:00 am because values are for previous hour
            if (!isDebugMode && stepCounter % 24 == 1) {
                energyCumulated = 0;
            }
        }
        notifyDataSetChanged();
    }

    // function for week forecast list
    public void updateWeekForecastData(List<WeekForecast> forecasts) {
        if (forecasts.isEmpty()) return;
        int cityId = forecasts.get(0).getCity_id();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (sp.getBoolean("pref_summarize",false)){
            ArrayList<Integer> CityIDList = new ArrayList<Integer>();
            SQLiteHelper dbHelper = SQLiteHelper.getInstance(context.getApplicationContext());
            forecasts = dbHelper.getWeekForecastsByCityId(cityId);  //get fresh values from database to make sure we do add new values to sum values from last update
            List<CityToWatch> citiesToWatch = dbHelper.getAllCitiesToWatch();
            CityToWatch requestedCity = dbHelper.getCityToWatch(cityId);
            for (int i = 0; i < citiesToWatch.size(); i++) {
                CityToWatch city = citiesToWatch.get(i);
                if (city.getCityId()!=requestedCity.getCityId() && city.getLatitude() == requestedCity.getLatitude() && city.getLongitude() == requestedCity.getLongitude()) {
                    CityIDList.add(city.getCityId());
                }
            }
            if (CityIDList.size()>0){
                for (int c=0; c<CityIDList.size();c++) {
                    int iteratorCityId = CityIDList.get(c);
                    List<WeekForecast> wfc = dbHelper.getWeekForecastsByCityId(iteratorCityId);
                    if (wfc.size() != forecasts.size()) break;  //maybe something went wrong during update or city is not yet updated
                    for (int i=0;i<wfc.size();i++){
                        forecasts.get(i).setEnergyDay(forecasts.get(i).getEnergyDay()+wfc.get(i).getEnergyDay());
                    }
                }
            }
        }

        weekForecastList = forecasts;


        notifyDataSetChanged();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View v) {
            super(v);
        }
    }

    public class OverViewHolder extends ViewHolder {
        TextView temperature;
        TextView updatetime;
        TextView sun;

        OverViewHolder(View v) {
            super(v);
            this.temperature = v.findViewById(R.id.card_overview_temperature);
            this.sun=v.findViewById(R.id.card_overview_sunrise_sunset);
            this.updatetime=v.findViewById(R.id.card_overview_update_time);
        }
    }

    public class DetailViewHolder extends ViewHolder {
        TextView humidity;
        TextView pressure;
        TextView windspeed;
        TextView rain60min;
        TextView rain60minLegend;
        TextView time;
        ImageView winddirection;

        DetailViewHolder(View v) {
            super(v);
            this.humidity = v.findViewById(R.id.card_details_humidity_value);
            this.pressure = v.findViewById(R.id.card_details_pressure_value);
            this.windspeed = v.findViewById(R.id.card_details_wind_speed_value);
            this.rain60min = v.findViewById(R.id.card_details_rain60min_value);
            this.rain60minLegend=v.findViewById(R.id.card_details_legend_rain60min);
            this.winddirection =v.findViewById((R.id.card_details_wind_direction_value));
            this.time=v.findViewById(R.id.card_details_title);
        }
    }

    public class WeekViewHolder extends ViewHolder {
        RecyclerView recyclerView;

        WeekViewHolder(View v) {
            super(v);
            recyclerView = v.findViewById(R.id.recycler_view_week);
            mWeekWeather=recyclerView;
        }
    }

    public class DayViewHolder extends ViewHolder {
        RecyclerView recyclerView;
        TextView recyclerViewHeader;

        DayViewHolder(View v) {
            super(v);
            recyclerView = v.findViewById(R.id.recycler_view_course_day);
            mCourseOfDay=recyclerView;
            recyclerViewHeader=v.findViewById(R.id.recycler_view_header);
        }
    }

    public class ChartViewHolder extends ViewHolder {

        TextView energyUnit;
        BarChartView barChartView;

        ChartViewHolder(View v) {
            super(v);
            this.barChartView = v.findViewById(R.id.graph_energy);
            this.energyUnit =v.findViewById(R.id.graph_energyunit);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v;
        mParent=viewGroup;
        if (viewType == OVERVIEW) {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_overview, viewGroup, false);

            return new OverViewHolder(v);

        } else if (viewType == DETAILS) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_details, viewGroup, false);
            return new DetailViewHolder(v);

        } else if (viewType == WEEK) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_week, viewGroup, false);
            return new WeekViewHolder(v);

        } else if (viewType == DAY) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_day, viewGroup, false);
            return new DayViewHolder(v);

        } else if (viewType == CHART) {

            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_chart, viewGroup, false);
            return new ChartViewHolder(v);
        } else {
            v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.card_empty, viewGroup, false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

         if (viewHolder.getItemViewType() == OVERVIEW) {
            OverViewHolder holder = (OverViewHolder) viewHolder;

            //correct for timezone differences
            int zoneseconds = generalDataList.getTimeZoneSeconds();
            long riseTime = (generalDataList.getTimeSunrise() + zoneseconds) * 1000;
            long setTime = (generalDataList.getTimeSunset() + zoneseconds) * 1000;
            if (riseTime==zoneseconds*1000 || setTime==zoneseconds*1000) holder.sun.setText("\u2600\u25b2 --:--" + " \u25bc --:--" );
            else  {
                holder.sun.setText("\u2600\u25b2 " + StringFormatUtils.formatTimeWithoutZone(context, riseTime) + " \u25bc " + StringFormatUtils.formatTimeWithoutZone(context, setTime));
            }
            long time = generalDataList.getTimestamp();
            long updateTime = ((time + zoneseconds) * 1000);

            holder.updatetime.setText("("+StringFormatUtils.formatTimeWithoutZone(context, updateTime)+")");


        } else if (viewHolder.getItemViewType() == WEEK) {

            final WeekViewHolder holder = (WeekViewHolder) viewHolder;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);


            final WeekWeatherAdapter adapter = new WeekWeatherAdapter(context, weekForecastList, generalDataList.getCity_id());
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setFocusable(false);

            if (mCourseOfDay!=null) {  //otherwise crash if courseOfDay not visible
                CourseOfDayAdapter dayadapter = (CourseOfDayAdapter) mCourseOfDay.getAdapter();
                dayadapter.setWeekRecyclerView(holder.recyclerView);        //provide CourseOfDayAdapter with reference to week recyclerview
                adapter.setCourseOfDayHeaderDate(dayadapter.getCourseOfDayHeaderDate());  //initialize WeekWeatherAdapter with current HeaderDate from CourseOfDayAdapter
            }

            holder.recyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(context, holder.recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            SQLiteHelper database = SQLiteHelper.getInstance(context.getApplicationContext());
                            List<WeekForecast> weekforecasts = database.getWeekForecastsByCityId(generalDataList.getCity_id());
                            long time = weekforecasts.get(position).getForecastTime();  //time of clicked week item
                            time=time-6*3600000;                                       //week item normally midday -> subtract 6h to get morning time

                            if (mCourseOfDay!=null){  //otherwise crash if courseOfDay not visible
                                LinearLayoutManager llm = (LinearLayoutManager) mCourseOfDay.getLayoutManager();

                                assert llm != null;
                                int num = llm.findLastVisibleItemPosition() - llm.findFirstVisibleItemPosition();  //get number of visible elements
                                int i;

                                for (i = 0; i < courseDayList.size(); i++) {
                                    if (courseDayList.get(i).getForecastTime() > time) {        //find first ForecastTime > time of clicked item
                                        Calendar HeaderTime = Calendar.getInstance();
                                        HeaderTime.setTimeZone(TimeZone.getTimeZone("GMT"));
                                        HeaderTime.setTimeInMillis(courseDayList.get(i).getLocalForecastTime(context));
                                        adapter.setCourseOfDayHeaderDate(HeaderTime.getTime());
                                        break;
                                    }
                                }

                                if (i < courseDayList.size()) {  //only if element found
                                    if (i > llm.findFirstVisibleItemPosition()) {               //if scroll right
                                        int min = Math.min(i + num, courseDayList.size()-1);      //scroll to i+num so that requested element is on the left. Max scroll to courseDayList.size()
                                        mCourseOfDay.getLayoutManager().scrollToPosition(min);
                                    } else {                                                    //if scroll left
                                        mCourseOfDay.getLayoutManager().scrollToPosition(i);
                                    }

                                }

                            }
                        }


                        public void onLongItemClick(View view, int position) {

                        }
                    })
            );

        } else if (viewHolder.getItemViewType() == DAY) {

            DayViewHolder holder = (DayViewHolder) viewHolder;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            holder.recyclerView.setLayoutManager(layoutManager);
            CourseOfDayAdapter adapter = new CourseOfDayAdapter(courseDayList, context,holder.recyclerViewHeader,holder.recyclerView);
            holder.recyclerView.setAdapter(adapter);
            holder.recyclerView.setFocusable(false);

        } else if (viewHolder.getItemViewType() == CHART) {
            ChartViewHolder holder = (ChartViewHolder) viewHolder;

            if(weekForecastList.isEmpty()) return;

            float energyMax=0;

            BarSet energyDataset = new BarSet();

            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT"));

            for (int i = 0 ; i < weekForecastList.size(); i++) {
                c.setTimeInMillis(weekForecastList.get(i).getLocalForecastTime(context));
                int day = c.get(Calendar.DAY_OF_WEEK);
                float energyDay=weekForecastList.get(i).getEnergyDay();

                String dayString = context.getResources().getString(StringFormatUtils.getDayShort(day));
                if (weekForecastList.size()>8) dayString=dayString.substring(0,1);  //use first character only if more than 8 days to avoid overlapping text

                energyDataset.addBar(dayString, energyDay);
                if (energyDay>energyMax) energyMax=energyDay;
            }

            //Calculate step size. Target:  4 <= steps <= 7, but step size must integer >= 1
            int stepSize = 1;
            int numSteps;

             do {
                 numSteps = (int) (energyMax / stepSize);
                 if (numSteps > 10) stepSize *=10;
                 else if (numSteps >= 8) stepSize *=2;
                 else if (numSteps < 4) stepSize /=2;
             } while (numSteps >= 8 || numSteps < 4 && stepSize>0);

            if (stepSize<1) stepSize=1;  //Step size must be integer, min 1

            ArrayList<ChartSet> energyData = new ArrayList<>();
            energyData.add(energyDataset);

            energyDataset.setColor(ContextCompat.getColor(context,R.color.yellow));
            energyDataset.setAlpha(0.8f);  // make energyData bars transparent

            holder.barChartView.addData(energyData);
            holder.barChartView.setBarSpacing(10);
            holder.barChartView.setStep(stepSize);
            holder.barChartView.setXAxis(false);
            holder.barChartView.setYAxis(false);
            holder.barChartView.setYLabels(AxisController.LabelPosition.OUTSIDE);
            holder.barChartView.setLabelsColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));  //transparent color, make labels invisible
            holder.barChartView.setAxisColor(ContextCompat.getColor(context,R.color.colorPrimaryDark));
            holder.barChartView.setFontSize((int) Tools.fromDpToPx(17));

            holder.barChartView.show();

            holder.energyUnit.setText(" " + context.getResources().getString(R.string.units_kWh)+" ");
        }
        //No update for error needed
    }


    @Override
    public int getItemCount() {
        return dataSetTypes.length;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSetTypes[position];
    }
}