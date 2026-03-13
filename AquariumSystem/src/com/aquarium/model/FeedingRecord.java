package com.aquarium.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable log entry that records a feeding event for a specific animal.
 */
public class FeedingRecord {

    private final String animalId;
    private final String animalName;
    private final double amountKg;
    private final String foodType;
    private final String result;
    private final LocalDateTime timestamp;

    public FeedingRecord(String animalId, String animalName, double amountKg,
                         String foodType, String result) {
        this.animalId   = animalId;
        this.animalName = animalName;
        this.amountKg   = amountKg;
        this.foodType   = foodType;
        this.result     = result;
        this.timestamp  = LocalDateTime.now();
    }

    public String getAnimalId()   { return animalId; }
    public String getAnimalName() { return animalName; }
    public double getAmountKg()   { return amountKg; }
    public String getFoodType()   { return foodType; }
    public String getResult()     { return result; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s | %s recibió %.3f kg de %s → %s",
            timestamp.format(DateTimeFormatter.ofPattern("dd/MM HH:mm")),
            animalName, amountKg, foodType, result);
    }
}
