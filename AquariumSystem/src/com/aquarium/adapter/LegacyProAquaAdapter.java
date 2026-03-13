package com.aquarium.adapter;

import com.aquarium.model.WaterParameters;

/**
 * ADAPTER PATTERN — Adapter
 *
 * Wraps the incompatible {@link LegacyProAquaSensor} and translates its
 * proprietary raw string output into the {@link SensorReader} interface
 * that the rest of the application understands.
 *
 * Key conversions performed:
 *   - Fahrenheit → Celsius
 *   - Specific gravity (1.000-based) → salinity in ppt (‰)
 */
public class LegacyProAquaAdapter implements SensorReader {

    private static final String SENSOR_NAME = "ProAqua 2000 (Legacy)";

    private final LegacyProAquaSensor legacySensor;

    public LegacyProAquaAdapter(LegacyProAquaSensor legacySensor) {
        this.legacySensor = legacySensor;
    }

    @Override
    public WaterParameters readParameters() throws SensorReadException {
        String raw = legacySensor.getRawDataString();

        if (raw == null || raw.startsWith("ERROR")) {
            throw new SensorReadException(SENSOR_NAME,
                "El sensor legacy respondió con error: " + raw);
        }

        String[] parts = raw.split(";");
        if (parts.length < 7) {
            throw new SensorReadException(SENSOR_NAME,
                "Formato de datos incorrecto. Se esperaban 7 campos, se recibieron " + parts.length);
        }

        try {
            double tempF      = Double.parseDouble(parts[0]);
            double ph         = Double.parseDouble(parts[1]);
            double spGravity  = Double.parseDouble(parts[2]);
            double ammonium   = Double.parseDouble(parts[3]);
            double nitrites   = Double.parseDouble(parts[4]);
            double nitrates   = Double.parseDouble(parts[5]);
            double oxygen     = Double.parseDouble(parts[6]);

            // ── Unit conversions ─────────────────────────────────────────
            double tempC      = fahrenheitToCelsius(tempF);
            double salinityPpt = specificGravityToSalinityPpt(spGravity);

            return new WaterParameters(tempC, ph, salinityPpt, ammonium, nitrites, nitrates, oxygen);

        } catch (NumberFormatException e) {
            throw new SensorReadException(SENSOR_NAME,
                "No se pudo parsear el dato del sensor: " + raw, e);
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
        legacySensor.setCalibrationOffset(0.0); // reset drift
        boolean ok = legacySensor.selfTest();
        return ok
            ? "ProAqua 2000 calibrado correctamente. Offset de temperatura restablecido."
            : "Falla durante la calibración. Revisar conexión del hardware.";
    }

    // ── Private unit conversion helpers ──────────────────────────────────────

    /**
     * Converts Fahrenheit to Celsius.
     */
    private double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }

    /**
     * Converts specific gravity (at 25°C reference) to practical salinity in ppt.
     * Approximation: salinity (ppt) ≈ (SG - 1.000) × 1000 × 1.294 × 20
     * A well-known linear approximation used in marine biology.
     */
    private double specificGravityToSalinityPpt(double sg) {
        // Practical approximation: (SG - 1.0) * 1000 maps linearly to ppt
        // 1.025 SG ≈ 34 ppt; factor ≈ 1.36 per unit of (SG-1)*1000
        return (sg - 1.0) * 1000.0 * 1.36;
    }
}
