package com.aquarium.adapter;

import com.aquarium.model.WaterParameters;

/**
 * ADAPTER PATTERN — Adapter
 *
 * Wraps the incompatible {@link LegacyProAquaSensor} and translates its
 * proprietary semicolon-delimited string into the {@link SensorReader}
 * interface that the application understands.
 *
 * Conversions:
 *   Fahrenheit  ->  Celsius
 *   Specific gravity (SG) -> salinity in ppt
 */
public class LegacyProAquaAdapter implements SensorReader {

    private static final String SENSOR_NAME = "ProAqua 2000 (Legacy)";

    private final LegacyProAquaSensor legacySensor;

    public LegacyProAquaAdapter(LegacyProAquaSensor legacySensor) {
        this.legacySensor = legacySensor;
    }

    /**
     * Reads and converts sensor data. No retries needed — the sensor now
     * always produces valid clamped output while powered.
     * If the device is off or returns an error token, a SensorReadException is thrown.
     */
    @Override
    public WaterParameters readParameters() throws SensorReadException {
        String raw = legacySensor.getRawDataString();

        if (raw == null || raw.isBlank()) {
            throw new SensorReadException(SENSOR_NAME, "El sensor no devolvio datos.");
        }
        if (raw.startsWith("ERROR")) {
            throw new SensorReadException(SENSOR_NAME,
                "El sensor respondio con error: " + raw);
        }

        String[] parts = raw.split(";");
        if (parts.length < 7) {
            throw new SensorReadException(SENSOR_NAME,
                "Trama incompleta: " + parts.length + " campo(s). Trama: [" + raw + "]");
        }

        try {
            double tempF     = Double.parseDouble(parts[0].trim());
            double ph        = Double.parseDouble(parts[1].trim());
            double spGravity = Double.parseDouble(parts[2].trim());
            double ammonium  = Double.parseDouble(parts[3].trim());
            double nitrites  = Double.parseDouble(parts[4].trim());
            double nitrates  = Double.parseDouble(parts[5].trim());
            double oxygen    = Double.parseDouble(parts[6].trim());

            // Unit conversions (core adapter responsibility)
            double tempC       = fahrenheitToCelsius(tempF);
            double salinityPpt = specificGravityToSalinityPpt(spGravity);

            return new WaterParameters(tempC, ph, salinityPpt,
                Math.max(0, ammonium), Math.max(0, nitrites),
                Math.max(0, nitrates), oxygen);

        } catch (NumberFormatException e) {
            throw new SensorReadException(SENSOR_NAME,
                "No se pudo parsear la trama: [" + raw + "]", e);
        }
    }

    @Override
    public boolean isConnected() {
        return legacySensor.selfTest();
    }

    @Override
    public String getSensorName() { return SENSOR_NAME; }

    @Override
    public String getVersion() { return legacySensor.getDeviceId(); }

    @Override
    public String calibrate() {
        legacySensor.setCalibrationOffset(0.0);
        return "ProAqua 2000 calibrado. Offset de temperatura restablecido a 0.0 F.";
    }

    /** F -> C */
    private double fahrenheitToCelsius(double f) {
        return (f - 32.0) * 5.0 / 9.0;
    }

    /**
     * Specific gravity at 25C -> salinity in ppt.
     * SG 1.025 ~ 34.0 ppt  |  factor: (SG - 1.0) * 1000 * 1.36
     */
    private double specificGravityToSalinityPpt(double sg) {
        return (sg - 1.0) * 1000.0 * 1.36;
    }
}
