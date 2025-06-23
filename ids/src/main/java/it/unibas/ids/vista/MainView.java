package it.unibas.ids.vista;

import it.unibas.ids.model.Alert;
import it.unibas.ids.model.Modello;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

import static it.unibas.ids.Costanti.AZIONE_START;
import static it.unibas.ids.Costanti.AZIONE_STOP;

@Slf4j
@Singleton
public class MainView extends JPanel implements IMainView {
    private Modello model;

    private JButton startButton = new JButton("Start");
    private JButton stopButton = new JButton("Stop");

    private JTable alertTable;
    private DefaultTableModel alertTableModel;
    private JTextArea eventLogArea;
    private JLabel statusLabel;
    private JLabel eventCountLabel;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Inject()
    private MainView(Modello model) {
        this.model = model;
        this.init();

    }

    @Override
    public void init() {
        setSize(1200, 800);
        setLayout(new BorderLayout());
        createHeaderPanel();
        add(createMainContent(), BorderLayout.CENTER);
    }

    private void createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.DARK_GRAY);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Intrusion Detection System");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        // Controlli
        JPanel controls = new JPanel(new FlowLayout());
        controls.setBackground(Color.DARK_GRAY);
        controls.add(startButton);
        controls.add(stopButton);

        header.add(title, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    private JComponent createMainContent() {
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // === SINISTRA: Log Eventi ===
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("📋 Event Log"));

        // Log area
        eventLogArea = new JTextArea();
        eventLogArea.setEditable(false);
        eventLogArea.setBackground(Color.BLACK);
        eventLogArea.setForeground(Color.GREEN);
        eventLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        leftPanel.add(new JScrollPane(eventLogArea), BorderLayout.CENTER);
        //leftPanel.add(createFilterPanel(), BorderLayout.NORTH);

        // === DESTRA: Alert e Statistiche ===
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Tabella Alert
        JPanel alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBorder(BorderFactory.createTitledBorder("🚨 Active Alerts"));

        String[] alertColumns = {"Id","Time", "Severity","Type", "Status"};
        alertTableModel = new DefaultTableModel(alertColumns, 0);
        alertTable = new JTable(alertTableModel);
        alertTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        alertPanel.add(new JScrollPane(alertTable), BorderLayout.CENTER);

        // Statistiche
        JPanel statsPanel = createStatsPanel();

        rightPanel.add(alertPanel, BorderLayout.CENTER);
        rightPanel.add(statsPanel, BorderLayout.SOUTH);

        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);
        mainSplit.setDividerLocation(600);

        return mainSplit;
    }

//    private JPanel createFilterPanel() {
//        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//
//        filterPanel.add(new JLabel("Filtro:"));
//        filterPanel.add(new JComboBox<>(new String[]{"Tutti", "Login", "File", "Network"}));
//
//        filterPanel.add(new JLabel("Severità:"));
//        filterPanel.add(new JComboBox<>(new String[]{"Tutti", "LOW", "MEDIUM", "HIGH", "CRITICAL"}));
//
//        JButton applyFilter = new JButton("Apply");
//        filterPanel.add(applyFilter);
//
//        return filterPanel;
//    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder("📊 Statistics"));

        eventCountLabel = new JLabel("Eventi: 0");
        JLabel alertCountLabel = new JLabel("Allarmi: 0");
        JLabel threatLevelLabel = new JLabel("Threat Level: LOW");

        JProgressBar systemLoad = new JProgressBar(0, 100);
        systemLoad.setStringPainted(true);
        systemLoad.setString("System Load: 0%");

        statsPanel.add(eventCountLabel);
        statsPanel.add(alertCountLabel);
        statsPanel.add(threatLevelLabel);
        statsPanel.add(systemLoad);

        return statsPanel;
    }

    public void setButtonAction(String button, Action action) {
        if (button.equals(AZIONE_START)) {
            startButton.setAction(action);
        }
        if (button.equals(AZIONE_STOP)) {
            stopButton.setAction(action);
        }
    }

    public void appendEventLog(String message) {
        eventLogArea.append(message + "\n");
        eventLogArea.setCaretPosition(eventLogArea.getDocument().getLength()); // Scroll to bottom
    }

    public void addAlert(Alert alert) {
        SwingUtilities.invokeLater(() -> {
            Object[] rowData = {
                    alert.getAlertId(),
                    alert.getTimestamp().format(formatter),
                    alert.getThreatLevel().toString(),
                    alert.getAlertType(),
                    alert.getStatus().toString()
            };

            alertTableModel.addRow(rowData);

            // Auto-scroll alla nuova riga
            int lastRow = alertTableModel.getRowCount() - 1;
            alertTable.scrollRectToVisible(alertTable.getCellRect(lastRow, 0, true));
            alertTable.setRowSelectionInterval(lastRow, lastRow);

            // Log dell'aggiunta
            log.debug("Alert aggiunto alla tabella: {} - {}", alert.getAlertId(), alert.getThreatLevel());
        });
    }

}
