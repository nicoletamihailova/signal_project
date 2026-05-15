package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

import java.util.Random;

/**
 * Generates simulated alert status data for patients.
 * It tracks whether each patient currently has an active alert and randomly
 * triggers or clears alerts over time.
 *
 * <p>Alerts can be either "triggered" or "resolved".
 */
public class AlertGenerator implements PatientDataGenerator {
    // changed constant name to UPPER_SNAKE_CASE according to google Java Style Guide
    public static final Random RANDOM_GENERATOR = new Random();
    // changed field name to lowerCamelCase according to google Java Style Guide
    private boolean[] alertStates; // false = resolved, true = pressed

    /**
     * Creates an alert generator for a number of patients.
     * Each patient has a stored alert state using its ID as an index.
     *
     * @param patientCount the number of patients to generate alerts for
     */
    public AlertGenerator(int patientCount) {

        alertStates = new boolean[patientCount + 1];
    }

    /**
     * Generates an alert update for a patient and sends it to the output strategy.
     *
     * <p>If an alert is active, there is a high chance it will be resolved.
     * Otherwise, a new alert may be triggered based on a probability value.
     *
     * @param patientId      the patient’s unique ID
     * @param outputStrategy where the alert data is sent
     */
    @Override
    public void generate(int patientId, OutputStrategy outputStrategy) {
        try {
            if (alertStates[patientId]) {
                if (RANDOM_GENERATOR.nextDouble() < 0.9) { // 90% chance to resolve
                    alertStates[patientId] = false;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "resolved");
                }
            } else {
                // changed variable name Lambda to lower camel case, because it's a variable name
                // according to google Java Style Guide
                double lambda = 0.1; // Average rate (alerts per period), adjust based on desired frequency
                double p = -Math.expm1(-lambda); // Probability of at least one alert in the period
                boolean alertTriggered = RANDOM_GENERATOR.nextDouble() < p;

                if (alertTriggered) {
                    alertStates[patientId] = true;
                    // Output the alert
                    outputStrategy.output(patientId, System.currentTimeMillis(), "Alert", "triggered");
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred while generating alert data for patient " + patientId);
            e.printStackTrace();
        }
    }
}
