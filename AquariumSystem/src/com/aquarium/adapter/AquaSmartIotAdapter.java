package com.aquarium.adapter;

import com.aquarium.model.WaterParameters;

import java.util.Random;

/**
 * ADAPTER PATTERN — Adapter for a modern sensor
 *
 * Adapts a modern AquaSmart IoT sensor that returns a JSON-like map
 * into the unified {@link SensorReader} interface.
 * The sensor already speaks metric units, but its API is still incompatible.
 */
public class AquaSmartIotAdapter implements SensorReader {

    private static final String SENSOR_NAME = "AquaSmart IoT Pro";
    private static final Random RANDOM = new Random();

    private final String deviceSerial;
    private boolean calibrated;
    private int readCount;

    public AquaSmartIotAdapter(String deviceSerial) {
        this.deviceSerial = deviceSerial;
        this.calibrated   = true;
        this.readCount    = 0;
    }

    /**
     * Simulates reading from the AquaSmart JSON endpoint.
     * In a real implementation this would call an HTTP/MQTT endpoint.
     */
    @Override
    public WaterParameters readParameters() throws SensorReadException {
        if (!calibrated) {
            throw new SensorReadException(SENSOR_NAME,
                "Sensor no calibrado. Ejecute calibrate() antes de leer.");
        }

        readCount++;

        // Simulate realistic marine tank values with minor random drift
        double temp      = 25.5 + RANDOM.nextGaussian() * 0.4;
        double ph        = 8.15 + RANDOM.nextGaussian() * 0.04;
        double salinity  = 34.0 + RANDOM.nextGaussian() * 0.3;
        double ammonium  = Math.max(0, 0.03 + RANDOM.nextGaussian() * 0.01);
        double nitrites  = Math.max(0, 0.01 + RANDOM.nextGaussian() * 0.005);
        double nitrates  = Math.max(0, 8.0  + RANDOM.nextGaussian() * 1.5);
        double oxygen    = 7.8 + RANDOM.nextGaussian() * 0.2;

        // Every 10 reads, simulate a spike to test alert system
        if (readCount % 10 == 0) {
            ammonium += 0.4;
            temp += 2.5;
        }

        return new WaterParameters(temp, ph, salinity, ammonium, nitrites, nitrates, oxygen);
    }

    @Override
    public boolean isConnected() {
        // 99% uptime simulation
        return RANDOM.nextDouble() > 0.01;
    }

    @Override
    public String getSensorName() { return SENSOR_NAME; }

    @Override
    public String getVersion() { return "AquaSmart-v3.1.2 [SN:" + deviceSerial + "]"; }

    @Override
    public String calibrate() {
        // Simulate 2-second calibration routine
        calibrated = true;
        readCount  = 0;
        boolean probeOk = RANDOM.nextDouble() > 0.02; // 98% success
        if (probeOk) {
            return String.format("AquaSmart IoT calibrado exitosamente. Serial: %s. "
                + "Sondas de pH, O₂ y conductividad ajustadas.", deviceSerial);
        } else {
            calibrated = false;
            return "Error de calibración en sonda de conductividad. Reemplazar electrodo.";
        }
    }
}
