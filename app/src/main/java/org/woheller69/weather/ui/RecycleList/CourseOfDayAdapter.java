package org.woheller69.weather.ui.RecycleList;

import android.content.Context;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.woheller69.weather.R;
import org.woheller69.weather.database.GeneralData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.ui.Help.StringFormatUtils;
import org.woheller69.weather.ui.UiResourceProvider;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

//**
// * Created by yonjuni on 02.01.17.
// * Adapter for the horizontal listView for course of the day.
// */import java.util.List;

public class CourseOfDayAdapter extends RecyclerView.Adapter<CourseOfDayAdapter.CourseOfDayViewHolder> {

    private List<HourlyForecast> courseOfDayList;
    private Context context;
    private TextView recyclerViewHeader;
    private RecyclerView recyclerView;
    private RecyclerView weekRecyclerView;
    private Date courseOfDayHeaderDate;

    CourseOfDayAdapter(List<HourlyForecast> courseOfDayList, Context context, TextView recyclerViewHeader, RecyclerView recyclerView) {
        this.context = context;
        this.courseOfDayList = courseOfDayList;
        this.recyclerViewHeader=recyclerViewHeader;
        this.recyclerView=recyclerView;
        if (courseOfDayList.size()!=0 && courseOfDayList.get(0)!=null) {
            this.courseOfDayHeaderDate = new Date(courseOfDayList.get(0).getLocalForecastTime(context));
        }else this.courseOfDayHeaderDate=new Date();  //fallback if no data available
    }

    public void setWeekRecyclerView(RecyclerView weekRecyclerView){
        this.weekRecyclerView=weekRecyclerView;
    }

    public Date getCourseOfDayHeaderDate(){
        return this.courseOfDayHeaderDate;
    }
    @Override
    public CourseOfDayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_course_of_day, parent, false);
        return new CourseOfDayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CourseOfDayViewHolder holder, int position) {
        SQLiteHelper dbHelper = SQLiteHelper.getInstance(context);
        GeneralData generalData = dbHelper.getGeneralDataByCityId(courseOfDayList.get(position).getCity_id());

        Calendar forecastTime = Calendar.getInstance();
        forecastTime.setTimeZone(TimeZone.getTimeZone("GMT"));
        forecastTime.setTimeInMillis(courseOfDayList.get(position).getLocalForecastTime(context));

        boolean isDay;
        if (generalData.getTimeSunrise()==0 || generalData.getTimeSunset()==0){
            if ((dbHelper.getCityToWatch(courseOfDayList.get(position).getCity_id()).getLatitude())>0){  //northern hemisphere
                isDay= forecastTime.get(Calendar.DAY_OF_YEAR) >= 80 && forecastTime.get(Calendar.DAY_OF_YEAR) <= 265;  //from March 21 to September 22 (incl)
            }else{ //southern hemisphere
                isDay= forecastTime.get(Calendar.DAY_OF_YEAR) < 80 || forecastTime.get(Calendar.DAY_OF_YEAR) > 265;
            }
        }else {
            Calendar sunSetTime = Calendar.getInstance();
            sunSetTime.setTimeZone(TimeZone.getTimeZone("GMT"));
            sunSetTime.setTimeInMillis(generalData.getTimeSunset() * 1000 + generalData.getTimeZoneSeconds() * 1000L);
            sunSetTime.set(Calendar.DAY_OF_YEAR, forecastTime.get(Calendar.DAY_OF_YEAR));
            sunSetTime.set(Calendar.YEAR, forecastTime.get(Calendar.YEAR));


            Calendar sunRiseTime = Calendar.getInstance();
            sunRiseTime.setTimeZone(TimeZone.getTimeZone("GMT"));
            sunRiseTime.setTimeInMillis(generalData.getTimeSunrise() * 1000 + generalData.getTimeZoneSeconds() * 1000L);
            sunRiseTime.set(Calendar.DAY_OF_YEAR, forecastTime.get(Calendar.DAY_OF_YEAR));
            sunRiseTime.set(Calendar.YEAR, forecastTime.get(Calendar.YEAR));

            isDay = forecastTime.after(sunRiseTime) && forecastTime.before(sunSetTime);
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        if (sp.getBoolean("pref_debug",false)) {
            holder.diffuseRadiation.setVisibility(View.VISIBLE);
            holder.directRadiationNormal.setVisibility(View.VISIBLE);
            holder.energyCum.setVisibility(View.VISIBLE);
        } else {
            holder.diffuseRadiation.setVisibility(View.GONE);
            holder.directRadiationNormal.setVisibility(View.GONE);
            holder.energyCum.setVisibility(View.GONE);
        }

        holder.time.setText(StringFormatUtils.formatTimeWithoutZone(context, courseOfDayList.get(position).getLocalForecastTime(context)));
        holder.directRadiationNormal.setText(StringFormatUtils.formatInt(courseOfDayList.get(position).getDirectRadiationNormal()," W/qm"));
        holder.diffuseRadiation.setText(StringFormatUtils.formatInt(courseOfDayList.get(position).getDiffuseRadiation()," W/qm"));
        holder.power.setText(StringFormatUtils.formatInt(courseOfDayList.get(position).getPower()," "+ context.getString(R.string.units_Wh)));
        float energyCumulated=0;
        for (int i=0; i<=position;i++)
            energyCumulated+=courseOfDayList.get(i).getPower();
        holder.energyCum.setText(StringFormatUtils.formatInt(energyCumulated," "+ context.getString(R.string.units_Wh)));

        updateRecyclerViewHeader();  //update header according to date in first visible item on the left

        setIcon(courseOfDayList.get(position).getWeatherID(), holder.weather, isDay);


    }

    //update header according to date in first visible item on the left of recyclerview
    private void updateRecyclerViewHeader() {
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        LinearLayoutManager llm = (LinearLayoutManager) manager;
        assert llm != null;
        int visiblePosition = llm.findFirstVisibleItemPosition();
        if (visiblePosition>-1) {
            Calendar HeaderTime = Calendar.getInstance();
            HeaderTime.setTimeZone(TimeZone.getTimeZone("GMT"));
            HeaderTime.setTimeInMillis(courseOfDayList.get(visiblePosition).getLocalForecastTime(context));
            int headerday = HeaderTime.get(Calendar.DAY_OF_WEEK);
            headerday = StringFormatUtils.getDayLong(headerday);
            recyclerViewHeader.setText(context.getResources().getString(headerday));

            courseOfDayHeaderDate=HeaderTime.getTime();

            if (weekRecyclerView!=null){
                WeekWeatherAdapter weekadapter = (WeekWeatherAdapter) weekRecyclerView.getAdapter();
                weekadapter.setCourseOfDayHeaderDate(courseOfDayHeaderDate);
            }
        }
    }

    @Override
    public int getItemCount() {
        return courseOfDayList.size();
    }

    class CourseOfDayViewHolder extends RecyclerView.ViewHolder {
        TextView time;
        ImageView weather;
        TextView directRadiationNormal;
        TextView diffuseRadiation;
        TextView power;
        TextView energyCum;

        CourseOfDayViewHolder(View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.course_of_day_time);
            weather = itemView.findViewById(R.id.course_of_day_weather);
            directRadiationNormal = itemView.findViewById(R.id.course_of_day_direct);
            diffuseRadiation = itemView.findViewById(R.id.course_of_day_diffuse);
            power = itemView.findViewById(R.id.course_of_day_power);
            energyCum = itemView.findViewById(R.id.course_of_energy_cum);
        }
    }

    public void setIcon(int value, ImageView imageView, boolean isDay) {
        imageView.setImageResource(UiResourceProvider.getIconResourceForWeatherCategory(value, isDay));
    }
}

