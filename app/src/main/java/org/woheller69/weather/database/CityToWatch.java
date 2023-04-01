package org.woheller69.weather.database;

/**
 * This class is the database model for the cities to watch. 'Cities to watch' means the locations
 * for which a user would like to see the weather for. This includes those locations that will be
 * deleted after app close (non-persistent locations).
 */
public class CityToWatch {

    private int id;
    private int cityId;
    private String cityName;
    private float lon;
    private float lat;
    private float cellsMaxPower;
    private float cellsArea;
    private float cellsEfficiency;
    private float diffuseEfficiency;
    private float converterPowerLimit;
    private float converterEfficiency;
    private float azimuthAngle;
    private float elevationAngle;
    private int rank;

    public CityToWatch() {
    }

    public CityToWatch(int rank, int id, int cityId, float lon, float lat, String cityName) {
        this.rank = rank;
        this.lon = lon;
        this.lat = lat;
        this.id = id;
        this.cityId = cityId;
        this.cityName = cityName;
        this.cellsMaxPower=650;
        this.cellsArea=3.18f;
        this.cellsEfficiency=19.3f;
        this.diffuseEfficiency=40;
        this.converterPowerLimit=600;
        this.converterEfficiency=95;
        this.azimuthAngle=170;
        this.elevationAngle=90;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setLongitude(float lon) { this.lon = lon; }

    public float getLongitude() {  return lon; }

    public float getLatitude() {  return lat; }

    public void setLatitude(float lat) { this.lat = lat; }

    public float getCellsMaxPower() {
        return cellsMaxPower;
    }

    public float getCellsArea() {
        return cellsArea;
    }

    public float getAzimuthAngle() {
        return azimuthAngle;
    }

    public float getCellsEfficiency() {
        return cellsEfficiency;
    }

    public float getDiffuseEfficiency() {
        return diffuseEfficiency;
    }

    public float getConverterEfficiency() {
        return converterEfficiency;
    }

    public float getConverterPowerLimit() {
        return converterPowerLimit;
    }

    public float getElevationAngle() {
        return elevationAngle;
    }

    public void setCellsMaxPower(float cellsMaxPower) {
        this.cellsMaxPower = cellsMaxPower;
    }

    public void setCellsArea(float cellsArea) {
        this.cellsArea = cellsArea;
    }

    public void setAzimuthAngle(float azimuthAngle) {
        this.azimuthAngle = azimuthAngle;
    }

    public void setCellsEfficiency(float cellsEfficiency) {
        this.cellsEfficiency = cellsEfficiency;
    }

    public void setConverterEfficiency(float converterEfficiency) {
        this.converterEfficiency = converterEfficiency;
    }

    public void setConverterPowerLimit(float converterPowerLimit) {
        this.converterPowerLimit = converterPowerLimit;
    }

    public void setDiffuseEfficiency(float diffuseEfficiency) {
        this.diffuseEfficiency = diffuseEfficiency;
    }

    public void setElevationAngle(float elevationAngle) {
        this.elevationAngle = elevationAngle;
    }
}