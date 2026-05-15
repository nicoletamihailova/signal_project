package com.cardio_generator.outputs;

/**
 * Defines how simulated patient data should be output.
 * Implementations can send the data to the console, files,
 * WebSocket connections, or TCP sockets.
 */
public interface OutputStrategy {
    /**
     * Outputs a single generated patient data entry.
     *
     * @param patientId the patient’s unique ID
     * @param timestamp time the data was generated (in milliseconds since epoch)
     * @param label     the type of data (e.g. heart rate, alert)
     * @param data      the generated value or message
     */
    void output(int patientId, long timestamp, String label, String data);
}
