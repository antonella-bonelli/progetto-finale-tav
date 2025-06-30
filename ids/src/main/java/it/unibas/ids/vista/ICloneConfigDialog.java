package it.unibas.ids.vista;

import com.google.inject.ImplementedBy;
import it.unibas.common.model.Event;
import it.unibas.ids.analyzer.AnalysisRules;
import it.unibas.ids.analyzer.IEventAnalyzer;
import it.unibas.ids.model.Alert;

import javax.swing.*;
import java.util.List;

@ImplementedBy(CloneConfigDialog.class)
public interface ICloneConfigDialog {

    AnalysisRules getUpdatedRules();

    void updateAlertTable(List<Alert> alerts);

    void resetAlertTable();

    void setButtonAction(String button, Action action);

    void showDialog(List<Event> events, IEventAnalyzer mainAnalyzer);

    void showMe(boolean modal);

    void updateConfigPanelsFromRules();

    void initLogArea();
    String getSelectedAnalyzer();
}
