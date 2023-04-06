package org.woheller69.weather.ui.RecycleList;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.SQLiteHelper;

import java.util.Collections;
import java.util.List;

/**
 * This is the adapter for the RecyclerList that is to be used for the overview of added locations.
 * For the most part, it has been taken from
 * https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.hmhbe8sku
 * as of 2016-08-03
 */
public class RecyclerOverviewListAdapter extends RecyclerView.Adapter<ItemViewHolder> implements ItemTouchHelperAdapter {

    /**
     * Member variables
     */
    private Context context;
    private final List<CityToWatch> cities;

    SQLiteHelper database;


    /**
     * Constructor.
     */
    public RecyclerOverviewListAdapter(Context context, List<CityToWatch> cities) {
        this.context = context;
        this.cities = cities;
        this.database = SQLiteHelper.getInstance(context);
    }


    /**
     * @see RecyclerView.Adapter#onCreateViewHolder(ViewGroup, int)
     * Returns the template for a list item.
     */
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_city_list, parent, false);
        return new ItemViewHolder(view);
    }

    /**
     * @see RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, int)
     * Sets the content of items.
     */
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        holder.cityName.setText(cities.get(position).getCityName());
        holder.azimuthAngle.setText(context.getString(R.string.edit_location_hint_azimuth) +": "+ cities.get(position).getAzimuthAngle());
        holder.tiltAngle.setText(context.getString(R.string.edit_location_hint_tilt) +": "+ cities.get(position).getTiltAngle());
        holder.cellsMaxPower.setText(context.getString(R.string.edit_location_hint_cells_max_power) +": "+ cities.get(position).getCellsMaxPower());
        holder.cellsEfficiency.setText(context.getString(R.string.edit_location_hint_cells_efficiency) +": "+ cities.get(position).getCellsEfficiency());
        holder.cellsArea.setText(context.getString(R.string.edit_location_hint_cells_area) +": "+ cities.get(position).getCellsArea());
        holder.diffuseEfficiency.setText(context.getString(R.string.edit_location_hint_diffuse_efficiency) +": "+ cities.get(position).getDiffuseEfficiency());
        holder.inverterPowerLimit.setText(context.getString(R.string.edit_location_hint_inverter_power_limit) +": "+ cities.get(position).getInverterPowerLimit());
        holder.inverterEfficiency.setText(context.getString(R.string.edit_location_hint_inverter_efficiency) +": "+ cities.get(position).getInverterEfficiency());

    }

    /**
     * @see RecyclerView.Adapter#getItemCount()
     */
    @Override
    public int getItemCount() {
        return cities.size();
    }

    /**
     * @see ItemTouchHelperAdapter#onItemDismiss(int)
     * Removes an item from the list.
     */
    @Override
    public void onItemDismiss(int position) {

        CityToWatch city = cities.get(position);
        database.deleteCityToWatch(city);
        cities.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * @see ItemTouchHelperAdapter#onItemMove(int, int)
     */
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // For updating the database records
        CityToWatch fromCityToWatch = cities.get(fromPosition);
        int fromRank = fromCityToWatch.getRank();
        CityToWatch toCityToWatch = cities.get(toPosition);
        int toRank = toCityToWatch.getRank();

        fromCityToWatch.setRank(toRank);
        toCityToWatch.setRank(fromRank);
        database.updateCityToWatch(fromCityToWatch);
        database.updateCityToWatch(toCityToWatch);
        Collections.swap(cities, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);

    }

    public CityToWatch getCitytoWatch(int position){
        return cities.get(position);
    }
    public void updateCity(CityToWatch cityToWatch, String cityName, float latitude, float longitude, float azimuth, float tilt, float cellsMaxPower, float cellsArea, float cellsEfficiency, float diffuseEfficiency, float inverterPowerLimit, float inverterEfficiency, int[] shadingElevation, int[] shadingOpacity) {
        cityToWatch.setCityName(cityName);
        cityToWatch.setLatitude(latitude);
        cityToWatch.setLongitude(longitude);
        cityToWatch.setAzimuthAngle(azimuth);
        cityToWatch.setTiltAngle(tilt);
        cityToWatch.setCellsMaxPower(cellsMaxPower);
        cityToWatch.setCellsArea(cellsArea);
        cityToWatch.setCellsEfficiency(cellsEfficiency);
        cityToWatch.setDiffuseEfficiency(diffuseEfficiency);
        cityToWatch.setInverterPowerLimit(inverterPowerLimit);
        cityToWatch.setInverterEfficiency(inverterEfficiency);
        cityToWatch.setShadingElevation(shadingElevation);
        cityToWatch.setShadingOpacity(shadingOpacity);
        database.updateCityToWatch(cityToWatch);
        notifyDataSetChanged();
    }
}