package it.unibas.ids.vista;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.common.model.Event;
import it.unibas.common.model.EventGroup;
import it.unibas.common.model.EventSeverity;
import it.unibas.common.model.EventType;
import it.unibas.ids.analyzer.*;
import it.unibas.ids.model.Alert;
import it.unibas.ids.util.ViewUtil;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static it.unibas.ids.Costanti.AZIONE_SALVA;
import static it.unibas.ids.Costanti.AZIONE_TESTA_CONFIG;
import static it.unibas.ids.util.ViewUtil.getAnalysisTypeByDescription;

@Slf4j
@Singleton
public class CloneConfigDialog extends JDialog implements ICloneConfigDialog {

    private JComboBox<String> analyzerTypeCombo;

    // Checkbox maps per configurazione dinamica
    private final Map<EventSeverity, JCheckBox> severityCheckBoxMapSimple = new LinkedHashMap<>();
    private final Map<EventGroup, JCheckBox> groupCheckBoxMapSimple = new LinkedHashMap<>();

    private final Map<EventSeverity, JCheckBox> severityCheckBoxMapAdvanced = new LinkedHashMap<>();
    private final Map<EventType, JTextField> thresholdFieldMap = new LinkedHashMap<>();

    private JTextArea logArea;
    private DefaultTableModel alertTableModel;
    private JButton analyzeButton;
    private JButton saveButton;

    private List<Event> events;
    @Setter
    @Getter
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
        initUI();
    }

    @Override
    public void showDialog(List<Event> events, IEventAnalyzer mainAnalyzer) {
        this.events = events;
        this.mainAnalyzer = mainAnalyzer;
    }

    @Override
    public void showMe(boolean modal) {
        setVisible(modal);
    }

    @Override
    public String getAnalyzerSelected() {
        return Objects.requireNonNull(analyzerTypeCombo.getSelectedItem()).toString();
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

        selectAnalyzerPanel.add(new JLabel("Analyzer:"));
        selectAnalyzerPanel.add(analyzerTypeCombo);
        configPanel.add(selectAnalyzerPanel, BorderLayout.NORTH);

        // Pannello centrale per la configurazione dinamica
        analyzerConfigPanel = new JPanel(new CardLayout());
        simplePanel = createSimpleAnalyzerConfigPanel();
        advancedPanel = createAdvancedConfigPanel();

        // Cambio configurazione dinamica
        analyzerTypeCombo.addActionListener(e -> {
            log.debug("ActionListener triggered: {}", analyzerTypeCombo.getSelectedItem());
            CardLayout cl = (CardLayout) (analyzerConfigPanel.getLayout());
            cl.show(analyzerConfigPanel, (String) analyzerTypeCombo.getSelectedItem());
        });

        // Main content: split log/alert come MainView
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Log area
        JPanel leftPanel = ViewUtil.getLeftPanel();
        logArea = ViewUtil.getLogArea();
        leftPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Alert table area
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("🚨 Alert Generati"));

        String[] alertColumns = {"Id", "Time", "Severity", "Type", "Status"};
        alertTableModel = new DefaultTableModel(alertColumns, 0);
        JTable alertTable = new JTable(alertTableModel);
        alertTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        rightPanel.add(new JScrollPane(alertTable), BorderLayout.CENTER);

        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);
        mainSplit.setDividerLocation(550);

        // Bottone analizza
        analyzeButton = new JButton("Analizza eventi ora");

        // Bottone Salva
        saveButton = new JButton("Salva configurazione");
        saveButton.setEnabled(false);

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
        } else if (button.equals(AZIONE_SALVA)) {
            saveButton.setAction(action);
        }
    }

    private JPanel createSimpleAnalyzerConfigPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));

        // Checkbox per levels (EventSeverity)
        JPanel levelsPanel = createSeverityCheckBox(severityCheckBoxMapSimple);

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

    private JPanel createSeverityCheckBox(Map<EventSeverity, JCheckBox> severityCheckBoxMapSimple) {
        JPanel levelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        levelsPanel.setBorder(BorderFactory.createTitledBorder("Severità che generano sempre alert"));
        severityCheckBoxMapSimple.clear();
        for (EventSeverity sev : EventSeverity.values()) {
            JCheckBox cb = new JCheckBox(sev.name());
            severityCheckBoxMapSimple.put(sev, cb);
            levelsPanel.add(cb);
        }
        return levelsPanel;
    }

    public void resetAlertTable() {
        alertTableModel.setRowCount(0);
        alertTableModel.fireTableRowsUpdated(0, alertTableModel.getRowCount() - 1);
    }

    private JPanel createAdvancedConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Checkbox per levels (EventSeverity)
        JPanel levelsPanel = createSeverityCheckBox(severityCheckBoxMapAdvanced);

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
            JTextField tf = ViewUtil.createThresholdTextField();
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
        EAnalysisType type = getAnalysisTypeByDescription(selectedAnalyzer);
        return switch (type) {
            case ADVANCED -> getAdvancedRules();
            case SIMPLE -> getSimpleRules();
        };
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
        alertTableModel.setRowCount(0);
        analyzerTypeCombo.setSelectedItem(mainAnalyzer.getAnalysisType().getDescription());
        configPanel.add(analyzerConfigPanel, BorderLayout.CENTER);
        AnalysisRules simpleRules = simpleAnalyzer.getRules();
        AnalysisRules advancedRules = advancedAnalyzer.getRules();
        EAnalysisType type = getAnalysisTypeByDescription(mainAnalyzer.getAnalysisType().getDescription());
        switch (type) {
            case SIMPLE -> {
                analyzerConfigPanel.add(simplePanel, EAnalysisType.SIMPLE.getDescription());
                analyzerConfigPanel.add(advancedPanel, EAnalysisType.ADVANCED.getDescription());
                simpleRules = this.mainAnalyzer.getRules();
            }
            case ADVANCED -> {
                analyzerConfigPanel.add(advancedPanel, EAnalysisType.ADVANCED.getDescription());
                analyzerConfigPanel.add(simplePanel, EAnalysisType.SIMPLE.getDescription());
                advancedRules = this.mainAnalyzer.getRules();
            }
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

    @Override
    public void initLogArea() {
        for (Event event : events) {
            logArea.append(ViewUtil.formatLogMessage(event) + "\n");
        }
    }

    @Override
    public void enabledSaveButton() {
        saveButton.setEnabled(true);
    }

}