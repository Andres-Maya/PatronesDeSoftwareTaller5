package com.aquarium.bridge;

import com.aquarium.model.MarineAnimal;

/**
 * BRIDGE PATTERN — Concrete Implementor: Automatic Dispenser
 *
 * A motorized drum dispenser releases dry pellets on a timer.
 * Consistent volume delivery, but ~15% of pellets dissolve before consumed
 * and efficiency degrades if the drum humidity is high.
 */
public class AutomaticDispenserMethod implements FeedingMethod {

    private static final double BASE_EFFICIENCY = 0.85;

    private final String dispenserId;
    private int totalDispenses;
    private double humidityPercent; // affects pellet quality

    public AutomaticDispenserMethod(String dispenserId, double humidityPercent) {
        this.dispenserId    = dispenserId;
        this.totalDispenses = 0;
        this.humidityPercent = Math.max(0, Math.min(100, humidityPercent));
    }

    @Override
    public String deliverFood(MarineAnimal animal, double amountKg) {
        totalDispenses++;
        double effective = computeEffectiveAmount(amountKg);
        String result = animal.feed(effective);
        return String.format("[Dispensador %s] Ciclo #%d: %.4f kg de pellets → %s. → %s",
            dispenserId, totalDispenses, effective, animal.getName(), result);
    }

    @Override
    public String getMethodName() { return "Dispensador Automático"; }

    @Override
    public double computeEffectiveAmount(double requestedKg) {
        // High humidity degrades pellets: each 10% humidity over 60 reduces efficiency by 5%
        double humidityPenalty = humidityPercent > 60 ? ((humidityPercent - 60) / 10.0) * 0.05 : 0;
        double efficiency = Math.max(0.5, BASE_EFFICIENCY - humidityPenalty);
        return requestedKg * efficiency;
    }

    @Override
    public String getFoodType() { return "Pellets secos"; }

    public int getTotalDispenses()         { return totalDispenses; }
    public void setHumidityPercent(double h){ this.humidityPercent = h; }
}
