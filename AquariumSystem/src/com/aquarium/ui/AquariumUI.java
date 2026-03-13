package com.aquarium.ui;

import com.aquarium.adapter.AquaSmartIotAdapter;
import com.aquarium.adapter.LegacyProAquaAdapter;
import com.aquarium.adapter.LegacyProAquaSensor;
import com.aquarium.adapter.SensorReader;
import com.aquarium.bridge.*;
import com.aquarium.model.MarineAnimal;
import com.aquarium.model.WaterParameters;
import com.aquarium.repository.AnimalRepository;
import com.aquarium.repository.FeedingRecordRepository;
import com.aquarium.service.AquariumService;
import com.aquarium.util.DataSeeder;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main Swing UI for the Marine Aquarium Management System.
 * Deep-ocean dark aesthetic with bioluminescent accent colors.
 */
public class AquariumUI extends JFrame {

    // ── Color palette: abyssal ocean ─────────────────────────────────────────
    private static final Color BG_DEEP      = new Color(4, 12, 28);
    private static final Color BG_PANEL     = new Color(8, 22, 48);
    private static final Color BG_CARD      = new Color(12, 32, 64);
    private static final Color ACCENT_CYAN  = new Color(0, 210, 230);
    private static final Color ACCENT_TEAL  = new Color(0, 168, 180);
    private static final Color ACCENT_GLOW  = new Color(0, 255, 220);
    private static final Color TEXT_PRIMARY = new Color(210, 240, 255);
    private static final Color TEXT_DIM     = new Color(110, 160, 200);
    private static final Color ALERT_RED    = new Color(255, 80, 80);
    private static final Color ALERT_ORANGE = new Color(255, 160, 40);
    private static final Color OK_GREEN     = new Color(60, 210, 120);

    // ── Fonts ────────────────────────────────────────────────────────────────
    private static final Font FONT_TITLE  = new Font("Monospaced", Font.BOLD, 22);
    private static final Font FONT_HEADER = new Font("Monospaced", Font.BOLD, 13);
    private static final Font FONT_BODY   = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font FONT_SMALL  = new Font("Monospaced", Font.PLAIN, 11);

    // ── Service ──────────────────────────────────────────────────────────────
    private final AquariumService service;

    // ── UI Components ────────────────────────────────────────────────────────
    private JTable animalTable;
    private AnimalTableModel tableModel;
    private JTextArea logArea;
    private JLabel waterStatusLabel;
    private JLabel healthScoreLabel;
    private JLabel sensorNameLabel;
    private JLabel feedMethodLabel;
    private JProgressBar tempBar, phBar, salinityBar, o2Bar, nh4Bar;
    private JLabel tempLabel, phLabel, salinityLabel, o2Label, nh4Label;
    private JPanel alertPanel;
    private JLabel alertTextLabel;
    private Timer autoRefreshTimer;

    // ── Sensor options (Adapter pattern) ─────────────────────────────────────
    private final SensorReader sensorIot    = new AquaSmartIotAdapter("SN-2024-001");
    private final SensorReader sensorLegacy = new LegacyProAquaAdapter(new LegacyProAquaSensor());

    // ── Feeding method options (Bridge pattern) ───────────────────────────────
    private final FeedingMethod feedManual     = new ManualFeedingMethod("Carlos Ruíz");
    private final FeedingMethod feedDispenser  = new AutomaticDispenserMethod("DISP-A1", 45.0);
    private final FeedingMethod feedDrip       = new DripFeedingMethod("PUMP-B2", 25.0);

    // ─────────────────────────────────────────────────────────────────────────

    public AquariumUI() {
        service = new AquariumService(
            new AnimalRepository(),
            new FeedingRecordRepository(),
            sensorIot,
            feedManual
        );
        DataSeeder.seedAnimals(service);

        initFrame();
        buildUI();
        startAutoRefresh();
        refreshAll();
    }

    // ── Frame setup ───────────────────────────────────────────────────────────

    private void initFrame() {
        setTitle("🌊 Sistema de Gestión — Acuario Marino");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_DEEP);
    }

    // ── Main layout construction ──────────────────────────────────────────────

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),      BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildStatusBar(),   BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_PANEL);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT_TEAL));
        header.setPreferredSize(new Dimension(0, 70));

        // Title
        JLabel title = new JLabel("  🌊 ACUARIO MARINO — SISTEMA DE CONTROL", JLabel.LEFT);
        title.setFont(FONT_TITLE);
        title.setForeground(ACCENT_GLOW);
        header.add(title, BorderLayout.WEST);

        // Alert banner
        alertPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 18));
        alertPanel.setBackground(BG_PANEL);
        alertTextLabel = new JLabel("Sistema listo.");
        alertTextLabel.setFont(FONT_HEADER);
        alertTextLabel.setForeground(OK_GREEN);
        alertPanel.add(alertTextLabel);
        header.add(alertPanel, BorderLayout.EAST);

        return header;
    }

    // ── Center: three-column layout ───────────────────────────────────────────

    private JPanel buildCenterPanel() {
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBackground(BG_DEEP);
        center.setBorder(new EmptyBorder(8, 8, 8, 8));

        JSplitPane splitH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildLeftPanel(), buildRightPanel());
        splitH.setDividerLocation(620);
        splitH.setBackground(BG_DEEP);
        splitH.setBorder(null);
        splitH.setDividerSize(4);

        center.add(splitH, BorderLayout.CENTER);
        return center;
    }

    // ── Left panel: animal table + action buttons ─────────────────────────────

    private JPanel buildLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_DEEP);

        panel.add(buildAnimalTablePanel(), BorderLayout.CENTER);
        panel.add(buildActionBar(),        BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildAnimalTablePanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(createCardBorder("🐠  ANIMALES MARINOS REGISTRADOS"));

        tableModel = new AnimalTableModel();
        animalTable = new JTable(tableModel);
        styleTable(animalTable);

        JScrollPane scroll = new JScrollPane(animalTable);
        styleScroll(scroll);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        bar.setBackground(BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT_TEAL));

        bar.add(makeButton("📡 Tomar Medición",    ACCENT_TEAL,  e -> takeMeasurement()));
        bar.add(makeButton("➕ Agregar Animal",    ACCENT_CYAN,  e -> showAddAnimalDialog()));
        bar.add(makeButton("🗑 Eliminar",           ALERT_RED,    e -> deleteSelectedAnimal()));
        bar.add(makeButton("🍽 Alimentar",           OK_GREEN,     e -> feedSelectedAnimal()));
        bar.add(makeButton("🍽🍽 Alimentar Todos",   OK_GREEN,     e -> feedAllHungry()));
        bar.add(makeButton("⏩ +3 Horas",            ALERT_ORANGE, e -> { service.advanceTime(3); refreshAll(); }));
        return bar;
    }

    // ── Right panel: sensor gauges + config + log ─────────────────────────────

    private JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_DEEP);

        JSplitPane splitV = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildTopRight(), buildLogPanel());
        splitV.setDividerLocation(420);
        splitV.setBackground(BG_DEEP);
        splitV.setBorder(null);
        splitV.setDividerSize(4);

        panel.add(splitV, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTopRight() {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setBackground(BG_DEEP);
        p.add(buildWaterQualityPanel());
        p.add(buildConfigPanel());
        return p;
    }

    // ── Water quality gauges ──────────────────────────────────────────────────

    private JPanel buildWaterQualityPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(BG_CARD);
        p.setBorder(createCardBorder("💧  CALIDAD DEL AGUA"));

        // Status labels row
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 4));
        statusRow.setBackground(BG_CARD);
        waterStatusLabel = makeLabel("—", FONT_HEADER, ACCENT_CYAN);
        healthScoreLabel = makeLabel("— / 100", FONT_HEADER, OK_GREEN);
        sensorNameLabel  = makeLabel("Sensor: —", FONT_SMALL, TEXT_DIM);
        statusRow.add(makeLabel("Estado: ", FONT_BODY, TEXT_DIM));
        statusRow.add(waterStatusLabel);
        statusRow.add(makeLabel("  Puntaje: ", FONT_BODY, TEXT_DIM));
        statusRow.add(healthScoreLabel);
        p.add(statusRow, BorderLayout.NORTH);

        // Gauge bars
        JPanel gauges = new JPanel(new GridLayout(5, 1, 0, 6));
        gauges.setBackground(BG_CARD);
        gauges.setBorder(new EmptyBorder(6, 10, 6, 10));

        tempBar     = makeGaugeBar(0, 40);   tempLabel     = makeLabel("Temp: —°C",  FONT_BODY, TEXT_PRIMARY);
        phBar       = makeGaugeBar(0, 100);  phLabel       = makeLabel("pH: —",       FONT_BODY, TEXT_PRIMARY);
        salinityBar = makeGaugeBar(0, 50);   salinityLabel = makeLabel("Sal: — ‰",   FONT_BODY, TEXT_PRIMARY);
        o2Bar       = makeGaugeBar(0, 15);   o2Label       = makeLabel("O₂: — mg/L", FONT_BODY, TEXT_PRIMARY);
        nh4Bar      = makeGaugeBar(0, 3);    nh4Label      = makeLabel("NH₄: — mg/L",FONT_BODY, TEXT_PRIMARY);

        gauges.add(buildGaugeRow(tempLabel,     tempBar));
        gauges.add(buildGaugeRow(phLabel,       phBar));
        gauges.add(buildGaugeRow(salinityLabel, salinityBar));
        gauges.add(buildGaugeRow(o2Label,       o2Bar));
        gauges.add(buildGaugeRow(nh4Label,      nh4Bar));

        p.add(gauges, BorderLayout.CENTER);

        // South area: measurement button + sensor name stacked vertically
        JPanel southArea = new JPanel(new BorderLayout(0, 2));
        southArea.setBackground(BG_CARD);
        southArea.setBorder(new EmptyBorder(4, 10, 6, 10));

        JButton measureBtn = makeButton("📡 Tomar Medición", ACCENT_TEAL, e -> takeMeasurement());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnRow.setBackground(BG_CARD);
        btnRow.add(measureBtn);

        southArea.add(btnRow,          BorderLayout.CENTER);
        southArea.add(sensorNameLabel, BorderLayout.SOUTH);

        p.add(southArea, BorderLayout.SOUTH);

        return p;
    }

    private JPanel buildGaugeRow(JLabel label, JProgressBar bar) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(BG_CARD);
        label.setPreferredSize(new Dimension(130, 18));
        row.add(label, BorderLayout.WEST);
        row.add(bar,   BorderLayout.CENTER);
        return row;
    }

    // ── Config panel: sensor & feeding method selector ─────────────────────────

    private JPanel buildConfigPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(BG_CARD);
        p.setBorder(createCardBorder("⚙  CONFIGURACIÓN DEL SISTEMA"));

        JPanel inner = new JPanel(new GridLayout(0, 1, 0, 10));
        inner.setBackground(BG_CARD);
        inner.setBorder(new EmptyBorder(10, 12, 10, 12));

        // ── Sensor selector ───────────────────────────────────────────────
        inner.add(makeLabel("SENSOR DE AGUA (Patrón Adapter):", FONT_HEADER, ACCENT_CYAN));
        JComboBox<String> sensorCombo = new JComboBox<>(new String[]{
            "AquaSmart IoT Pro (Moderno)", "ProAqua 2000 (Legacy)"
        });
        styleCombBox(sensorCombo);
        sensorCombo.addActionListener(e -> {
            if (sensorCombo.getSelectedIndex() == 0) service.switchSensor(sensorIot);
            else                                      service.switchSensor(sensorLegacy);
            log("Sensor cambiado a: " + sensorCombo.getSelectedItem());
        });
        inner.add(sensorCombo);

        inner.add(makeLabel("↑ Adapter traduce formatos incompatibles", FONT_SMALL, TEXT_DIM));

        // ── Feeding method selector ───────────────────────────────────────
        inner.add(Box.createVerticalStrut(8));
        inner.add(makeLabel("MÉTODO DE ALIMENTACIÓN (Patrón Bridge):", FONT_HEADER, ACCENT_CYAN));
        JComboBox<String> feedCombo = new JComboBox<>(new String[]{
            "Manual (Carlos Ruíz)", "Dispensador Automático", "Goteo / Bomba Peristáltica"
        });
        styleCombBox(feedCombo);
        feedMethodLabel = makeLabel("Manual (Carlos Ruíz)", FONT_SMALL, ACCENT_GLOW);
        feedCombo.addActionListener(e -> {
            switch (feedCombo.getSelectedIndex()) {
                case 0 -> service.switchFeedingMethod(feedManual);
                case 1 -> service.switchFeedingMethod(feedDispenser);
                case 2 -> service.switchFeedingMethod(feedDrip);
            }
            feedMethodLabel.setText((String) feedCombo.getSelectedItem());
            log("Método de alimentación cambiado a: " + feedCombo.getSelectedItem());
        });
        inner.add(feedCombo);
        inner.add(makeLabel("↑ Bridge desacopla estrategia de entrega", FONT_SMALL, TEXT_DIM));
        inner.add(feedMethodLabel);

        // ── Report button ─────────────────────────────────────────────────
        inner.add(Box.createVerticalStrut(8));
        JButton reportBtn = makeButton("📋 Ver Reporte Completo", ACCENT_TEAL, e -> showReport());
        inner.add(reportBtn);

        JButton calibrateBtn = makeButton("🔧 Calibrar Sensor", ALERT_ORANGE, e -> calibrateSensor());
        inner.add(calibrateBtn);

        p.add(inner, BorderLayout.NORTH);
        return p;
    }

    // ── Log panel ─────────────────────────────────────────────────────────────

    private JPanel buildLogPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);
        p.setBorder(createCardBorder("📜  REGISTRO DE EVENTOS"));

        logArea = new JTextArea();
        logArea.setBackground(new Color(2, 8, 18));
        logArea.setForeground(new Color(0, 210, 140));
        logArea.setFont(FONT_SMALL);
        logArea.setEditable(false);
        logArea.setCaretColor(ACCENT_GLOW);

        JScrollPane scroll = new JScrollPane(logArea);
        styleScroll(scroll);
        p.add(scroll, BorderLayout.CENTER);

        JButton clearBtn = makeButton("🧹 Limpiar", TEXT_DIM, e -> logArea.setText(""));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 3));
        btnRow.setBackground(BG_CARD);
        btnRow.add(clearBtn);
        p.add(btnRow, BorderLayout.SOUTH);
        return p;
    }

    // ── Status bar ────────────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        bar.setBackground(new Color(2, 8, 20));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT_TEAL));

        bar.add(makeLabel("🌊 Sistema Activo", FONT_SMALL, ACCENT_GLOW));
        bar.add(makeLabel("|", FONT_SMALL, TEXT_DIM));
        bar.add(sensorNameLabel = makeLabel("Sensor: AquaSmart IoT", FONT_SMALL, TEXT_DIM));
        bar.add(makeLabel("|", FONT_SMALL, TEXT_DIM));
        bar.add(makeLabel("Actualización automática cada 15 s", FONT_SMALL, TEXT_DIM));
        return bar;
    }

    // ── Business action handlers ──────────────────────────────────────────────

    private void takeMeasurement() {
        WaterParameters params = service.takeMeasurement();
        if (params == null) {
            // Keep last known readings visible; just show alert in log and header
            String sensorName = service.getActiveSensor().getSensorName();
            log("⚠ Falla transitoria en [" + sensorName + "]. Reintentando en el siguiente ciclo...");
            alertTextLabel.setForeground(ALERT_ORANGE);
            alertTextLabel.setText("⚠ SENSOR — Ultimo valor conocido mostrado");
            // If we have a previous measurement, keep gauges as-is (do not clear them)
            WaterParameters last = service.getLastMeasurement();
            if (last == null) {
                alertTextLabel.setText("⚠ Sin datos de sensor aun");
            }
            return;
        }
        updateWaterGauges(params);
        updateAnimalTable();
        updateAlerts();
        log("📡 Medicion tomada [" + service.getActiveSensor().getSensorName() + "]: " + params);
    }

    private void feedSelectedAnimal() {
        int row = animalTable.getSelectedRow();
        if (row < 0) { showInfo("Seleccione un animal de la tabla."); return; }
        String id = (String) tableModel.getValueAt(row, 0);
        String result = service.feedAnimal(id, 0);
        log("🍽 " + result);
        updateAnimalTable();
    }

    private void feedAllHungry() {
        List<String> reports = service.feedAllHungry();
        reports.forEach(r -> log("🍽 " + r));
        updateAnimalTable();
    }

    private void deleteSelectedAnimal() {
        int row = animalTable.getSelectedRow();
        if (row < 0) { showInfo("Seleccione un animal para eliminar."); return; }
        String id   = (String) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar a " + name + " del sistema?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            service.removeAnimal(id);
            log("🗑 " + name + " eliminado del sistema.");
            updateAnimalTable();
        }
    }

    private void calibrateSensor() {
        String result = service.getActiveSensor().calibrate();
        log("🔧 Calibración: " + result);
        showInfo(result);
    }

    private void showReport() {
        JTextArea area = new JTextArea(service.generateSummaryReport());
        area.setFont(FONT_BODY);
        area.setBackground(BG_DEEP);
        area.setForeground(TEXT_PRIMARY);
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(540, 360));
        JOptionPane.showMessageDialog(this, scroll, "Reporte del Acuario", JOptionPane.PLAIN_MESSAGE);
    }

    private void showAddAnimalDialog() {
        JTextField nameField     = new JTextField();
        JComboBox<MarineAnimal.Species> speciesBox =
            new JComboBox<>(MarineAnimal.Species.values());
        JTextField weightField   = new JTextField("0.5");
        JTextField ageField      = new JTextField("12");
        JTextField zoneField     = new JTextField("Zona Arrecife");

        styleDialogField(nameField);
        styleDialogField(weightField);
        styleDialogField(ageField);
        styleDialogField(zoneField);
        styleCombBox(speciesBox);

        Object[] msg = {
            makeLabel("Nombre:", FONT_BODY, TEXT_PRIMARY), nameField,
            makeLabel("Especie:", FONT_BODY, TEXT_PRIMARY), speciesBox,
            makeLabel("Peso (kg):", FONT_BODY, TEXT_PRIMARY), weightField,
            makeLabel("Edad (meses):", FONT_BODY, TEXT_PRIMARY), ageField,
            makeLabel("Zona del tanque:", FONT_BODY, TEXT_PRIMARY), zoneField
        };

        int ok = JOptionPane.showConfirmDialog(this, msg, "Agregar Animal Marino",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (ok == JOptionPane.OK_OPTION) {
            try {
                String name  = nameField.getText().trim();
                if (name.isEmpty()) { showInfo("El nombre no puede estar vacío."); return; }
                MarineAnimal.Species species = (MarineAnimal.Species) speciesBox.getSelectedItem();
                double weight = Double.parseDouble(weightField.getText().trim());
                int age       = Integer.parseInt(ageField.getText().trim());
                String zone   = zoneField.getText().trim();

                MarineAnimal animal = new MarineAnimal(name, species, weight, age, zone);
                service.addAnimal(animal);
                log("➕ Nuevo animal agregado: " + animal);
                updateAnimalTable();
            } catch (NumberFormatException ex) {
                showInfo("Por favor, ingrese valores numéricos válidos.");
            }
        }
    }

    // ── UI update helpers ─────────────────────────────────────────────────────

    private void refreshAll() {
        takeMeasurement();
        updateAnimalTable();
    }

    private void updateWaterGauges(WaterParameters p) {
        // Temperature (0–40°C)
        int tempInt = (int) Math.round(p.getTemperatureCelsius());
        tempBar.setValue(tempInt);
        tempBar.setForeground(tempInt >= 22 && tempInt <= 28 ? OK_GREEN : ALERT_RED);
        tempLabel.setText(String.format("Temp: %.1f°C", p.getTemperatureCelsius()));

        // pH mapped to 0–100 for bar (7.5–8.8 visible range)
        int phMapped = (int) Math.round((p.getPh() - 7.5) / (8.8 - 7.5) * 100);
        phBar.setValue(Math.max(0, Math.min(100, phMapped)));
        phBar.setForeground(p.getPh() >= 8.0 && p.getPh() <= 8.4 ? OK_GREEN : ALERT_ORANGE);
        phLabel.setText(String.format("pH: %.3f", p.getPh()));

        // Salinity (0–50 ppt)
        int salInt = (int) Math.round(p.getSalinityPpt());
        salinityBar.setValue(Math.min(50, salInt));
        salinityBar.setForeground(salInt >= 32 && salInt <= 36 ? OK_GREEN : ALERT_ORANGE);
        salinityLabel.setText(String.format("Sal: %.1f ‰", p.getSalinityPpt()));

        // Oxygen (0–15 mg/L)
        int o2Mapped = (int) Math.round(p.getOxygenMgL());
        o2Bar.setValue(Math.min(15, Math.max(0, o2Mapped)));
        o2Bar.setForeground(p.getOxygenMgL() >= 7 ? OK_GREEN : ALERT_RED);
        o2Label.setText(String.format("O₂: %.2f mg/L", p.getOxygenMgL()));

        // Ammonium (0–3 mg/L — high is bad)
        int nh4Mapped = (int) Math.round(p.getAmmoniumMgL() * 100); // scale for bar
        nh4Bar.setValue(Math.min(100, nh4Mapped));
        nh4Bar.setForeground(p.getAmmoniumMgL() < 0.1 ? OK_GREEN :
                              p.getAmmoniumMgL() < 0.5 ? ALERT_ORANGE : ALERT_RED);
        nh4Label.setText(String.format("NH₄: %.4f mg/L", p.getAmmoniumMgL()));

        // Overall status
        int score = p.computeHealthScore();
        waterStatusLabel.setText(p.getHealthLabel());
        waterStatusLabel.setForeground(score >= 85 ? OK_GREEN : score >= 60 ? ALERT_ORANGE : ALERT_RED);
        healthScoreLabel.setText(score + " / 100");
        healthScoreLabel.setForeground(score >= 85 ? OK_GREEN : score >= 60 ? ALERT_ORANGE : ALERT_RED);

        sensorNameLabel.setText("Sensor: " + service.getActiveSensor().getSensorName());
    }

    private void updateAnimalTable() {
        tableModel.setAnimals(service.getAllAnimals());
    }

    private void updateAlerts() {
        List<String> alerts = service.getAlertLog();
        if (alerts.isEmpty()) {
            alertTextLabel.setText("✅ Parámetros normales");
            alertTextLabel.setForeground(OK_GREEN);
        } else {
            alertTextLabel.setText(alerts.get(0));
            alertTextLabel.setForeground(ALERT_RED);
        }
    }

    private void log(String msg) {
        String time = java.time.LocalTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + time + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(15_000, e -> refreshAll());
        autoRefreshTimer.start();
    }

    // ── Widget factory helpers ────────────────────────────────────────────────

    private JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    private JButton makeButton(String text, Color fg, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(fg);
        btn.setBackground(BG_PANEL);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fg.darker(), 1),
            new EmptyBorder(5, 10, 5, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(fg.darker().darker()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(BG_PANEL); }
        });
        return btn;
    }

    private JProgressBar makeGaugeBar(int min, int max) {
        JProgressBar bar = new JProgressBar(min, max);
        bar.setBackground(new Color(10, 25, 50));
        bar.setForeground(ACCENT_TEAL);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(0, 14));
        return bar;
    }

    private Border createCardBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_TEAL, 1),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(4, 6, 4, 6),
                title,
                TitledBorder.LEFT, TitledBorder.TOP,
                FONT_HEADER, ACCENT_TEAL
            )
        );
    }

    private void styleTable(JTable table) {
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_PRIMARY);
        table.setFont(FONT_SMALL);
        table.setGridColor(new Color(20, 45, 80));
        table.setRowHeight(22);
        table.setSelectionBackground(new Color(0, 80, 120));
        table.setSelectionForeground(ACCENT_GLOW);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(6, 18, 40));
        header.setForeground(ACCENT_CYAN);
        header.setFont(FONT_HEADER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ACCENT_TEAL));

        // Custom renderer for health status column (col 4)
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBackground(sel ? new Color(0, 80, 120) : BG_PANEL);
                setForeground(getStatusColor(val == null ? "" : val.toString()));
                setFont(FONT_SMALL);
                return this;
            }

            private Color getStatusColor(String status) {
                if (status.contains("Saludable"))       return OK_GREEN;
                if (status.contains("Estresado"))       return ALERT_ORANGE;
                if (status.contains("Enfermo"))         return ALERT_RED;
                if (status.contains("Recuperación"))    return ACCENT_CYAN;
                return TEXT_PRIMARY;
            }
        });
    }

    private void styleScroll(JScrollPane scroll) {
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_PANEL);
        scroll.getVerticalScrollBar().setBackground(BG_PANEL);
        scroll.getHorizontalScrollBar().setBackground(BG_PANEL);
    }

    @SuppressWarnings("unchecked")
    private void styleCombBox(JComboBox combo) {
        combo.setBackground(BG_PANEL);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_BODY);
        combo.setBorder(BorderFactory.createLineBorder(ACCENT_TEAL));
    }

    private void styleDialogField(JTextField field) {
        field.setBackground(BG_PANEL);
        field.setForeground(TEXT_PRIMARY);
        field.setFont(FONT_BODY);
        field.setCaretColor(ACCENT_GLOW);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_TEAL),
            new EmptyBorder(3, 5, 3, 5)));
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.PLAIN_MESSAGE);
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        // Apply Nimbus as base L&F then override with ocean theme
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        UIManager.put("OptionPane.background", BG_DEEP);
        UIManager.put("Panel.background", BG_DEEP);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        SwingUtilities.invokeLater(() -> new AquariumUI().setVisible(true));
    }
}
