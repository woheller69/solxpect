package org.woheller69.weather.ui.RecycleList;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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
    private float[][] forecastData;

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
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        courseDayList = new ArrayList<>();

            for (HourlyForecast f : hourlyForecasts) {
                if (sp.getBoolean("pref_debug",false)) {
                    courseDayList.add(f);
                } else {
                    if (f.getForecastTime() >= System.currentTimeMillis()) {
                        courseDayList.add(f);
                    }
                }
            }
            notifyDataSetChanged();
    }

    // function for week forecast list
    public void updateWeekForecastData(List<WeekForecast> forecasts) {
        if (forecasts.isEmpty()) return;

        int cityId = forecasts.get(0).getCity_id();

        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context.getApplicationContext());
        int zonemilliseconds = dbHelper.getGeneralDataByCityId(cityId).getTimeZoneSeconds() * 1000;

        //temp max 0, temp min 1, humidity 2, pressure 3, precipitation 4, wind 5, wind direction 6, uv_index 7, forecast time 8, weather ID 9, number of FCs for day 10

        forecastData = new float[forecasts.size()][11];

        for (int i=0;i<forecasts.size();i++){
            forecastData[i][0]=0;
            forecastData[i][1]=0;
            forecastData[i][2]=0;
            forecastData[i][3]=0;
            forecastData[i][4]=forecasts.get(i).getEnergyDay();
            forecastData[i][5]=0;
            forecastData[i][6]=0;
            forecastData[i][7]=0;
            forecastData[i][8]=forecasts.get(i).getForecastTime()+zonemilliseconds;
            forecastData[i][9]=forecasts.get(i).getWeatherID();
            forecastData[i][10]=1;
        }

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


            final WeekWeatherAdapter adapter = new WeekWeatherAdapter(context, forecastData, generalDataList.getCity_id());
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

                                    highlightSelected(view);
                                }

                            }
                        }

                        private void highlightSelected(View view) {
                            for (int j=0;j<courseDayList.size();j++){  //reset all items
                                if (holder.recyclerView.getLayoutManager().getChildAt(j)!=null){
                                    holder.recyclerView.getLayoutManager().getChildAt(j).setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_transparent,null));
                                }
                            }
                            view.setBackground(ResourcesCompat.getDrawable(context.getResources(),R.drawable.rounded_highlight,null)); //highlight selected item
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

            SQLiteHelper database = SQLiteHelper.getInstance(context.getApplicationContext());
            List<WeekForecast> weekforecasts = database.getWeekForecastsByCityId(generalDataList.getCity_id());

            if (weekforecasts.isEmpty()) {
                return;
            }

            float energyMax=0;

            BarSet energyDataset = new BarSet();

            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT"));
            int zonemilliseconds = generalDataList.getTimeZoneSeconds()*1000;

            for (int i=0 ; i< weekforecasts.size();i++) {
                c.setTimeInMillis(weekforecasts.get(i).getForecastTime()+zonemilliseconds);
                int day = c.get(Calendar.DAY_OF_WEEK);
                float energyDay=weekforecasts.get(i).getEnergyDay();

                String dayString = context.getResources().getString(StringFormatUtils.getDayShort(day));
                if (weekforecasts.size()>8) dayString=dayString.substring(0,1);  //use first character only if more than 8 days to avoid overlapping text

                energyDataset.addBar(dayString, energyDay);
                if (energyDay>energyMax) energyMax=energyDay;
            }

            //Calculate step size. Target:  4 <= steps <= 7, but step size must integer >= 1
            int stepSize = 1;
            int numSteps;

             do {
                 numSteps = (int) (energyMax / stepSize);
                 if (numSteps > 10) stepSize *=10;
                 else if (numSteps > 7) stepSize *=2;
                 else if (numSteps < 4) stepSize /=2;
             } while (numSteps > 7 || numSteps < 4 && stepSize>0);

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