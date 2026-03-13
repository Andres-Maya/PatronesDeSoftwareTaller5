package com.aquarium.bridge;

import com.aquarium.model.MarineAnimal;

/**
 * BRIDGE PATTERN — Implementor Interface
 *
 * Defines how food is physically delivered to an animal.
 * The Bridge pattern decouples the feeding *strategy* (what and how much
 * to feed) from the *delivery mechanism* (manual, automatic, drip).
 *
 * This allows any animal type to be combined with any delivery method
 * without creating an exponential number of subclasses.
 */
public interface FeedingMethod {

    /**
     * Delivers food to the given animal.
     *
     * @param animal    the recipient animal
     * @param amountKg  the amount of food in kilograms
     * @return a human-readable delivery report
     */
    String deliverFood(MarineAnimal animal, double amountKg);

    /**
     * Returns the name of this delivery method.
     */
    String getMethodName();

    /**
     * Calculates the actual amount delivered, factoring in method efficiency.
     * Some methods lose food to water current or evaporation.
     *
     * @param requestedKg the intended amount
     * @return the amount that actually reaches the animal
     */
    double computeEffectiveAmount(double requestedKg);

    /**
     * Returns a description of what food type this method delivers.
     */
    String getFoodType();
}
