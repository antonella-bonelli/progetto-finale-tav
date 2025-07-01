package it.unibas.ids.controllo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.ids.analyzer.AnalysisContext;
import it.unibas.ids.analyzer.IEventAnalyzer;
import it.unibas.ids.vista.ICloneConfigDialog;
import it.unibas.ids.vista.IVista;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Singleton
@Slf4j
public class SaveConfigAction extends AbstractAction {
    AnalysisContext analysisContext;
    ICloneConfigDialog dialog;
    IVista vista;

    @Inject()
    public SaveConfigAction(AnalysisContext analysisContext, ICloneConfigDialog dialog, IVista vista) {
        this.dialog = dialog;
        this.analysisContext = analysisContext;
        this.vista = vista;
        this.putValue(Action.NAME, "Salva configurazione");
        this.putValue(Action.SHORT_DESCRIPTION, "Salva la configurazione corrente");
        this.putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_S);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl S"));
        this.enabled = false;
    }

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        log.info("Saving current configuration...");
        IEventAnalyzer analyzer = dialog.getMainAnalyzer();
        analysisContext.setStrategy(analyzer);
        JOptionPane.showMessageDialog((Component) dialog, "Configurazione salvata correttamente.", "Info", JOptionPane.INFORMATION_MESSAGE);
        dialog.enabledSaveButton();
    }
}
