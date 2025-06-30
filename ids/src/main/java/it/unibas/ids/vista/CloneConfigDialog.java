package it.unibas.ids.vista;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventGroup;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.EventType;
import it.unibas.ids.Applicazione;
import it.unibas.ids.analyzer.*;
import it.unibas.ids.model.Alert;
import it.unibas.ids.util.ViewUtil;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static it.unibas.ids.Costanti.AZIONE_TESTA_CONFIG;

@Slf4j
@Singleton
public class CloneConfigDialog extends JDialog implements ICloneConfigDialog {

    private JComboBox<String> analyzerTypeCombo;

    // Checkbox maps per configurazione dinamica
    private Map<EventSeverity, JCheckBox> severityCheckBoxMapSimple = new LinkedHashMap<>();
    private Map<EventGroup, JCheckBox> groupCheckBoxMapSimple = new LinkedHashMap<>();

    private Map<EventSeverity, JCheckBox> severityCheckBoxMapAdvanced = new LinkedHashMap<>();
    private Map<EventType, JTextField> thresholdFieldMap = new LinkedHashMap<>();

    private JTextArea logArea;
    private JTable alertTable;
    private DefaultTableModel alertTableModel;
    private JButton analyzeButton;
    private JButton saveButton;

    private List<Event> events;
    private IEventAnalyzer mainAnalyzer;
    @Inject
    @Named("advancedAnalyzer")
    private AdvancedAnalyzer advancedAnalyzer;
    @Inject
    @Named("simpleAnalyzer")
    private SimpleAnalyzer simpleAnalyzer;

    private JPanel analyzerConfigPanel;
    private JPanel simplePanel;
    private JPanel advancedPanel;
    private JPanel configPanel;


    public CloneConfigDialog() {
//        this.events = events;
//        this.mainAnalyzer = Applicazione.getInstance().getComponentInstance(AnalysisContext.class).getCurrentStrategy();
//        this.simpleAnalyzer = Applicazione.getInstance().getComponentInstance(SimpleAnalyzer.class);
//        this.advancedAnalyzer = Applicazione.getInstance().getComponentInstance(AdvancedAnalyzer.class);
//
        initUI();
    }

    @Override
    public void showDialog(List<Event> events, IEventAnalyzer mainAnalyzer) {
        this.events = events;
        this.mainAnalyzer = mainAnalyzer;
        //initUI();
        //setVisible(true);
    }

    @Override
    public void showMe(boolean modal) {
        setVisible(modal);
    }

    private void initUI() {
        setTitle("Clona e Analizza Configurazione");
        setModal(true);
        setSize(1100, 750);
        setLayout(new BorderLayout());

        // Config Panel sopra
        configPanel = new JPanel(new BorderLayout());
        JPanel selectAnalyzerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        analyzerTypeCombo = new JComboBox<>(new String[]{
                EAnalysisType.ADVANCED.getDescription(),
                EAnalysisType.SIMPLE.getDescription()
        });
        //analyzerTypeCombo.setSelectedItem(mainAnalyzer.getAnalysisType().getDescription());

        selectAnalyzerPanel.add(new JLabel("Analyzer:"));
        selectAnalyzerPanel.add(analyzerTypeCombo);
        configPanel.add(selectAnalyzerPanel, BorderLayout.NORTH);

        // Pannello centrale per la configurazione dinamica
        analyzerConfigPanel = new JPanel(new CardLayout());
        simplePanel = createSimpleAnalyzerConfigPanel();
        advancedPanel = createAdvancedConfigPanel();
//        if (mainAnalyzer instanceof SimpleAnalyzer) {
//            analyzerConfigPanel.add(simplePanel, EAnalysisType.SIMPLE.getDescription());
//            analyzerConfigPanel.add(advancedPanel, EAnalysisType.ADVANCED.getDescription());
//        } else {
//            analyzerConfigPanel.add(advancedPanel, EAnalysisType.ADVANCED.getDescription());
//            analyzerConfigPanel.add(simplePanel, EAnalysisType.SIMPLE.getDescription());
//        }
//        configPanel.add(analyzerConfigPanel, BorderLayout.CENTER);

        // Cambio configurazione dinamica
        analyzerTypeCombo.addActionListener(e -> {
            log.debug("ActionListener triggered: {}", analyzerTypeCombo.getSelectedItem());
            CardLayout cl = (CardLayout) (analyzerConfigPanel.getLayout());
            cl.show(analyzerConfigPanel, (String) analyzerTypeCombo.getSelectedItem());
        });

        // Main content: split log/alert come MainView
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Log area
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("📋 Event Log"));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        leftPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        //initLogArea();

        // Alert table area
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("🚨 Alert Generati"));

        String[] alertColumns = {"Id", "Time", "Severity", "Type", "Status"};
        alertTableModel = new DefaultTableModel(alertColumns, 0);
        alertTable = new JTable(alertTableModel);
        alertTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        rightPanel.add(new JScrollPane(alertTable), BorderLayout.CENTER);

        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);
        mainSplit.setDividerLocation(550);

        // Bottone analizza
        analyzeButton = new JButton("Analizza eventi ora");
        //analyzeButton.addActionListener(e -> runCloneAnalysis());

        // Bottone Salva
        saveButton = new JButton("Salva configurazione");
        saveButton.setEnabled(false);
        saveButton.addActionListener(e -> saveCurrentConfigToMainAnalyzer());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 8));
        bottomPanel.add(analyzeButton);
        bottomPanel.add(saveButton);

        add(configPanel, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
    }

    public void setButtonAction(String button, Action action) {
        if (button.equals(AZIONE_TESTA_CONFIG)) {
            analyzeButton.setAction(action);
        }

    }

    private JPanel createSimpleAnalyzerConfigPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));

        // Checkbox per levels (EventSeverity)
        JPanel levelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        levelsPanel.setBorder(BorderFactory.createTitledBorder("Severità che generano sempre alert"));
        severityCheckBoxMapSimple.clear();
        for (EventSeverity sev : EventSeverity.values()) {
            JCheckBox cb = new JCheckBox(sev.name());
            severityCheckBoxMapSimple.put(sev, cb);
            levelsPanel.add(cb);
        }

        // Checkbox per groups (EventGroup)
        JPanel groupsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        groupsPanel.setBorder(BorderFactory.createTitledBorder("Gruppi di eventi che generano sempre alert"));
        groupCheckBoxMapSimple.clear();
        for (EventGroup group : EventGroup.values()) {
            JCheckBox cb = new JCheckBox(group.name());
            groupCheckBoxMapSimple.put(group, cb);
            groupsPanel.add(cb);
        }

        panel.add(levelsPanel);
        panel.add(groupsPanel);

        return panel;
    }

    public void resetAlertTable() {
        alertTableModel.setRowCount(0);
        alertTableModel.fireTableRowsUpdated(0, alertTableModel.getRowCount() - 1);
    }

    private JPanel createAdvancedConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Checkbox per levels (EventSeverity)
        JPanel levelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        levelsPanel.setBorder(BorderFactory.createTitledBorder("Severità che generano sempre alert"));
        severityCheckBoxMapAdvanced.clear();
        for (EventSeverity sev : EventSeverity.values()) {
            JCheckBox cb = new JCheckBox(sev.name());
            severityCheckBoxMapAdvanced.put(sev, cb);
            levelsPanel.add(cb);
        }

        // Soglie per ogni EventType - divise in 2 colonne
        JPanel thresholdsOuterPanel = new JPanel(new BorderLayout());
        thresholdsOuterPanel.setBorder(BorderFactory.createTitledBorder("Soglia per tipo di evento"));

        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));

        thresholdFieldMap.clear();
        EventType[] types = EventType.values();
        int mid = (int) Math.ceil(types.length / 2.0);
        for (int i = 0; i < types.length; i++) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            row.add(new JLabel(types[i].name()));
            JTextField tf = new JTextField(5);
            thresholdFieldMap.put(types[i], tf);
            row.add(tf);
            if (i < mid) {
                leftColumn.add(row);
            } else {
                rightColumn.add(row);
            }
        }
        JPanel thresholdsInnerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        thresholdsInnerPanel.add(leftColumn);
        thresholdsInnerPanel.add(rightColumn);

        thresholdsOuterPanel.add(thresholdsInnerPanel, BorderLayout.CENTER);

        // Aggiungi i due pannelli
        panel.add(levelsPanel);
        panel.add(thresholdsOuterPanel);

        return panel;
    }

    public AnalysisRules getUpdatedRules() {
        String selectedAnalyzer = (String) analyzerTypeCombo.getSelectedItem();
        log.debug("**** selectedAnalyzer: {}", selectedAnalyzer);
        if (EAnalysisType.ADVANCED.getDescription().equals(selectedAnalyzer)) {
            log.debug("**** selectedAnalyzer");
            return getAdvancedRules();
        } else if (EAnalysisType.SIMPLE.getDescription().equals(selectedAnalyzer)) {
            log.debug("**** simple rules");
            return getSimpleRules();
        }
        return null;
    }

    public String getSelectedAnalyzer() {
        return (String) analyzerTypeCombo.getSelectedItem();
    }

    private AnalysisRules getSimpleRules() {
        // Aggiorna levels
        Set<EventSeverity> selectedLevels = severityCheckBoxMapSimple.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        // Aggiorna groups
        Set<EventGroup> selectedGroups = groupCheckBoxMapSimple.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return AnalysisRules.builder()
                .groups(selectedGroups)
                .levels(selectedLevels)
                .build();
    }

    private AnalysisRules getAdvancedRules() {
        // Aggiorna levels in base ai checkbox
        Set<EventSeverity> selectedLevels = severityCheckBoxMapAdvanced.entrySet().stream()
                .filter(e -> e.getValue().isSelected())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Map<EventType, Integer> thresholds = new HashMap<>();
        for (Map.Entry<EventType, JTextField> entry : thresholdFieldMap.entrySet()) {
            try {
                int threshold = Integer.parseInt(entry.getValue().getText());
                thresholds.put(entry.getKey(), threshold);
            } catch (NumberFormatException ex) {
                thresholds.put(entry.getKey(), Integer.MAX_VALUE);
            }
        }

        return AnalysisRules.builder()
                .eventThresholds(thresholds)
                .levels(selectedLevels)
                .build();
    }

    public void updateAlertTable(List<Alert> alerts) {
        for (Alert alert : alerts) {
            alertTableModel.addRow(new Object[]{
                    alert.getAlertId(),
                    alert.getTimestamp().format(ViewUtil.getFormatter()),
                    alert.getEventSeverity().toString(),
                    alert.getAlertType(),
                    alert.getStatus().toString()
            });
        }
        saveButton.setEnabled(true);
    }

    @Override
    public void updateConfigPanelsFromRules() {
        log.debug("--- {}", mainAnalyzer.getAnalysisType().getDescription());
        analyzerTypeCombo.setSelectedItem(mainAnalyzer.getAnalysisType().getDescription());
        log.debug("!!! {} - {}", analyzerTypeCombo, analyzerTypeCombo.getSelectedItem());
        if (mainAnalyzer instanceof SimpleAnalyzer) {
            analyzerConfigPanel.add(simplePanel, EAnalysisType.SIMPLE.getDescription());
            analyzerConfigPanel.add(advancedPanel, EAnalysisType.ADVANCED.getDescription());
        } else {
            analyzerConfigPanel.add(advancedPanel, EAnalysisType.ADVANCED.getDescription());
            analyzerConfigPanel.add(simplePanel, EAnalysisType.SIMPLE.getDescription());
        }
        configPanel.add(analyzerConfigPanel, BorderLayout.CENTER);
        AnalysisRules simpleRules = simpleAnalyzer.getRules();
        AnalysisRules advancedRules = advancedAnalyzer.getRules();
        if (this.mainAnalyzer instanceof SimpleAnalyzer) {
            simpleRules = this.mainAnalyzer.getRules();
        } else {
            advancedRules = this.mainAnalyzer.getRules();
        }
        // SimpleAnalyzer
        Set<EventSeverity> simpleLevels = simpleRules.getLevels();
        Set<EventGroup> simpleGroups = simpleRules.getGroups();

        for (Map.Entry<EventSeverity, JCheckBox> entry : severityCheckBoxMapSimple.entrySet()) {
            entry.getValue().setSelected(simpleLevels != null && simpleLevels.contains(entry.getKey()));
        }
        for (Map.Entry<EventGroup, JCheckBox> entry : groupCheckBoxMapSimple.entrySet()) {
            entry.getValue().setSelected(simpleGroups != null && simpleGroups.contains(entry.getKey()));
        }

        // AdvancedAnalyzer
        Set<EventSeverity> advLevels = advancedRules.getLevels();
        Map<EventType, Integer> thresholds = advancedRules.getEventThresholds();

        for (Map.Entry<EventSeverity, JCheckBox> entry : severityCheckBoxMapAdvanced.entrySet()) {
            entry.getValue().setSelected(advLevels != null && advLevels.contains(entry.getKey()));
        }
        for (Map.Entry<EventType, JTextField> entry : thresholdFieldMap.entrySet()) {
            Integer val = thresholds != null ? thresholds.get(entry.getKey()) : null;
            entry.getValue().setText(val != null ? String.valueOf(val) : "");
        }
    }

//    private void runCloneAnalysis() {
//        alertTableModel.setRowCount(0);
//
//        String selectedAnalyzer = (String) analyzerTypeCombo.getSelectedItem();
//        AnalysisRules rules;
//        IEventAnalyzer analyzer;
//        if (EAnalysisType.ADVANCED.getDescription().equals(selectedAnalyzer)) {
//            // Clona e aggiorna le regole
//            rules = advancedAnalyzer.getRules().clone();
//
//            // Aggiorna levels in base ai checkbox
//            Set<EventSeverity> selectedLevels = severityCheckBoxMapAdvanced.entrySet().stream()
//                    .filter(e -> e.getValue().isSelected())
//                    .map(Map.Entry::getKey)
//                    .collect(Collectors.toSet());
//            rules.setLevels(selectedLevels);
//
//            // Aggiorna soglie in base ai campi
//            Map<EventType, Integer> thresholds = new HashMap<>();
//            for (Map.Entry<EventType, JTextField> entry : thresholdFieldMap.entrySet()) {
//                try {
//                    int threshold = Integer.parseInt(entry.getValue().getText());
//                    thresholds.put(entry.getKey(), threshold);
//                } catch (NumberFormatException ex) {
//                    thresholds.put(entry.getKey(), Integer.MAX_VALUE);
//                }
//            }
//            rules.setEventThresholds(thresholds);
//
//            analyzer = advancedAnalyzer.clone();
//            analyzer.updateRules(rules);
//        } else {
//            rules = simpleAnalyzer.getRules();
//
//            // Aggiorna levels
//            Set<EventSeverity> selectedLevels = severityCheckBoxMapSimple.entrySet().stream()
//                    .filter(e -> e.getValue().isSelected())
//                    .map(Map.Entry::getKey)
//                    .collect(Collectors.toSet());
//            rules.setLevels(selectedLevels);
//
//            // Aggiorna groups
//            Set<EventGroup> selectedGroups = groupCheckBoxMapSimple.entrySet().stream()
//                    .filter(e -> e.getValue().isSelected())
//                    .map(Map.Entry::getKey)
//                    .collect(Collectors.toSet());
//            rules.setGroups(selectedGroups);
//
//            analyzer = simpleAnalyzer.clone();
//            analyzer.updateRules(rules);
//        }
//
//        log.debug(" Analyzer: {}", analyzer.getAnalyzerName());
//        // Svuota anche gli allarmi precedenti nell'AlertManager dell'analyzer
//        analyzer.getManager().clearAll();
//
//        // Analizza eventi
//        AnalysisContextHolder.setModalAnalysis(true);
//        try {
//            for (Event event : events) {
//                analyzer.analyzeEvent(event.clone());
//            }
//        } finally {
//            AnalysisContextHolder.setModalAnalysis(false);
//        }
//
//        // Recupera e mostra Alert
//        List<Alert> alerts = analyzer.getManager().getActiveAlerts();
//        mainAnalyzer = analyzer;
//        log.debug("ci sono {} allarmi", alerts.size());
//        for (Alert alert : alerts) {
//            alertTableModel.addRow(new Object[]{
//                    alert.getAlertId(),
//                    alert.getTimestamp().format(ViewUtil.getFormatter()),
//                    alert.getEventSeverity().toString(),
//                    alert.getAlertType(),
//                    alert.getStatus().toString()
//            });
//        }
//        saveButton.setEnabled(true);
//    }

    @Override
    public void initLogArea() {
        for (Event event : events) {
            logArea.append(ViewUtil.formatLogMessage(event) + "\n");
        }
    }

    private void saveCurrentConfigToMainAnalyzer() {

        log.info("Analyzer: {} - {}", mainAnalyzer.getAnalyzerName(), mainAnalyzer.getRules().toString());
        Applicazione.getInstance().getComponentInstance(AnalysisContext.class).setStrategy(mainAnalyzer);
        JOptionPane.showMessageDialog(this, "Configurazione salvata e applicata all'analyzer principale!");
        saveButton.setEnabled(false);
    }

}