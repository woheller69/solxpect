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
    private float inverterPowerLimit;
    private float inverterEfficiency;
    private float azimuthAngle;
    private float tiltAngle;
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
        this.inverterPowerLimit =600;
        this.inverterEfficiency =95;
        this.azimuthAngle=170;
        this.tiltAngle =90;

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

    public float getInverterEfficiency() {
        return inverterEfficiency;
    }

    public float getInverterPowerLimit() {
        return inverterPowerLimit;
    }

    public float getTiltAngle() {
        return tiltAngle;
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

    public void setInverterEfficiency(float inverterEfficiency) {
        this.inverterEfficiency = inverterEfficiency;
    }

    public void setInverterPowerLimit(float inverterPowerLimit) {
        this.inverterPowerLimit = inverterPowerLimit;
    }

    public void setDiffuseEfficiency(float diffuseEfficiency) {
        this.diffuseEfficiency = diffuseEfficiency;
    }

    public void setTiltAngle(float tiltAngle) {
        this.tiltAngle = tiltAngle;
    }
}