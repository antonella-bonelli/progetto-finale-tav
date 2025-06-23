package it.unibas.ids.vista;

import com.google.inject.ImplementedBy;

import javax.swing.*;

@ImplementedBy(MainView.class)
public interface IMainView {
    public void init();
    public void setButtonAction(String button, Action action);
}
