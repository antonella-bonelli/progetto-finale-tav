package it.unibas.ids.controllo;

import com.google.inject.Singleton;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Singleton
public class ExitAction extends AbstractAction {
    public ExitAction() {
        this.putValue(Action.NAME, "Esci");
        this.putValue(Action.SHORT_DESCRIPTION, "Esce dall'applicazione");
        this.putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_E);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl E"));
    }

    public void actionPerformed(ActionEvent evt) {
        System.exit(0);
    }
}
