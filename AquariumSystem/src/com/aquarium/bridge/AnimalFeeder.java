package com.aquarium.bridge;

import com.aquarium.model.FeedingRecord;
import com.aquarium.model.MarineAnimal;

/**
 * BRIDGE PATTERN — Abstraction
 *
 * {@code AnimalFeeder} is the high-level feeding controller.
 * It knows *how much* to feed each species (business logic),
 * but delegates the physical delivery to a {@link FeedingMethod} (implementor).
 *
 * This decoupling allows you to swap delivery methods at runtime
 * without changing the feeding logic, or change feeding logic
 * without touching the delivery hardware.
 */
public class AnimalFeeder {

    private FeedingMethod feedingMethod;
    private double portionMultiplier; // e.g., 0.5 = half portion, 2.0 = double

    public AnimalFeeder(FeedingMethod feedingMethod) {
        this.feedingMethod    = feedingMethod;
        this.portionMultiplier = 1.0;
    }

    /**
     * Feeds a single animal using its species-recommended daily portion
     * adjusted by the portion multiplier and custom override.
     *
     * @param animal         the animal to feed
     * @param portionOverride if > 0, overrides the auto-calculated portion (in kg)
     * @return a FeedingRecord capturing the event
     */
    public FeedingRecord feedAnimal(MarineAnimal animal, double portionOverride) {
        double baseAmount = (portionOverride > 0)
            ? portionOverride
            : animal.getSpecies().getDailyFoodKg() * portionMultiplier;

        // Adjust for hunger: if very hungry, give a 20% bonus
        if (animal.getHungerLevel() > 70) {
            baseAmount *= 1.20;
        }

        // If animal is sick, reduce portion to 50% (easier on digestion)
        if (animal.getHealthStatus() == MarineAnimal.HealthStatus.SICK) {
            baseAmount *= 0.50;
        }

        String deliveryReport = feedingMethod.deliverFood(animal, baseAmount);

        return new FeedingRecord(
            animal.getId(),
            animal.getName(),
            feedingMethod.computeEffectiveAmount(baseAmount),
            feedingMethod.getFoodType(),
            deliveryReport
        );
    }

    /**
     * Switches the feeding delivery method at runtime (core Bridge flexibility).
     * @param newMethod the new implementation to delegate to
     */
    public void changeFeedingMethod(FeedingMethod newMethod) {
        this.feedingMethod = newMethod;
    }

    public FeedingMethod getFeedingMethod()         { return feedingMethod; }
    public double getPortionMultiplier()            { return portionMultiplier; }
    public void setPortionMultiplier(double mult)  { this.portionMultiplier = Math.max(0.1, mult); }
}
