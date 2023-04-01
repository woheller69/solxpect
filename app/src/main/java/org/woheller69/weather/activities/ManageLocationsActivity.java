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
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.woheller69.weather.R;
import org.woheller69.weather.database.City;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.SQLiteHelper;
import org.woheller69.weather.dialogs.AddLocationDialogOmGeocodingAPI;
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
                        AlertDialog.Builder alert = new AlertDialog.Builder(context);

                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_edit_location, null);

                        alert.setTitle(getString(R.string.edit_location_title));
                        alert.setView(dialogView);
                        EditText editLatitude = (EditText) dialogView.findViewById(R.id.EditLocation_Lat);
                        EditText editLongitude = (EditText) dialogView.findViewById(R.id.EditLocation_Lon);
                        EditText editCity = (EditText) dialogView.findViewById(R.id.EditLocation_Name);
                        EditText editAzimuth = (EditText) dialogView.findViewById(R.id.EditLocation_Azimuth);
                        EditText editElevation = (EditText) dialogView.findViewById(R.id.EditLocation_Elevation);
                        EditText editCellsMaxPower = (EditText) dialogView.findViewById(R.id.EditLocation_Cell_Max_Power);
                        EditText editCellsArea = (EditText) dialogView.findViewById(R.id.EditLocation_Cells_Area);
                        EditText editCellsEfficiency = (EditText) dialogView.findViewById(R.id.EditLocation_Cell_Efficiency);
                        EditText editDiffuseEfficiency = (EditText) dialogView.findViewById(R.id.EditLocation_Diffuse_Efficiency);
                        EditText editConverterPowerLimit = (EditText) dialogView.findViewById(R.id.EditLocation_Converter_Power_Limit);
                        EditText editConverterEfficiency = (EditText) dialogView.findViewById(R.id.EditLocation_Converter_Efficiency);

                        editCity.setText(adapter.getCitytoWatch(position).getCityName());
                        editLatitude.setText(Float.toString(adapter.getCitytoWatch(position).getLatitude()));
                        editLongitude.setText(Float.toString(adapter.getCitytoWatch(position).getLongitude()));
                        editAzimuth.setText(Float.toString(adapter.getCitytoWatch(position).getAzimuthAngle()));
                        editElevation.setText(Float.toString(adapter.getCitytoWatch(position).getElevationAngle()));
                        editCellsMaxPower.setText(Float.toString(adapter.getCitytoWatch(position).getCellsMaxPower()));
                        editCellsArea.setText(Float.toString(adapter.getCitytoWatch(position).getCellsArea()));
                        editCellsEfficiency.setText(Float.toString(adapter.getCitytoWatch(position).getCellsEfficiency()));
                        editDiffuseEfficiency.setText(Float.toString(adapter.getCitytoWatch(position).getDiffuseEfficiency()));
                        editConverterPowerLimit.setText(Float.toString(adapter.getCitytoWatch(position).getConverterPowerLimit()));
                        editConverterEfficiency.setText(Float.toString(adapter.getCitytoWatch(position).getConverterEfficiency()));
                        editElevation.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                float elevation = Float.parseFloat(editElevation.getText().toString());
                                editDiffuseEfficiency.setText(Float.toString(100-50 * elevation/90));
                            }
                        });

                        alert.setPositiveButton(getString(R.string.dialog_edit_change_button), (dialog, whichButton) -> {
                            adapter.updateCity(position, String.valueOf(editCity.getText()),
                                    Float.parseFloat(editLatitude.getText().toString()),
                                    Float.parseFloat(editLongitude.getText().toString()),
                                    Float.parseFloat(editAzimuth.getText().toString()),
                                    Float.parseFloat(editElevation.getText().toString()),
                                    Float.parseFloat(editCellsMaxPower.getText().toString()),
                                    Float.parseFloat(editCellsArea.getText().toString()),
                                    Float.parseFloat(editCellsEfficiency.getText().toString()),
                                    Float.parseFloat(editDiffuseEfficiency.getText().toString()),
                                    Float.parseFloat(editConverterPowerLimit.getText().toString()),
                                    Float.parseFloat(editConverterEfficiency.getText().toString())
                                    );
                        });
                        alert.setNegativeButton(getString(R.string.dialog_add_close_button), (dialog, whichButton) -> {
                        });

                        alert.show();
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
