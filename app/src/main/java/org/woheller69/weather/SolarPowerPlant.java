package org.woheller69.weather;

import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.Grena3;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SolarPowerPlant {
    double albedo;
    double latitude;
    double longitude;
    double cellsMaxPower;
    double cellsArea;
    double cellsEfficiency;
    double cellsTempCoeff;
    double diffuseEfficiency;
    double inverterPowerLimit;
    double inverterEfficiency;
    boolean isCentralInverter;
    double azimuthAngle;
    double tiltAngle;
    private final int[] shadingElevation;
    private final int[] shadingOpacity;

    public SolarPowerPlant(double latitude, double longitude, double cellsMaxPower, double cellsArea, double cellsEfficiency, double cellsTempCoeff, double diffuseEfficiency, double inverterPowerLimit, double inverterEfficiency,boolean isCentralInverter, double azimuthAngle, double tiltAngle, int[] shadingElevation, int[] shadingOpacity, double albedo ) {
        this.albedo = albedo;
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
        this.isCentralInverter = isCentralInverter;

    }

    public float getPower(double solarPowerNormal, double solarPowerDiffuse, double shortwaveRadiation, long epochTimeSeconds, double ambientTemperature) {
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
                double shFactor=0;
                Instant sh;
                ZonedDateTime shDateTime;
                AzimuthZenithAngle shPosition;
                double shSolarAzimuth;
                double shSolarElevation;

                //Calculate shading in 6 intervals per hour -> 10min steps xx:05 / xx:15 / xx:25 / xx:35 / xx:45 / xx:55
                int numSteps = 6;
                long interval = 3600 / numSteps;
                for (int j=0; j<numSteps;j++){
                    sh = Instant.ofEpochSecond(epochTimeSeconds-(numSteps-1)*interval/2+j*interval);
                    shDateTime = ZonedDateTime.ofInstant(sh, ZoneId.of("GMT"));  //currentTimeMillis is in GMT

                    shPosition = Grena3.calculateSolarPosition(
                            shDateTime,
                            latitude,
                            longitude,
                            DeltaT.estimate(shDateTime.toLocalDate())); // delta T (s)

                    shSolarAzimuth = shPosition.getAzimuth();
                    shSolarElevation = 90 - shPosition.getZenithAngle();

                    //shading values are provided in 10 degree ranges -> total of 36 ranges
                    int shadingIndex = ((((int) Math.round((shSolarAzimuth + 5) / 10)) - 1) % 36 + 36) % 36;
                    if (shadingElevation[shadingIndex] > shSolarElevation) {
                        shFactor += (double) (100 - shadingOpacity[shadingIndex])/(numSteps*100);
                    } else {
                        shFactor += (double) 100/(numSteps*100);  //numSteps iterations with no shading give 100%
                    }
                }
                efficiency *= shFactor;
            }
        }

        double totalRadiationOnCell = solarPowerNormal * efficiency + solarPowerDiffuse * diffuseEfficiency + shortwaveRadiation * (0.5-0.5*Math.cos(tiltAngle/180*Math.PI)) * albedo;  //flat plate equivalent of the solar irradiance
        double cellTemperature = calcCellTemperature(ambientTemperature,totalRadiationOnCell);
        double dcPower;
        if (cellsEfficiency!=0 && cellsArea!=0){
            dcPower = totalRadiationOnCell * (1+(cellTemperature - 25)*cellsTempCoeff) * cellsEfficiency * cellsArea;
        } else { //assume cellMaxPower is defined at 1000W/sqm
            dcPower = totalRadiationOnCell/1000 * (1+(cellTemperature - 25)*cellsTempCoeff) * cellsMaxPower;
        }

        double acPower;
        if (!isCentralInverter)
            acPower = Math.min(dcPower * inverterEfficiency, inverterPowerLimit);
        else
            acPower = dcPower * inverterEfficiency;  //do limiting on system level

        return (float) acPower;
    }

    public static int calcDiffuseEfficiency(float tilt){
        return (int) ( 50 + 50* Math.cos(tilt/180*Math.PI));
    }

    public static double calcCellTemperature(double ambientTemperature, double totalIrradiance){
        //models from here: https://www.scielo.br/j/babt/a/FBq5Pmm4gSFqsfh3V8MxfGN/  Photovoltaic Cell Temperature Estimation for a Grid-Connect Photovoltaic Systems in Curitiba
        //float cellTemperature =  30.006f + 0.0175f*(totalIrradiance-300f)+1.14f*(ambientTemperature-25f);  //Lasnier and Ang  Lasnier, F.; Ang, T. G. Photovoltaic engineering handbook, 1st ed.; IOP Publishing LTD: Lasnier, France, 1990; pp. 258.
        //float cellTemperature = ambientTemperature + 0.028f*totalIrradiance-1f;  //Schott Schott, T. Operation temperatures of PV modules. Photovoltaic solar energy conference 1985, pp. 392-396.
        double cellTemperature = ambientTemperature + 0.0342f*totalIrradiance;  //Ross model: https://www.researchgate.net/publication/275438802_Thermal_effects_of_the_extended_holographic_regions_for_holographic_planar_concentrator
        //assuming "not so well cooled" : 0.0342
        return cellTemperature;
    }
}
