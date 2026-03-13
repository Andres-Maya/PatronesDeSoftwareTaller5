package com.aquarium.repository;

import com.aquarium.model.MarineAnimal;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory repository for {@link MarineAnimal} entities.
 * Handles CRUD operations and domain-level queries.
 */
public class AnimalRepository {

    private final Map<String, MarineAnimal> store = new LinkedHashMap<>();

    // ── Create / Update / Delete ──────────────────────────────────────────────

    public void save(MarineAnimal animal) {
        store.put(animal.getId(), animal);
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public Optional<MarineAnimal> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<MarineAnimal> findAll() {
        return new ArrayList<>(store.values());
    }

    public List<MarineAnimal> findBySpecies(MarineAnimal.Species species) {
        return store.values().stream()
            .filter(a -> a.getSpecies() == species)
            .collect(Collectors.toList());
    }

    public List<MarineAnimal> findByHealthStatus(MarineAnimal.HealthStatus status) {
        return store.values().stream()
            .filter(a -> a.getHealthStatus() == status)
            .collect(Collectors.toList());
    }

    /**
     * Returns animals whose hunger level exceeds the given threshold.
     */
    public List<MarineAnimal> findHungry(int hungerThreshold) {
        return store.values().stream()
            .filter(a -> a.getHungerLevel() >= hungerThreshold)
            .sorted(Comparator.comparingInt(MarineAnimal::getHungerLevel).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Returns animals sorted by stress level (most stressed first).
     */
    public List<MarineAnimal> findMostStressed() {
        return store.values().stream()
            .sorted(Comparator.comparingInt(MarineAnimal::getStressLevel).reversed())
            .collect(Collectors.toList());
    }

    public int count() { return store.size(); }

    /**
     * Returns a summary of how many animals exist per health status.
     */
    public Map<MarineAnimal.HealthStatus, Long> getHealthSummary() {
        return store.values().stream()
            .collect(Collectors.groupingBy(MarineAnimal::getHealthStatus, Collectors.counting()));
    }
}
