package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Represents the basic behavior for classes that generate simulated
 * patient health data. Each implementation is responsible for creating
 * a specific type of data and sending it to the chosen output method.
 */
public interface PatientDataGenerator {
    /**
     * Generates simulated data for a patient and sends it to the selected output method.
     *
     * @param patientId      the patient's unique ID
     * @param outputStrategy the output method for the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
