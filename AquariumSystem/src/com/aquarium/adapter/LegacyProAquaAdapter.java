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
 *   - Fahrenheit to Celsius
 *   - Specific gravity (1.000-based) to salinity in ppt
 *
 * Resilience: retries up to 3 times on transient hardware failures
 * before propagating a SensorReadException.
 */
public class LegacyProAquaAdapter implements SensorReader {

    private static final String SENSOR_NAME  = "ProAqua 2000 (Legacy)";
    private static final int    MAX_ATTEMPTS = 3;

    private final LegacyProAquaSensor legacySensor;

    public LegacyProAquaAdapter(LegacyProAquaSensor legacySensor) {
        this.legacySensor = legacySensor;
    }

    /**
     * Reads parameters with up to MAX_ATTEMPTS retries to tolerate
     * transient legacy hardware glitches (loose connections, voltage dips).
     */
    @Override
    public WaterParameters readParameters() throws SensorReadException {
        SensorReadException lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return tryRead();
            } catch (SensorReadException e) {
                lastException = e;
                System.out.printf("[%s] Intento %d/%d fallido: %s%n",
                    SENSOR_NAME, attempt, MAX_ATTEMPTS, e.getMessage());
            }
        }

        throw new SensorReadException(SENSOR_NAME,
            "Fallo tras " + MAX_ATTEMPTS + " intentos. Ultimo error: "
            + (lastException != null ? lastException.getMessage() : "desconocido"),
            lastException);
    }

    /**
     * Single read attempt: fetches raw string, validates structure,
     * runs sanity checks, then converts units.
     */
    private WaterParameters tryRead() throws SensorReadException {
        String raw = legacySensor.getRawDataString();

        if (raw == null || raw.isBlank()) {
            throw new SensorReadException(SENSOR_NAME, "El sensor no devolvio datos.");
        }
        if (raw.startsWith("ERROR")) {
            throw new SensorReadException(SENSOR_NAME,
                "El sensor respondio con codigo de error: " + raw);
        }

        String[] parts = raw.split(";");
        if (parts.length < 7) {
            throw new SensorReadException(SENSOR_NAME,
                "Formato incorrecto: se esperaban 7 campos, se recibieron "
                + parts.length + ". Trama: [" + raw + "]");
        }

        try {
            double tempF     = Double.parseDouble(parts[0].trim());
            double ph        = Double.parseDouble(parts[1].trim());
            double spGravity = Double.parseDouble(parts[2].trim());
            double ammonium  = Double.parseDouble(parts[3].trim());
            double nitrites  = Double.parseDouble(parts[4].trim());
            double nitrates  = Double.parseDouble(parts[5].trim());
            double oxygen    = Double.parseDouble(parts[6].trim());

            // Sanity checks: reject physically impossible readings
            if (tempF < 32.0 || tempF > 120.0)
                throw new SensorReadException(SENSOR_NAME,
                    "Temperatura fuera de rango: " + tempF + " F");
            if (ph < 0.0 || ph > 14.0)
                throw new SensorReadException(SENSOR_NAME, "pH invalido: " + ph);
            if (spGravity < 1.000 || spGravity > 1.040)
                throw new SensorReadException(SENSOR_NAME,
                    "Gravedad especifica fuera de rango: " + spGravity);
            if (oxygen < 0.0 || oxygen > 20.0)
                throw new SensorReadException(SENSOR_NAME,
                    "Oxigeno disuelto fuera de rango: " + oxygen + " mg/L");

            // Unit conversions
            double tempC       = fahrenheitToCelsius(tempF);
            double salinityPpt = specificGravityToSalinityPpt(spGravity);

            // Clamp noise-induced negatives
            ammonium = Math.max(0.0, ammonium);
            nitrites = Math.max(0.0, nitrites);
            nitrates = Math.max(0.0, nitrates);

            return new WaterParameters(tempC, ph, salinityPpt, ammonium, nitrites, nitrates, oxygen);

        } catch (NumberFormatException e) {
            throw new SensorReadException(SENSOR_NAME,
                "No se pudo parsear valor numerico en: [" + raw + "]", e);
        }
    }

    /**
     * Checks device health independently from reading.
     * A failed selfTest does NOT block a read attempt.
     */
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
        boolean ok = legacySensor.selfTest();
        return ok
            ? "ProAqua 2000 calibrado. Offset de temperatura restablecido a 0.0."
            : "Falla en calibracion. Verificar conexion USB y alimentacion.";
    }

    // ── Unit-conversion helpers ───────────────────────────────────────────────

    /** Converts Fahrenheit to Celsius: C = (F - 32) x 5/9 */
    private double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }

    /**
     * Converts specific gravity at 25C to practical salinity in ppt.
     * SG 1.025 = ~34 ppt. Factor: (SG - 1.0) * 1000 * 1.36
     */
    private double specificGravityToSalinityPpt(double sg) {
        return (sg - 1.0) * 1000.0 * 1.36;
    }
}
