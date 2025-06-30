package it.unibas.ids.controllo;

import com.google.inject.Inject;
import it.unibas.common.util.AnalysisContextHolder;
import it.unibas.ids.Applicazione;
import it.unibas.ids.Costanti;
import it.unibas.ids.analyzer.AnalysisContext;
import it.unibas.ids.collector.EventCollector;
import it.unibas.ids.vista.ICloneConfigDialog;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Singleton
@Slf4j
public class CloneConfigAction extends AbstractAction {
    private final EventCollector collector;
    private final AnalysisContext analysisContext;
    private final ICloneConfigDialog dialog;

    @Inject()
    public CloneConfigAction(EventCollector collector, AnalysisContext analysisContext, ICloneConfigDialog dialog) {
        this.collector = collector;
        this.analysisContext = analysisContext;
        this.dialog = dialog;
        this.putValue(Action.NAME, "Clona configurazione");
        this.putValue(Action.SHORT_DESCRIPTION, "Clona la configurazione corrente");
        this.putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_C);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
    }

    public void actionPerformed(ActionEvent evt) {
        log.info("Cloning current configuration...");
        Controllo controllo = Applicazione.getInstance().getComponentInstance(Controllo.class);
        //AnalysisContextHolder.setModalAnalysis(true);
        dialog.showDialog(collector.getAllEvents(), analysisContext.getCurrentStrategy());
        dialog.setButtonAction(Costanti.AZIONE_TESTA_CONFIG, controllo.getAction(Costanti.AZIONE_TESTA_CONFIG));
        dialog.updateConfigPanelsFromRules();
        dialog.initLogArea();
        dialog.showMe(true);
    }
}
