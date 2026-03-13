package com.aquarium.adapter;

import java.util.Random;

/**
 * ADAPTER PATTERN — Adaptee (Legacy System)
 *
 * Simulates an old ProAqua 2000 sensor that outputs data as a raw
 * semicolon-delimited string in a non-standard proprietary format.
 * This class cannot be modified (treat it as a third-party library).
 *
 * Format: "TEMP;PH;SAL;NH4;NO2;NO3;DO;VOLTAGE"
 * Units:  temperature in Fahrenheit, salinity as specific gravity (not ppt)
 */
public class LegacyProAquaSensor {

    private static final Random RANDOM = new Random();
    private boolean powered;
    private double calibrationOffset;

    public LegacyProAquaSensor() {
        this.powered = true;
        this.calibrationOffset = 0.0;
    }

    /**
     * Returns raw sensor data in proprietary format.
     * Temperature is in Fahrenheit; salinity is specific gravity (1.020–1.026).
     */
    public String getRawDataString() {
        if (!powered) return "ERROR;DEVICE_OFF";

        double tempF     = 75.0 + RANDOM.nextGaussian() * 2.0 + calibrationOffset;   // ~75°F ≈ 24°C
        double ph        = 8.2  + RANDOM.nextGaussian() * 0.05;
        double spGravity = 1.025 + RANDOM.nextGaussian() * 0.001;                    // specific gravity
        double nh4       = 0.05 + Math.abs(RANDOM.nextGaussian() * 0.02);
        double no2       = 0.02 + Math.abs(RANDOM.nextGaussian() * 0.01);
        double no3       = 5.0  + Math.abs(RANDOM.nextGaussian() * 1.0);
        double dissolvedO2 = 7.5 + RANDOM.nextGaussian() * 0.3;
        double voltage   = 12.0 + RANDOM.nextGaussian() * 0.1;                       // device voltage (ignored by adapter)

        return String.format("%.2f;%.3f;%.4f;%.4f;%.4f;%.3f;%.3f;%.2f",
            tempF, ph, spGravity, nh4, no2, no3, dissolvedO2, voltage);
    }

    public boolean selfTest() {
        return powered && RANDOM.nextDouble() > 0.005; // 99.5% success rate
    }

    public String getDeviceId()   { return "PROAQUA-2000-REV3"; }
    public void setPowered(boolean p)              { this.powered = p; }
    public void setCalibrationOffset(double offset){ this.calibrationOffset = offset; }
}
