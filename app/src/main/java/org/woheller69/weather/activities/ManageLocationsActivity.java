package org.woheller69.weather.activities;

import android.content.Context;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.woheller69.weather.R;
import org.woheller69.weather.database.City;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.dialogs.AddLocationDialogOmGeocodingAPI;
import org.woheller69.weather.ui.Help.InputFilterMinMax;
import org.woheller69.weather.ui.RecycleList.RecyclerItemClickListener;
import org.woheller69.weather.ui.RecycleList.RecyclerOverviewListAdapter;
import org.woheller69.weather.ui.RecycleList.SimpleItemTouchHelperCallback;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//in-App: where cities get added & sorted
public class ManageLocationsActivity extends NavigationActivity {

    private SQLiteHelper database;

    private ItemTouchHelper.Callback callback;
    private ItemTouchHelper touchHelper;
    RecyclerOverviewListAdapter adapter;
    List<CityToWatch> cities;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_locations);
        overridePendingTransition(0, 0);
        context=this;
        database = SQLiteHelper.getInstance(getApplicationContext());


        try {
            cities = database.getAllCitiesToWatch();
            Collections.sort(cities, new Comparator<CityToWatch>() {
                @Override
                public int compare(CityToWatch o1, CityToWatch o2) {
                    return o1.getRank() - o2.getRank();
                }

            });
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(getBaseContext(), "No cities in DB", Toast.LENGTH_SHORT);
            toast.show();
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_view_cities);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getBaseContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        CityToWatch city = adapter.getCitytoWatch(position);
                        editCityToWatch(city);
                    }

                    public void onLongItemClick(View view, int position) {

                    }

                })
        );

        adapter = new RecyclerOverviewListAdapter(getApplicationContext(), cities);
        recyclerView.setAdapter(adapter);
        recyclerView.setFocusable(false);

        callback = new SimpleItemTouchHelperCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        FloatingActionButton addFab1 = (FloatingActionButton) findViewById(R.id.fabAddLocation);

            if (addFab1 != null) {

                addFab1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        AddLocationDialogOmGeocodingAPI addLocationDialog = new AddLocationDialogOmGeocodingAPI();
                        addLocationDialog.show(fragmentManager, "AddLocationDialog");
                        getSupportFragmentManager().executePendingTransactions();
                        addLocationDialog.getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                });
            }

    }

    private void editCityToWatch(CityToWatch city) {
        int[] shadingElevation = city.getShadingElevation();
        int[] shadingOpacity = city.getShadingOpacity();
        TextView[] textViews = new TextView[shadingElevation.length];
        EditText[] elevationViews = new EditText[shadingElevation.length];
        EditText[] opacityViews = new EditText[shadingElevation.length];

        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_location, null);

        ViewGroup shading = (ViewGroup) dialogView.findViewById(R.id.edit_Location_shading);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.weight = 1;

        LinearLayout header = new LinearLayout(this);
        TextView headerRange = new TextView(this);
        TextView headerMinElevation = new TextView(this);
        TextView headerOpacity = new TextView(this);
        headerRange.setText(R.string.edit_location_shading_azimuth_heading);
        headerRange.setPadding(5,0,5,0);
        headerMinElevation.setText(R.string.edit_location_shading_solar_elevation_heading);
        headerMinElevation.setPadding(5,0,5,0);
        headerOpacity.setText(R.string.edit_location_shading_opacity_heading);
        headerOpacity.setPadding(5,0,5,0);
        headerRange.setLayoutParams(p);
        headerMinElevation.setLayoutParams(p);
        headerOpacity.setLayoutParams(p);
        header.addView(headerRange);
        header.addView(headerMinElevation);
        header.addView(headerOpacity);
        shading.addView(header);

        LinearLayout[] container = new LinearLayout[shadingElevation.length];
        for (int i = 0; i < shadingElevation.length; i++) {
            container[i] = new LinearLayout(this);
            container[i].setOrientation(LinearLayout.HORIZONTAL);
            textViews[i] = new TextView(this);
            opacityViews[i] = new EditText(this);
            elevationViews[i] = new EditText(this);
            textViews[i].setText("[" + (i*10) +","+ (i*10+10)+"]");
            textViews[i].setTextSize(18);
            elevationViews[i].setText(Integer.toString(shadingElevation[i]));
            elevationViews[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            elevationViews[i].setFilters(new InputFilter[]{ new InputFilterMinMax(0, 90)});
            elevationViews[i].setTextSize(18);
            opacityViews[i].setText(Integer.toString(shadingOpacity[i]));
            opacityViews[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            opacityViews[i].setFilters(new InputFilter[]{ new InputFilterMinMax(0, 100)});
            opacityViews[i].setTextSize(18);
            textViews[i].setLayoutParams(p);
            elevationViews[i].setLayoutParams(p);
            opacityViews[i].setLayoutParams(p);
            container[i].addView(textViews[i]);
            container[i].addView(elevationViews[i]);
            container[i].addView(opacityViews[i]);
            shading.addView(container[i]);
        }

        alert.setTitle(getString(R.string.edit_location_title));
        alert.setView(dialogView);
        EditText editLatitude = (EditText) dialogView.findViewById(R.id.EditLocation_Lat);
        EditText editLongitude = (EditText) dialogView.findViewById(R.id.EditLocation_Lon);
        EditText editCity = (EditText) dialogView.findViewById(R.id.EditLocation_Name);
        EditText editAzimuth = (EditText) dialogView.findViewById(R.id.EditLocation_Azimuth);
        EditText editTilt = (EditText) dialogView.findViewById(R.id.EditLocation_Tilt);
        EditText editCellsMaxPower = (EditText) dialogView.findViewById(R.id.EditLocation_Cell_Max_Power);
        EditText editCellsArea = (EditText) dialogView.findViewById(R.id.EditLocation_Cells_Area);
        EditText editCellsEfficiency = (EditText) dialogView.findViewById(R.id.EditLocation_Cell_Efficiency);
        EditText editCellsTempCoeff = (EditText) dialogView.findViewById(R.id.EditLocation_Cell_Temp_Coeff);
        EditText editDiffuseEfficiency = (EditText) dialogView.findViewById(R.id.EditLocation_Diffuse_Efficiency);
        EditText editInverterPowerLimit = (EditText) dialogView.findViewById(R.id.EditLocation_Inverter_Power_Limit);
        EditText editInverterEfficiency = (EditText) dialogView.findViewById(R.id.EditLocation_Inverter_Efficiency);

        editCity.setText(city.getCityName());
        editLatitude.setText(Float.toString(city.getLatitude()));
        editLatitude.setFilters(new InputFilter[]{ new InputFilterMinMax(-90, 90)});
        editLongitude.setText(Float.toString(city.getLongitude()));
        editLongitude.setFilters(new InputFilter[]{ new InputFilterMinMax(-180, 180)});
        editAzimuth.setText(Float.toString(city.getAzimuthAngle()));
        editAzimuth.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 360)});
        editTilt.setText(Float.toString(city.getTiltAngle()));
        editTilt.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 90)});
        editCellsMaxPower.setText(Float.toString(city.getCellsMaxPower()));
        editCellsArea.setText(Float.toString(city.getCellsArea()));
        editCellsEfficiency.setText(Float.toString(city.getCellsEfficiency()));
        editCellsEfficiency.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 100)});
        editCellsTempCoeff.setText(Float.toString(city.getCellsTempCoeff()));
        editCellsTempCoeff.setFilters(new InputFilter[]{ new InputFilterMinMax(-100, 100)});
        editDiffuseEfficiency.setText(Float.toString(city.getDiffuseEfficiency()));
        editDiffuseEfficiency.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 100)});
        editInverterPowerLimit.setText(Float.toString(city.getInverterPowerLimit()));
        editInverterEfficiency.setText(Float.toString(city.getInverterEfficiency()));
        editInverterEfficiency.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 100)});
        editTilt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                float tilt = Float.parseFloat(!editTilt.getText().toString().isEmpty() ? editTilt.getText().toString() : "0");
                int diffuseEfficiency = (int) (100-50 * tilt/90);
                editDiffuseEfficiency.setText(Float.toString((float) diffuseEfficiency));
            }
        });

        alert.setPositiveButton(getString(R.string.dialog_edit_change_button), (dialog, whichButton) -> {

            for (int i = 0; i < shadingElevation.length ; i++) {
                shadingElevation[i]= Integer.parseInt(elevationViews[i].getText().toString().isEmpty() ? "0" : elevationViews[i].getText().toString());
                shadingOpacity[i]= Integer.parseInt(opacityViews[i].getText().toString().isEmpty() ? "0" : opacityViews[i].getText().toString());
            }
            adapter.updateCity(city, String.valueOf(editCity.getText()),
                    Float.parseFloat(editLatitude.getText().toString().isEmpty() ? "0" : editLatitude.getText().toString()),
                    Float.parseFloat(editLongitude.getText().toString().isEmpty() ? "0" : editLongitude.getText().toString()),
                    Float.parseFloat(editAzimuth.getText().toString().isEmpty() ? "0" : editAzimuth.getText().toString()),
                    Float.parseFloat(editTilt.getText().toString().isEmpty() ? "0" : editTilt.getText().toString()),
                    Float.parseFloat(editCellsMaxPower.getText().toString().isEmpty() ? "0" : editCellsMaxPower.getText().toString()),
                    Float.parseFloat(editCellsArea.getText().toString().isEmpty() ? "0" : editCellsArea.getText().toString()),
                    Float.parseFloat(editCellsEfficiency.getText().toString().isEmpty() ? "0" : editCellsEfficiency.getText().toString()),
                    Float.parseFloat(editCellsTempCoeff.getText().toString().isEmpty() ? "0" : editCellsTempCoeff.getText().toString()),
                    Float.parseFloat(editDiffuseEfficiency.getText().toString().isEmpty() ? "0" : editDiffuseEfficiency.getText().toString()),
                    Float.parseFloat(editInverterPowerLimit.getText().toString().isEmpty() ? "0" : editInverterPowerLimit.getText().toString()),
                    Float.parseFloat(editInverterEfficiency.getText().toString().isEmpty() ? "0" : editInverterEfficiency.getText().toString()),
                    shadingElevation,
                    shadingOpacity
                    );
        });
        alert.setNegativeButton(getString(R.string.dialog_add_close_button), (dialog, whichButton) -> {
        });

        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_manage;
    }

    public void addCityToList(City city) {
        CityToWatch newCity=convertCityToWatched(city);
        long id=database.addCityToWatch(newCity);
        newCity.setId((int) id);
        newCity.setCityId((int) id);  //use id also instead of city id as unique identifier
        cities.add(newCity);
        adapter.notifyDataSetChanged();
        editCityToWatch(newCity);
    }
    private CityToWatch convertCityToWatched(City selectedCity) {

        return new CityToWatch(
                database.getMaxRank() + 1,
                -1,
                selectedCity.getCityId(), selectedCity.getLongitude(),selectedCity.getLatitude(),
                selectedCity.getCityName()
        );
    }

}
