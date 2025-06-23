package it.unibas.ids.vista;

import com.google.inject.ImplementedBy;
import it.unibas.ids.model.Alert;

import javax.swing.*;

@ImplementedBy(MainView.class)
public interface IMainView {
    void init();
    void setButtonAction(String button, Action action);
    void appendEventLog(String message);
    void addAlert(Alert alert);
}
