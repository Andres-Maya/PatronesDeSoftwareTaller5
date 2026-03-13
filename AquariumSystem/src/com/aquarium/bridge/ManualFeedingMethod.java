package com.aquarium.bridge;

import com.aquarium.model.MarineAnimal;

/**
 * BRIDGE PATTERN — Concrete Implementor: Manual Feeding
 *
 * A keeper manually drops food into the tank using feeding tongs or their hand.
 * High precision but labor-intensive. Efficiency ~95% (minimal waste).
 */
public class ManualFeedingMethod implements FeedingMethod {

    private static final double EFFICIENCY = 0.95;
    private final String keeperName;

    public ManualFeedingMethod(String keeperName) {
        this.keeperName = keeperName;
    }

    @Override
    public String deliverFood(MarineAnimal animal, double amountKg) {
        double effective = computeEffectiveAmount(amountKg);
        String result = animal.feed(effective);
        return String.format("[Manual – %s] Entregó %.4f kg (de %.4f solicitados) a %s. → %s",
            keeperName, effective, amountKg, animal.getName(), result);
    }

    @Override
    public String getMethodName() { return "Alimentación Manual"; }

    @Override
    public double computeEffectiveAmount(double requestedKg) {
        // Small random human error: ±2%
        double humanError = 1.0 + (Math.random() * 0.04 - 0.02);
        return requestedKg * EFFICIENCY * humanError;
    }

    @Override
    public String getFoodType() { return "Alimento fresco / congelado"; }
}
