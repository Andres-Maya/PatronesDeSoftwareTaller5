package com.aquarium.util;

import com.aquarium.model.MarineAnimal;
import com.aquarium.model.MarineAnimal.Species;
import com.aquarium.service.AquariumService;

/**
 * Populates the aquarium with realistic demo animals on startup.
 */
public class DataSeeder {

    private DataSeeder() {}

    public static void seedAnimals(AquariumService service) {
        service.addAnimal(new MarineAnimal("Nemo",     Species.CLOWNFISH,  0.05,  18, "Zona Arrecife"));
        service.addAnimal(new MarineAnimal("Dory",     Species.BLUE_TANG,  0.15,  36, "Zona Arrecife"));
        service.addAnimal(new MarineAnimal("Leo",      Species.LIONFISH,   0.40,  24, "Zona Profunda"));
        service.addAnimal(new MarineAnimal("Poseidón", Species.SEAHORSE,   0.02,   8, "Zona Arrecife"));
        service.addAnimal(new MarineAnimal("Kraken",   Species.OCTOPUS,    1.80,  60, "Zona Profunda"));
        service.addAnimal(new MarineAnimal("Marina",   Species.SEA_TURTLE, 45.00, 120, "Zona Principal"));
        service.addAnimal(new MarineAnimal("Stella",   Species.STARFISH,   0.10,  12, "Zona Arrecife"));
        service.addAnimal(new MarineAnimal("Aurelia",  Species.JELLYFISH,  0.03,   6, "Zona Medusas"));
    }
}
