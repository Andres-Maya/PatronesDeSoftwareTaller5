package com.aquarium.service;

import com.aquarium.adapter.SensorReadException;
import com.aquarium.adapter.SensorReader;
import com.aquarium.bridge.AnimalFeeder;
import com.aquarium.bridge.FeedingMethod;
import com.aquarium.model.FeedingRecord;
import com.aquarium.model.MarineAnimal;
import com.aquarium.model.WaterParameters;
import com.aquarium.repository.AnimalRepository;
import com.aquarium.repository.FeedingRecordRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Central service that orchestrates all aquarium operations.
 * Coordinates sensor reading, animal health evaluation, and feeding.
 */
public class AquariumService {

    private final AnimalRepository animalRepo;
    private final FeedingRecordRepository feedingRepo;
    private final AnimalFeeder feeder;

    private SensorReader activeSensor;
    private WaterParameters lastMeasurement;
    private final List<String> alertLog = new ArrayList<>();

    public AquariumService(AnimalRepository animalRepo,
                           FeedingRecordRepository feedingRepo,
                           SensorReader defaultSensor,
                           FeedingMethod defaultFeedingMethod) {
        this.animalRepo   = animalRepo;
        this.feedingRepo  = feedingRepo;
        this.activeSensor = defaultSensor;
        this.feeder       = new AnimalFeeder(defaultFeedingMethod);
    }

    // ── Sensor Operations ─────────────────────────────────────────────────────

    /**
     * Reads water parameters from the active sensor, evaluates alerts,
     * and updates every animal's health based on current water quality.
     *
     * @return the measurement, or null if the sensor failed
     */
    public WaterParameters takeMeasurement() {
        try {
            WaterParameters params = activeSensor.readParameters();
            this.lastMeasurement = params;
            evaluateWaterAlerts(params);
            updateAllAnimalHealth(params);
            return params;
        } catch (SensorReadException e) {
            alertLog.add("⚠ ERROR SENSOR [" + e.getSensorName() + "]: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates alert messages if any parameter is out of safe range.
     */
    private void evaluateWaterAlerts(WaterParameters p) {
        alertLog.clear();
        if (p.getTemperatureCelsius() > 29)
            alertLog.add("🔥 TEMPERATURA ALTA: " + String.format("%.1f°C", p.getTemperatureCelsius()));
        if (p.getTemperatureCelsius() < 22)
            alertLog.add("🧊 TEMPERATURA BAJA: " + String.format("%.1f°C", p.getTemperatureCelsius()));
        if (p.getPh() < 7.9 || p.getPh() > 8.5)
            alertLog.add("⚗ pH FUERA DE RANGO: " + String.format("%.2f", p.getPh()));
        if (p.getAmmoniumMgL() > 0.25)
            alertLog.add("☠ AMONIACO ELEVADO: " + String.format("%.3f mg/L", p.getAmmoniumMgL()));
        if (p.getNitritesMgL() > 0.1)
            alertLog.add("⚡ NITRITOS ELEVADOS: " + String.format("%.3f mg/L", p.getNitritesMgL()));
        if (p.getOxygenMgL() < 6.0)
            alertLog.add("💨 OXÍGENO BAJO: " + String.format("%.2f mg/L", p.getOxygenMgL()));
        if (p.computeHealthScore() < 50)
            alertLog.add("🚨 CALIDAD DEL AGUA CRÍTICA — Puntaje: " + p.computeHealthScore());
    }

    /**
     * Passes current water conditions to every registered animal for health re-evaluation.
     */
    private void updateAllAnimalHealth(WaterParameters params) {
        for (MarineAnimal animal : animalRepo.findAll()) {
            animal.evaluateHealthFromWater(params);
        }
    }

    // ── Feeding Operations ────────────────────────────────────────────────────

    /**
     * Feeds a specific animal. Saves the record.
     *
     * @param animalId       target animal
     * @param portionOverride custom amount in kg, or 0 to use recommended
     * @return feeding record or error message
     */
    public String feedAnimal(String animalId, double portionOverride) {
        Optional<MarineAnimal> opt = animalRepo.findById(animalId);
        if (opt.isEmpty()) return "Animal con ID " + animalId + " no encontrado.";

        FeedingRecord record = feeder.feedAnimal(opt.get(), portionOverride);
        feedingRepo.save(record);
        return record.toString();
    }

    /**
     * Feeds all hungry animals (hunger >= 50) using recommended portions.
     * @return list of feeding reports
     */
    public List<String> feedAllHungry() {
        List<MarineAnimal> hungry = animalRepo.findHungry(50);
        List<String> reports = new ArrayList<>();
        for (MarineAnimal animal : hungry) {
            FeedingRecord record = feeder.feedAnimal(animal, 0);
            feedingRepo.save(record);
            reports.add(record.toString());
        }
        if (reports.isEmpty()) reports.add("Ningún animal tiene hambre en este momento.");
        return reports;
    }

    // ── Animal CRUD ───────────────────────────────────────────────────────────

    public void addAnimal(MarineAnimal animal)    { animalRepo.save(animal); }
    public boolean removeAnimal(String id)        { return animalRepo.delete(id); }
    public List<MarineAnimal> getAllAnimals()      { return animalRepo.findAll(); }
    public Optional<MarineAnimal> getAnimal(String id) { return animalRepo.findById(id); }

    /**
     * Simulates time passing for all animals (increases hunger/stress).
     * @param hours simulated hours to advance
     */
    public void advanceTime(int hours) {
        animalRepo.findAll().forEach(a -> a.passTime(hours));
    }

    // ── Sensor Switching (Adapter pattern in action) ──────────────────────────

    public void switchSensor(SensorReader newSensor) { this.activeSensor = newSensor; }
    public SensorReader getActiveSensor()            { return activeSensor; }

    // ── Feeding Method Switching (Bridge pattern in action) ───────────────────

    public void switchFeedingMethod(FeedingMethod method) { feeder.changeFeedingMethod(method); }
    public AnimalFeeder getFeeder()                       { return feeder; }

    // ── Reports & Accessors ───────────────────────────────────────────────────

    public WaterParameters getLastMeasurement()    { return lastMeasurement; }
    public List<String> getAlertLog()              { return new ArrayList<>(alertLog); }
    public List<FeedingRecord> getFeedingHistory() { return feedingRepo.findAll(); }
    public AnimalRepository getAnimalRepo()        { return animalRepo; }

    /**
     * Generates a plain-text summary report of the aquarium's current state.
     */
    public String generateSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("══════════════════════════════════════════\n");
        sb.append("   REPORTE DEL ACUARIO\n");
        sb.append("══════════════════════════════════════════\n");
        sb.append("Sensor activo : ").append(activeSensor.getSensorName()).append("\n");
        sb.append("Método aliment.: ").append(feeder.getFeedingMethod().getMethodName()).append("\n");
        sb.append("Animales reg.  : ").append(animalRepo.count()).append("\n");
        sb.append("Alimentaciones : ").append(feedingRepo.count()).append("\n\n");

        if (lastMeasurement != null) {
            sb.append("AGUA → ").append(lastMeasurement).append("\n");
            sb.append("Salud del agua : ").append(lastMeasurement.getHealthLabel())
              .append(" (").append(lastMeasurement.computeHealthScore()).append("/100)\n\n");
        }

        sb.append("RESUMEN SALUD ANIMALES:\n");
        animalRepo.getHealthSummary().forEach((status, count) ->
            sb.append("  ").append(status.getLabel()).append(": ").append(count).append("\n"));

        if (!alertLog.isEmpty()) {
            sb.append("\nALERTAS ACTIVAS:\n");
            alertLog.forEach(a -> sb.append("  ").append(a).append("\n"));
        }

        return sb.toString();
    }
}
