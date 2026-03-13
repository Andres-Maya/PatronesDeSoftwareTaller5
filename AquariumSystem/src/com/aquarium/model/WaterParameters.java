package com.aquarium.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable snapshot of water quality parameters captured at a specific moment.
 * All physical and chemical readings are stored together for traceability.
 */
public class WaterParameters {

    private final double temperatureCelsius;
    private final double ph;
    private final double salinityPpt;       // parts per thousand
    private final double ammoniumMgL;       // mg/L - toxic if high
    private final double nitritesMgL;       // mg/L - toxic intermediate
    private final double nitratesMgL;       // mg/L - acceptable in low amounts
    private final double oxygenMgL;         // dissolved oxygen
    private final LocalDateTime timestamp;

    public WaterParameters(double temperatureCelsius, double ph, double salinityPpt,
                           double ammoniumMgL, double nitritesMgL, double nitratesMgL,
                           double oxygenMgL) {
        this.temperatureCelsius = temperatureCelsius;
        this.ph = ph;
        this.salinityPpt = salinityPpt;
        this.ammoniumMgL = ammoniumMgL;
        this.nitritesMgL = nitritesMgL;
        this.nitratesMgL = nitratesMgL;
        this.oxygenMgL = oxygenMgL;
        this.timestamp = LocalDateTime.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public double getTemperatureCelsius() { return temperatureCelsius; }
    public double getPh()                 { return ph; }
    public double getSalinityPpt()        { return salinityPpt; }
    public double getAmmoniumMgL()        { return ammoniumMgL; }
    public double getNitritesMgL()        { return nitritesMgL; }
    public double getNitratesMgL()        { return nitratesMgL; }
    public double getOxygenMgL()          { return oxygenMgL; }
    public LocalDateTime getTimestamp()   { return timestamp; }

    /**
     * Evaluates whether the parameters are within acceptable marine aquarium ranges.
     * Returns a health score from 0 (critical) to 100 (perfect).
     */
    public int computeHealthScore() {
        int score = 100;

        // Temperature: ideal 24–27°C
        if (temperatureCelsius < 22 || temperatureCelsius > 30) score -= 30;
        else if (temperatureCelsius < 24 || temperatureCelsius > 27) score -= 10;

        // pH: ideal 8.1–8.4
        if (ph < 7.8 || ph > 8.6) score -= 25;
        else if (ph < 8.1 || ph > 8.4) score -= 8;

        // Salinity: ideal 33–35 ppt
        if (salinityPpt < 30 || salinityPpt > 38) score -= 20;
        else if (salinityPpt < 33 || salinityPpt > 35) score -= 5;

        // Ammonium: should be ~0
        if (ammoniumMgL > 1.0) score -= 30;
        else if (ammoniumMgL > 0.25) score -= 15;

        // Nitrites: should be ~0
        if (nitritesMgL > 0.5) score -= 20;
        else if (nitritesMgL > 0.1) score -= 10;

        // Nitrates: < 20 mg/L acceptable
        if (nitratesMgL > 50) score -= 15;
        else if (nitratesMgL > 20) score -= 5;

        // Dissolved oxygen: ideal > 7 mg/L
        if (oxygenMgL < 5.0) score -= 25;
        else if (oxygenMgL < 7.0) score -= 8;

        return Math.max(0, score);
    }

    /**
     * Returns a human-readable status label based on health score.
     */
    public String getHealthLabel() {
        int score = computeHealthScore();
        if (score >= 85) return "Óptimo";
        if (score >= 65) return "Aceptable";
        if (score >= 40) return "Alerta";
        return "Crítico";
    }

    @Override
    public String toString() {
        return String.format(
            "Temp: %.1f°C | pH: %.2f | Sal: %.1f‰ | NH₄: %.2f | NO₂: %.2f | NO₃: %.2f | O₂: %.2f — [%s]",
            temperatureCelsius, ph, salinityPpt, ammoniumMgL, nitritesMgL, nitratesMgL, oxygenMgL,
            timestamp.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
        );
    }
}
