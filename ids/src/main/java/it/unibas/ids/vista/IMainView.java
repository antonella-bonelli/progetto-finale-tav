package it.unibas.ids.vista;

import com.google.inject.ImplementedBy;
import it.unibas.ids.model.Alert;
import it.unibas.common.model.Event;

import javax.swing.*;

@ImplementedBy(MainView.class)
public interface IMainView {
    void init();
    void setButtonAction(String button, Action action);
    void appendEventLog(Event event, String message);
    void addAlert(Alert alert);
}
