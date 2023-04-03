package org.woheller69.weather.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

import org.woheller69.weather.R;
import org.woheller69.weather.database.GeneralData;
import org.woheller69.weather.database.HourlyForecast;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.ui.updater.IUpdateableCityUI;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.ui.viewPager.WeatherPagerAdapter;

import java.lang.reflect.Field;
import java.util.List;

public class ForecastCityActivity extends NavigationActivity implements IUpdateableCityUI {
    private WeatherPagerAdapter pagerAdapter;
    private static MenuItem refreshActionButton;

    private int cityId = -1;
    private ViewPager2 viewPager2;
    private TabLayout tabLayout;
    private TextView noCityText;
    Context context;

    @Override
    protected void onPause() {
        super.onPause();

        ViewUpdater.removeSubscriber(this);
        ViewUpdater.removeSubscriber(pagerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SQLiteHelper db = SQLiteHelper.getInstance(this);
        if (db.getAllCitiesToWatch().isEmpty()) {
            // no cities selected.. don't show the viewPager - rather show a text that tells the user that no city was selected
            viewPager2.setVisibility(View.GONE);
            noCityText.setVisibility(View.VISIBLE);

        } else {
            noCityText.setVisibility(View.GONE);
            viewPager2.setVisibility(View.VISIBLE);
            pagerAdapter.loadCities();
            viewPager2.setAdapter(pagerAdapter);
            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2,false,false, (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position)));
            tabLayoutMediator.attach();
        }

        ViewUpdater.addSubscriber(this);
        ViewUpdater.addSubscriber(pagerAdapter);

        if (pagerAdapter.getItemCount()>0) {  //only if at least one city is watched
             //if pagerAdapter has item with current cityId go there, otherwise use cityId from current item
            if (pagerAdapter.getPosForCityID(cityId)==-1) cityId=pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem());
            GeneralData generalData = db.getGeneralDataByCityId(cityId);

            long timestamp = generalData.getTimestamp();
            long systemTime = System.currentTimeMillis() / 1000;
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            long updateInterval = (long) (Float.parseFloat(prefManager.getString("pref_updateInterval", "2")) * 60 * 60);

            if (timestamp + updateInterval - systemTime <= 0) {
                WeatherPagerAdapter.refreshSingleData(getApplicationContext(), true, cityId); //only update current tab at start
                ForecastCityActivity.startRefreshAnimation();

            }
            if (viewPager2.getCurrentItem()!=pagerAdapter.getPosForCityID(cityId)) viewPager2.setCurrentItem(pagerAdapter.getPosForCityID(cityId),false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_forecast_city);
        overridePendingTransition(0, 0);

        initResources();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                //Update current tab if outside update interval, show animation
                SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SQLiteHelper database = SQLiteHelper.getInstance(getApplicationContext().getApplicationContext());
                GeneralData generalData = database.getGeneralDataByCityId(pagerAdapter.getCityIDForPos(position));

                long timestamp = generalData.getTimestamp();
                long systemTime = System.currentTimeMillis() / 1000;
                long updateInterval = (long) (Float.parseFloat(prefManager.getString("pref_updateInterval", "2")) * 60 * 60);

                if (timestamp + updateInterval - systemTime <= 0) {
                    WeatherPagerAdapter.refreshSingleData(getApplicationContext(),true, pagerAdapter.getCityIDForPos(position));
                    ForecastCityActivity.startRefreshAnimation();

                }
                //post method needed to avoid Illegal State Exception: Cannot call this method in a scroll callback.
                viewPager2.post(() -> {
                    pagerAdapter.notifyItemChanged(position);  //fix crash with StaggeredGridLayoutManager when moving back and forth between items
                });
                cityId=pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem());  //save current cityId for next resume
            }

        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra("cityId")) {
            cityId = intent.getIntExtra("cityId",-1);
            if (pagerAdapter.getItemCount()>0) viewPager2.setCurrentItem(pagerAdapter.getPosForCityID(cityId),false);
        }
    }

    private void initResources() {
        viewPager2 = findViewById(R.id.viewPager2);
        reduceViewpager2DragSensitivity(viewPager2,2);
        tabLayout = findViewById(R.id.tab_layout);
        pagerAdapter = new WeatherPagerAdapter(this, getSupportFragmentManager(),getLifecycle());
        noCityText = findViewById(R.id.noCitySelectedText);
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_weather;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_forecast_city, menu);

        final Menu m = menu;

        refreshActionButton = menu.findItem(R.id.menu_refresh);
        refreshActionButton.setActionView(R.layout.menu_refresh_action_view);
        refreshActionButton.getActionView().setOnClickListener(v -> m.performIdentifierAction(refreshActionButton.getItemId(), 0));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SQLiteHelper db = SQLiteHelper.getInstance(this);

        if (id==R.id.menu_refresh){
            if (!db.getAllCitiesToWatch().isEmpty()) {  //only if at least one city is watched, otherwise crash
                WeatherPagerAdapter.refreshSingleData(getApplicationContext(),true, pagerAdapter.getCityIDForPos(viewPager2.getCurrentItem()));
                ForecastCityActivity.startRefreshAnimation();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    public void processNewGeneralData(GeneralData data) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    @Override
    public void processNewWeekForecasts(List<WeekForecast> forecasts) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    @Override
    public void processNewForecasts(List<HourlyForecast> hourlyForecasts) {
        if (refreshActionButton != null && refreshActionButton.getActionView() != null) {
            refreshActionButton.getActionView().clearAnimation();
        }
    }

    public static void startRefreshAnimation(){
        {
            if(refreshActionButton !=null && refreshActionButton.getActionView() != null) {
                RotateAnimation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotate.setDuration(500);
                rotate.setRepeatCount(5);
                rotate.setInterpolator(new LinearInterpolator());
                rotate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        refreshActionButton.getActionView().setActivated(false);
                        refreshActionButton.getActionView().setEnabled(false);
                        refreshActionButton.getActionView().setClickable(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        refreshActionButton.getActionView().setActivated(true);
                        refreshActionButton.getActionView().setEnabled(true);
                        refreshActionButton.getActionView().setClickable(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                refreshActionButton.getActionView().startAnimation(rotate);
            }
        }
    }

    //https://devdreamz.com/question/348298-how-to-modify-sensitivity-of-viewpager
    private void reduceViewpager2DragSensitivity(ViewPager2 viewPager, int sensitivity) {
        try {
            Field ff = ViewPager2.class.getDeclaredField("mRecyclerView") ;
            ff.setAccessible(true);
            RecyclerView recyclerView =  (RecyclerView) ff.get(viewPager);
            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop") ;
            touchSlopField.setAccessible(true);
            int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView,touchSlop*sensitivity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}

