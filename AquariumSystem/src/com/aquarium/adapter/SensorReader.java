package com.aquarium.adapter;

import com.aquarium.model.WaterParameters;

/**
 * ADAPTER PATTERN — Target Interface
 *
 * This is the unified interface that the rest of the application uses
 * to read water data from ANY sensor device, regardless of its vendor
 * or communication protocol.
 *
 * The Adapter pattern allows incompatible sensor APIs to be used
 * interchangeably through this common contract.
 */
public interface SensorReader {

    /**
     * Reads all water quality parameters from the connected sensor.
     * @return a WaterParameters snapshot, never null
     * @throws SensorReadException if the sensor fails to respond
     */
    WaterParameters readParameters() throws SensorReadException;

    /**
     * Checks whether the sensor hardware is connected and operational.
     * @return true if the sensor responds to a ping
     */
    boolean isConnected();

    /**
     * Returns the human-readable name of the sensor device.
     */
    String getSensorName();

    /**
     * Returns the firmware or protocol version reported by the device.
     */
    String getVersion();

    /**
     * Performs a self-calibration routine on the sensor.
     * @return a summary report of the calibration result
     */
    String calibrate();
}
