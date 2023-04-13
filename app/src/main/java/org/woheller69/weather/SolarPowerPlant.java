package org.woheller69.weather;

import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.Grena3;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SolarPowerPlant {
    double latitude;
    double longitude;
    double cellsMaxPower;
    double cellsArea;
    double cellsEfficiency;
    double cellsTempCoeff;
    double diffuseEfficiency;
    double inverterPowerLimit;
    double inverterEfficiency;
    double azimuthAngle;
    double tiltAngle;
    private int[] shadingElevation;
    private int[] shadingOpacity;

    public SolarPowerPlant(double latitude, double longitude, double cellsMaxPower, double cellsArea, double cellsEfficiency, double cellsTempCoeff, double diffuseEfficiency, double inverterPowerLimit, double inverterEfficiency, double azimuthAngle, double tiltAngle, int[] shadingElevation, int[] shadingOpacity ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.cellsMaxPower = cellsMaxPower;
        this.cellsArea = cellsArea;
        this.cellsEfficiency = cellsEfficiency / 100;
        this.diffuseEfficiency = diffuseEfficiency / 100;
        this.inverterPowerLimit = inverterPowerLimit;
        this.inverterEfficiency = inverterEfficiency / 100;
        this.azimuthAngle = azimuthAngle;
        this.tiltAngle = tiltAngle;
        this.shadingElevation = shadingElevation;
        this.shadingOpacity = shadingOpacity;
        this.cellsTempCoeff = cellsTempCoeff / 100;

    }

    public float getPower(double solarPowerNormal, double solarPowerDiffuse, long epochTimeSeconds, float ambientTemperature) {
        Instant i = Instant.ofEpochSecond(epochTimeSeconds); //currentTimeMillis is in GMT
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(i, ZoneId.of("GMT"));

        AzimuthZenithAngle position = Grena3.calculateSolarPosition(
                dateTime,
                latitude,
                longitude,
                DeltaT.estimate(dateTime.toLocalDate())); // delta T (s)

        double solarAzimuth = position.getAzimuth();
        double solarElevation = 90 - position.getZenithAngle();

        Double[] directionSun = {Math.sin(solarAzimuth / 180 * Math.PI) * Math.cos(solarElevation / 180 * Math.PI), Math.cos(solarAzimuth / 180 * Math.PI) * Math.cos(solarElevation / 180 * Math.PI), Math.sin(solarElevation / 180 * Math.PI)};
        Double[] normalPanel = {Math.sin(azimuthAngle / 180 * Math.PI) * Math.cos((90 - tiltAngle) / 180 * Math.PI), Math.cos(azimuthAngle / 180 * Math.PI) * Math.cos((90 - tiltAngle) / 180 * Math.PI), Math.sin((90 - tiltAngle) / 180 * Math.PI)};

        double efficiency = 0;  //calculate scalar product of sunDirection and normalPanel vectors
        if(solarPowerNormal>0) {  //only needed if normal radiation is available
            for (int j = 0; j < directionSun.length; j++) {
                efficiency += directionSun[j] * normalPanel[j];
            }

            efficiency = Math.max(0, efficiency); //scalar product is negative if sun points to back of module. set 0 in this case

            if (efficiency > 0) {
                //Calculate shading  in 10 degree ranges, total 36 ranges
                int shadingIndex = ((((int) Math.round((solarAzimuth + 5) / 10)) - 1) % 36 + 36) % 36;
                if (shadingElevation[shadingIndex] > solarElevation) {
                    efficiency *= (double) (100 - shadingOpacity[shadingIndex])/100;
                }
            }
        }

        float totalRadiationOnCell = (float) (solarPowerNormal * efficiency + solarPowerDiffuse * diffuseEfficiency);  //flat plate equivalent of the solar irradiance
        float cellTemperature = calcCellTemperature(ambientTemperature,totalRadiationOnCell);

        double dcPower = totalRadiationOnCell * cellsEfficiency * (1+(cellTemperature - 25)*cellsTempCoeff) * cellsArea;

        double acPower = Math.min(dcPower * inverterEfficiency, inverterPowerLimit);

        return (float) acPower;
    }

    public static float calcCellTemperature(float ambientTemperature, float totalIrradiance){
        //models from here: https://www.scielo.br/j/babt/a/FBq5Pmm4gSFqsfh3V8MxfGN/  Photovoltaic Cell Temperature Estimation for a Grid-Connect Photovoltaic Systems in Curitiba
        //float cellTemperature =  30.006f + 0.0175f*(totalIrradiance-300f)+1.14f*(ambientTemperature-25f);  //Lasnier and Ang  Lasnier, F.; Ang, T. G. Photovoltaic engineering handbook, 1st ed.; IOP Publishing LTD: Lasnier, France, 1990; pp. 258.
        //float cellTemperature = ambientTemperature + 0.028f*totalIrradiance-1f;  //Schott Schott, T. Operation temperatures of PV modules. Photovoltaic solar energy conference 1985, pp. 392-396.
        float cellTemperature = ambientTemperature + 0.0342f*totalIrradiance;  //Ross model: https://www.researchgate.net/publication/275438802_Thermal_effects_of_the_extended_holographic_regions_for_holographic_planar_concentrator
        //assuming "not so well cooled" : 0.0342
        return cellTemperature;
    }
}
