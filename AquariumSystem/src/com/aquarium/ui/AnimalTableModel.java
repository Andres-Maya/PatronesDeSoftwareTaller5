package com.aquarium.ui;

import com.aquarium.model.MarineAnimal;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom {@link javax.swing.table.TableModel} that backs the animal JTable.
 * Displays key animal attributes with live refresh support.
 */
public class AnimalTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {
        "ID", "Nombre", "Especie", "Peso (kg)", "Estado Salud",
        "Hambre %", "Estrés %", "Zona", "Edad (m)"
    };

    private List<MarineAnimal> animals = new ArrayList<>();

    public void setAnimals(List<MarineAnimal> animals) {
        this.animals = new ArrayList<>(animals);
        fireTableDataChanged();
    }

    @Override public int getRowCount()    { return animals.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }

    @Override
    public Object getValueAt(int row, int col) {
        MarineAnimal a = animals.get(row);
        return switch (col) {
            case 0 -> a.getId();
            case 1 -> a.getName();
            case 2 -> a.getSpecies().toString();
            case 3 -> String.format("%.3f", a.getWeightKg());
            case 4 -> a.getHealthStatus().getLabel();
            case 5 -> a.getHungerLevel();
            case 6 -> a.getStressLevel();
            case 7 -> a.getTankZone();
            case 8 -> a.getAgeMonths();
            default -> "—";
        };
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return switch (col) {
            case 3 -> String.class;
            case 5, 6, 8 -> Integer.class;
            default -> String.class;
        };
    }

    @Override public boolean isCellEditable(int row, int col) { return false; }
}
