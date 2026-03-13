package com.aquarium.adapter;

/**
 * Thrown when a sensor device fails to produce a valid reading.
 */
public class SensorReadException extends Exception {

    private final String sensorName;

    public SensorReadException(String sensorName, String message) {
        super(message);
        this.sensorName = sensorName;
    }

    public SensorReadException(String sensorName, String message, Throwable cause) {
        super(message, cause);
        this.sensorName = sensorName;
    }

    public String getSensorName() { return sensorName; }

    @Override
    public String toString() {
        return String.format("SensorReadException [%s]: %s", sensorName, getMessage());
    }
}
