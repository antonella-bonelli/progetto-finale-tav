package it.unibas.ids.vista;

import it.unibas.common.model.Event;
import it.unibas.common.model.EventGroup;
import it.unibas.common.model.EventSeverity;
import it.unibas.ids.model.Alert;
import it.unibas.ids.util.ViewUtil;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static it.unibas.ids.Costanti.AZIONE_START;
import static it.unibas.ids.Costanti.AZIONE_STOP;
import static it.unibas.ids.util.ViewUtil.getLeftPanel;
import static it.unibas.ids.util.ViewUtil.getLogArea;

@Slf4j
@Singleton
public class MainView extends JPanel implements IMainView {

    private final JButton startButton = new JButton("Start");
    private final JButton stopButton = new JButton("Stop");

    private JTable alertTable;
    private DefaultTableModel alertTableModel;
    private JTextArea eventLogArea;

    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> severityFilterCombo;

    @Inject()
    private MainView() {
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
        JPanel leftPanel = getLeftPanel();
        eventLogArea = getLogArea();

        leftPanel.add(new JScrollPane(eventLogArea), BorderLayout.CENTER);
        leftPanel.add(createFilterPanel(), BorderLayout.NORTH);

        // === DESTRA: Alert e Statistiche ===
        JPanel rightPanel = new JPanel(new BorderLayout());

        // Tabella Alert
        JPanel alertPanel = new JPanel(new BorderLayout());
        alertPanel.setBorder(BorderFactory.createTitledBorder("🚨 Active Alerts"));

        String[] alertColumns = {"Id", "Time", "Severity", "Type", "Status"};
        alertTableModel = new DefaultTableModel(alertColumns, 0);
        alertTable = new JTable(alertTableModel);
        alertTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        alertPanel.add(new JScrollPane(alertTable), BorderLayout.CENTER);

        rightPanel.add(alertPanel, BorderLayout.CENTER);

        mainSplit.setLeftComponent(leftPanel);
        mainSplit.setRightComponent(rightPanel);
        mainSplit.setDividerLocation(600);

        return mainSplit;
    }

    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        filterPanel.add(new JLabel("Filtro:"));
        typeFilterCombo = new JComboBox<>(new String[]{"Tutti", EventGroup.LOGIN.getDescription(), EventGroup.FILE_ACCESS.getDescription(), EventGroup.NETWORK_ACTIVITY.getDescription()});
        filterPanel.add(typeFilterCombo);

        filterPanel.add(new JLabel("Severità:"));
        severityFilterCombo = new JComboBox<>(new String[]{"Tutti", EventSeverity.LOW.name(), EventSeverity.MEDIUM.name(), EventSeverity.HIGH.name(), EventSeverity.CRITICAL.name()});
        filterPanel.add(severityFilterCombo);

        return filterPanel;
    }

    public void setButtonAction(String button, Action action) {
        if (button.equals(AZIONE_START)) {
            startButton.setAction(action);
        }
        if (button.equals(AZIONE_STOP)) {
            stopButton.setAction(action);
        }
    }

    public void appendEventLog(Event event, String message) {
        updateEventLogArea(event, message);
    }

    private void updateEventLogArea(Event event, String message) {
        String selectedType = (String) typeFilterCombo.getSelectedItem();
        String selectedSeverity = (String) severityFilterCombo.getSelectedItem();
        //log.info("selectedType: {} - selectedSeverity: {}", selectedType, selectedSeverity);

        // Normalizza i valori (gestione eventuali null e maiuscole/minuscole)
        String typeFilter = selectedType != null ? selectedType.trim().toUpperCase() : "TUTTI";
        String severityFilter = selectedSeverity != null ? selectedSeverity.trim().toUpperCase() : "TUTTI";

        // Filtra per tipo evento
        boolean matchesType = typeFilter.equals("TUTTI") || event.getGroup().getDescription().equalsIgnoreCase(selectedType);

        // Filtra per severità (usando name in maiuscolo per sicurezza)
        boolean matchesSeverity = severityFilter.equals("TUTTI") ||
                event.getSeverity().name().equalsIgnoreCase(severityFilter);

        if (matchesType && matchesSeverity && message != null && !message.trim().isBlank()) {
            eventLogArea.append(message + "\n");
        }

        eventLogArea.setCaretPosition(eventLogArea.getDocument().getLength()); // Scroll to bottom
    }

    public void addAlert(Alert alert) {
        SwingUtilities.invokeLater(() -> {
            Object[] rowData = {
                    alert.getAlertId(),
                    alert.getTimestamp().format(ViewUtil.getFormatter()),
                    alert.getEventSeverity().toString(),
                    alert.getAlertType(),
                    alert.getStatus().toString()
            };

            alertTableModel.addRow(rowData);

            // Auto-scroll alla nuova riga
            int lastRow = alertTableModel.getRowCount() - 1;
            alertTable.scrollRectToVisible(alertTable.getCellRect(lastRow, 0, true));
            alertTable.setRowSelectionInterval(lastRow, lastRow);

            // Log dell'aggiunta
            log.debug("Alert aggiunto alla tabella: {} - {}", alert.getAlertId(), alert.getEventSeverity());
        });
    }

}
