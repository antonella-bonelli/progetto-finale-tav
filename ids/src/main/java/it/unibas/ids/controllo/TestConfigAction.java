package it.unibas.ids.controllo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.common.model.Event;
import it.unibas.common.util.AnalysisContextHolder;
import it.unibas.ids.analyzer.*;
import it.unibas.ids.collector.EventCollector;
import it.unibas.ids.model.Alert;
import it.unibas.ids.vista.ICloneConfigDialog;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.List;

import static it.unibas.ids.util.ViewUtil.getAnalysisTypeByDescription;

@Singleton
@Slf4j
public class TestConfigAction extends AbstractAction {
    private final AnalysisContext analysisContext;
    private final ICloneConfigDialog dialog;
    private final EventCollector collector;

    @Inject()
    public TestConfigAction(EventCollector collector, AnalysisContext analysisContext, ICloneConfigDialog dialog) {
        this.collector = collector;
        this.analysisContext = analysisContext;
        this.dialog = dialog;
        this.putValue(Action.NAME, "Testa la configurazione");
        this.putValue(Action.SHORT_DESCRIPTION, "Testa la configurazione corrente");
        this.putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_T);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl T"));
    }

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        AnalysisContextHolder.setModalAnalysis(true);
        dialog.resetAlertTable();
        IEventAnalyzer testStrategy = analysisContext.getCurrentStrategy().clone();
        testStrategy.getManager().clearAll();
        AnalysisRules newRules = dialog.getUpdatedRules();
        testStrategy.updateRules(newRules);
        EAnalysisType type = getAnalysisTypeByDescription(dialog.getAnalyzerSelected());
        switch (type) {
            case ADVANCED:
                AdvancedAnalyzer advancedAnalyzer = new AdvancedAnalyzer(testStrategy.getManager());
                advancedAnalyzer.updateRules(newRules);
                testStrategy = advancedAnalyzer;
                break;
            case SIMPLE:
                SimpleAnalyzer simpleAnalyzer = new SimpleAnalyzer(testStrategy.getManager());
                simpleAnalyzer.updateRules(newRules);
                testStrategy = simpleAnalyzer;
                break;
        }
        dialog.setMainAnalyzer(testStrategy);
        List<Event> events = collector.getAllEvents();
        for (Event event : events) {
            testStrategy.analyzeEvent(event);
        }
        List<Alert> alerts = testStrategy.getManager().getActiveAlerts();
        log.error("rules: {} - testStrategy {}", newRules, testStrategy.getManager());
        dialog.updateAlertTable(alerts);
        AnalysisContextHolder.setModalAnalysis(false);
    }
}
