package com.aquarium.bridge;

import com.aquarium.model.MarineAnimal;

/**
 * BRIDGE PATTERN — Concrete Implementor: Drip / Liquid Feeding
 *
 * A peristaltic pump injects liquid phytoplankton or zooplankton
 * directly into the water column. Used for filter-feeders and delicate
 * species like jellyfish and seahorses.
 * Efficiency is very high (~98%) but only suitable for liquid food.
 */
public class DripFeedingMethod implements FeedingMethod {

    private static final double EFFICIENCY = 0.98;

    private final String pumpId;
    private double flowRateMlPerMin;
    private boolean pumpRunning;

    public DripFeedingMethod(String pumpId, double flowRateMlPerMin) {
        this.pumpId           = pumpId;
        this.flowRateMlPerMin = flowRateMlPerMin;
        this.pumpRunning      = false;
    }

    @Override
    public String deliverFood(MarineAnimal animal, double amountKg) {
        pumpRunning = true;

        double effectiveKg = computeEffectiveAmount(amountKg);
        double amountMl    = amountKg * 1_000_000.0 / 1000.0; // kg → mL (density ~1 g/mL)
        double minutesNeeded = amountMl / flowRateMlPerMin;

        String result = animal.feed(effectiveKg);
        pumpRunning = false;

        return String.format("[Bomba Goteo %s] Infundió %.2f mL en %.1f min (%.4f kg efectivo) → %s. → %s",
            pumpId, amountMl, minutesNeeded, effectiveKg, animal.getName(), result);
    }

    @Override
    public String getMethodName() { return "Goteo / Bomba Peristáltica"; }

    @Override
    public double computeEffectiveAmount(double requestedKg) {
        // Minimal loss in tubing
        return requestedKg * EFFICIENCY;
    }

    @Override
    public String getFoodType() { return "Fitoplancton / Zooplancton líquido"; }

    public boolean isPumpRunning()        { return pumpRunning; }
    public double getFlowRateMlPerMin()   { return flowRateMlPerMin; }
    public void setFlowRateMlPerMin(double r){ this.flowRateMlPerMin = r; }
}
