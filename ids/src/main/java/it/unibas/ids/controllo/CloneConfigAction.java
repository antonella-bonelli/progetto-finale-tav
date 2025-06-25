package it.unibas.ids.controllo;

import com.google.inject.Inject;
import it.unibas.ids.analyzer.AdvancedAnalyzer;
import it.unibas.ids.analyzer.AnalysisContext;
import it.unibas.ids.analyzer.IEventAnalyzer;
import it.unibas.ids.analyzer.SimpleAnalyzer;
import it.unibas.ids.collector.EventCollector;
import it.unibas.ids.vista.CloneConfigDialog;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Singleton
@Slf4j
public class CloneConfigAction extends AbstractAction {
    private final EventCollector collector;
    private final AdvancedAnalyzer advancedAnalyzer;
    private final SimpleAnalyzer simpleAnalyzer;
    private final AnalysisContext analysisContext;

    @Inject()
    public CloneConfigAction(EventCollector collector, AdvancedAnalyzer advancedAnalyzer, SimpleAnalyzer simpleAnalyzer, IEventAnalyzer eventAnalyzer, AnalysisContext analysisContext) {
        this.collector = collector;
        this.advancedAnalyzer = advancedAnalyzer;
        this.simpleAnalyzer = simpleAnalyzer;
        this.analysisContext = analysisContext;
        this.putValue(Action.NAME, "Clona configurazione");
        this.putValue(Action.SHORT_DESCRIPTION, "Clona la configurazione corrente");
        this.putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_C);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
    }

    public void actionPerformed(ActionEvent evt) {
        log.info("Cloning current configuration...");
        CloneConfigDialog dialog = new CloneConfigDialog(
                collector.getAllEvents(),
                (AdvancedAnalyzer) advancedAnalyzer.clone(),
                (SimpleAnalyzer) simpleAnalyzer.clone(),
                analysisContext
        );
        dialog.setVisible(true);
    }
}
