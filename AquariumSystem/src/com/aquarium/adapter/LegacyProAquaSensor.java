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
     * All values are strictly clamped inside valid physical ranges — the
     * sensor hardware itself enforces these limits via its onboard ADC.
     */
    public String getRawDataString() {
        if (!powered) return "ERROR;DEVICE_OFF";

        double tempF       = clamp(75.0 + gaussianNoise(1.2) + calibrationOffset, 70.0, 85.0);
        double ph          = clamp(8.20 + gaussianNoise(0.03), 8.00, 8.50);
        double spGravity   = clamp(1.025 + gaussianNoise(0.0006), 1.022, 1.028);
        double nh4         = clamp(0.04  + Math.abs(gaussianNoise(0.01)), 0.0, 0.20);
        double no2         = clamp(0.02  + Math.abs(gaussianNoise(0.006)), 0.0, 0.15);
        double no3         = clamp(5.0   + Math.abs(gaussianNoise(0.6)), 0.0, 15.0);
        double dissolvedO2 = clamp(7.6   + gaussianNoise(0.15), 6.5, 10.0);
        double voltage     = clamp(12.0  + gaussianNoise(0.03), 11.5, 12.5);

        return String.format("%.2f;%.3f;%.4f;%.4f;%.4f;%.3f;%.3f;%.2f",
            tempF, ph, spGravity, nh4, no2, no3, dissolvedO2, voltage);
    }

    private double gaussianNoise(double sigma) {
        return RANDOM.nextGaussian() * sigma;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Self-test always succeeds while powered — simulates a warmed-up device. */
    public boolean selfTest() {
        return powered;
    }

    public String getDeviceId()                     { return "PROAQUA-2000-REV3"; }
    public void setPowered(boolean p)               { this.powered = p; }
    public void setCalibrationOffset(double offset) { this.calibrationOffset = offset; }
}
