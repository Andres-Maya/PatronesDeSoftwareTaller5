package com.aquarium.repository;

import com.aquarium.model.FeedingRecord;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for {@link FeedingRecord} history.
 */
public class FeedingRecordRepository {

    private final List<FeedingRecord> records = new ArrayList<>();

    public void save(FeedingRecord record) {
        records.add(record);
    }

    public List<FeedingRecord> findAll() {
        return Collections.unmodifiableList(records);
    }

    public List<FeedingRecord> findByAnimalId(String animalId) {
        return records.stream()
            .filter(r -> r.getAnimalId().equals(animalId))
            .collect(Collectors.toList());
    }

    /**
     * Returns the last N records across all animals.
     */
    public List<FeedingRecord> findLatest(int limit) {
        int size = records.size();
        if (size == 0) return Collections.emptyList();
        return records.subList(Math.max(0, size - limit), size);
    }

    /**
     * Computes total food delivered (kg) per animal ID.
     */
    public Map<String, Double> getTotalFoodPerAnimal() {
        return records.stream()
            .collect(Collectors.groupingBy(
                FeedingRecord::getAnimalId,
                Collectors.summingDouble(FeedingRecord::getAmountKg)
            ));
    }

    public int count() { return records.size(); }
}
