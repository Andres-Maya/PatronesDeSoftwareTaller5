package com.aquarium.model;

import java.util.UUID;

/**
 * Represents a marine animal living in the aquarium.
 */
public class MarineAnimal {

    public enum Species {
        CLOWNFISH("Pez Payaso", "🐠", 26.0, 8.2, 0.5),
        BLUE_TANG("Pez Cirujano Azul", "🐟", 25.5, 8.2, 0.6),
        LIONFISH("Pez León", "🦈", 26.0, 8.1, 0.8),
        SEAHORSE("Caballito de Mar", "🐎", 25.0, 8.2, 0.3),
        OCTOPUS("Pulpo", "🐙", 24.0, 8.0, 0.7),
        SEA_TURTLE("Tortuga Marina", "🐢", 27.0, 8.2, 1.0),
        STARFISH("Estrella de Mar", "⭐", 24.0, 8.1, 0.2),
        JELLYFISH("Medusa", "🪼", 23.0, 8.0, 0.1);

        private final String spanishName;
        private final String icon;
        private final double idealTempC;
        private final double idealPh;
        private final double dailyFoodKg; // daily food in kg

        Species(String spanishName, String icon, double idealTempC, double idealPh, double dailyFoodKg) {
            this.spanishName = spanishName;
            this.icon = icon;
            this.idealTempC = idealTempC;
            this.idealPh = idealPh;
            this.dailyFoodKg = dailyFoodKg;
        }

        public String getSpanishName() { return spanishName; }
        public String getIcon()        { return icon; }
        public double getIdealTempC()  { return idealTempC; }
        public double getIdealPh()     { return idealPh; }
        public double getDailyFoodKg() { return dailyFoodKg; }

        @Override
        public String toString() { return icon + " " + spanishName; }
    }

    public enum HealthStatus {
        HEALTHY("Saludable", new java.awt.Color(50, 200, 100)),
        STRESSED("Estresado", new java.awt.Color(255, 165, 0)),
        SICK("Enfermo", new java.awt.Color(220, 50, 50)),
        RECOVERING("En Recuperación", new java.awt.Color(100, 180, 255));

        private final String label;
        private final java.awt.Color color;

        HealthStatus(String label, java.awt.Color color) {
            this.label = label;
            this.color = color;
        }

        public String getLabel() { return label; }
        public java.awt.Color getColor() { return color; }

        @Override
        public String toString() { return label; }
    }

    private final String id;
    private String name;
    private Species species;
    private double weightKg;
    private int ageMonths;
    private HealthStatus healthStatus;
    private String tankZone;
    private int hungerLevel;   // 0=full, 100=starving
    private int stressLevel;   // 0=calm, 100=panic
    private String notes;

    public MarineAnimal(String name, Species species, double weightKg, int ageMonths, String tankZone) {
        this.id           = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name         = name;
        this.species      = species;
        this.weightKg     = weightKg;
        this.ageMonths    = ageMonths;
        this.tankZone     = tankZone;
        this.healthStatus = HealthStatus.HEALTHY;
        this.hungerLevel  = 20;
        this.stressLevel  = 10;
        this.notes        = "";
    }

    /**
     * Simulates the animal being fed. Reduces hunger; excess feeding increases stress.
     * @param amountKg amount of food given
     * @return feeding result message
     */
    public String feed(double amountKg) {
        double idealAmount = species.getDailyFoodKg();
        if (amountKg <= 0) return name + " no recibió alimento.";

        double ratio = amountKg / idealAmount;
        if (ratio >= 0.8 && ratio <= 1.2) {
            hungerLevel  = Math.max(0, hungerLevel - 60);
            stressLevel  = Math.max(0, stressLevel - 5);
            return name + " comió perfectamente. 🎉";
        } else if (ratio < 0.8) {
            hungerLevel  = Math.max(0, hungerLevel - (int)(ratio * 60));
            stressLevel  = Math.min(100, stressLevel + 10);
            return name + " comió poco. Puede quedarse con hambre.";
        } else {
            hungerLevel  = 0;
            stressLevel  = Math.min(100, stressLevel + 20);
            return name + " comió demasiado. Riesgo de sobrealimentación.";
        }
    }

    /**
     * Updates animal's health status based on water quality and internal condition.
     * @param params current water parameters
     */
    public void evaluateHealthFromWater(WaterParameters params) {
        double tempDiff = Math.abs(params.getTemperatureCelsius() - species.getIdealTempC());
        double phDiff   = Math.abs(params.getPh() - species.getIdealPh());
        int waterScore  = params.computeHealthScore();

        // Accumulate stress from environment
        if (tempDiff > 3 || phDiff > 0.4) stressLevel = Math.min(100, stressLevel + 20);
        if (params.getAmmoniumMgL() > 0.5)  stressLevel = Math.min(100, stressLevel + 30);
        if (waterScore < 40)                 stressLevel = Math.min(100, stressLevel + 25);

        // Reduce stress in good conditions
        if (waterScore >= 85 && tempDiff < 1) stressLevel = Math.max(0, stressLevel - 10);

        // Determine health status from composite indicators
        int risk = stressLevel + hungerLevel + (100 - waterScore);
        if      (risk < 50)  healthStatus = HealthStatus.HEALTHY;
        else if (risk < 100) healthStatus = HealthStatus.STRESSED;
        else if (risk < 160) healthStatus = HealthStatus.SICK;
        else                 healthStatus = HealthStatus.RECOVERING;
    }

    /**
     * Increases hunger level over time (called on each simulated hour).
     * @param hours number of hours since last update
     */
    public void passTime(int hours) {
        hungerLevel = Math.min(100, hungerLevel + hours * 4);
        if (hungerLevel > 70) stressLevel = Math.min(100, stressLevel + hours * 3);
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getId()              { return id; }
    public String getName()            { return name; }
    public Species getSpecies()        { return species; }
    public double getWeightKg()        { return weightKg; }
    public int getAgeMonths()          { return ageMonths; }
    public HealthStatus getHealthStatus() { return healthStatus; }
    public String getTankZone()        { return tankZone; }
    public int getHungerLevel()        { return hungerLevel; }
    public int getStressLevel()        { return stressLevel; }
    public String getNotes()           { return notes; }

    public void setName(String name)           { this.name = name; }
    public void setWeightKg(double weightKg)   { this.weightKg = weightKg; }
    public void setAgeMonths(int ageMonths)    { this.ageMonths = ageMonths; }
    public void setTankZone(String tankZone)   { this.tankZone = tankZone; }
    public void setHealthStatus(HealthStatus h){ this.healthStatus = h; }
    public void setHungerLevel(int v)          { this.hungerLevel = Math.max(0, Math.min(100, v)); }
    public void setStressLevel(int v)          { this.stressLevel = Math.max(0, Math.min(100, v)); }
    public void setNotes(String notes)         { this.notes = notes; }

    @Override
    public String toString() {
        return String.format("[%s] %s %s (%.1f kg) — %s", id, species.getIcon(), name, weightKg, healthStatus);
    }
}
