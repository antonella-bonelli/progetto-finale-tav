package it.unibas.ids.util;

import it.unibas.common.model.Event;
import it.unibas.ids.analyzer.EAnalysisType;
import lombok.Getter;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ViewUtil {

    @Getter
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static String formatLogMessage(Event event) {
        String formattedTimestamp = event.getTimestamp() != null
                ? event.getTimestamp().format(formatter)
                : "data sconosciuta";
        return String.format(
                "[%s] %s - %s (%s)",
                formattedTimestamp,
                event.getSeverity(),
                event.getType().getDescription(),
                event.getUserId()
        );
    }

    public static EAnalysisType getAnalysisTypeByDescription(String description) {
        if (EAnalysisType.ADVANCED.getDescription().equals(description)) {
            return EAnalysisType.ADVANCED;
        } else {
            return EAnalysisType.SIMPLE;
        }
    }

    public static JTextArea getLogArea() {
        // Log area
        JTextArea eventLogArea = new JTextArea();
        eventLogArea.setEditable(false);
        eventLogArea.setBackground(Color.BLACK);
        eventLogArea.setForeground(Color.GREEN);
        eventLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        return eventLogArea;
    }

    public static JPanel getLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("📋 Event Log"));
        return leftPanel;
    }

    public static JTextField createThresholdTextField() {
        JTextField textField = new JTextField(5);

        // Crea un DocumentFilter che accetta solo numeri interi positivi
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (isValidInput(fb.getDocument(), offset, string, 0)) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (isValidInput(fb.getDocument(), offset, text, length)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private boolean isValidInput(Document doc, int offset, String text, int length) {
                try {
                    // Ottieni il testo corrente nel documento
                    String currentText = doc.getText(0, doc.getLength());
                    // Simula l'inserimento/sostituzione
                    String newText = currentText.substring(0, offset) + text + currentText.substring(offset + length);

                    // Permetti stringa vuota (per poter cancellare tutto)
                    if (newText.isEmpty()) {
                        return true;
                    }

                    // Verifica che sia un numero intero positivo
                    int value = Integer.parseInt(newText);
                    return value > 0;
                } catch (NumberFormatException | BadLocationException e) {
                    return false;
                }
            }
        });

        return textField;
    }
}
