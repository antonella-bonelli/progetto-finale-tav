package it.unibas.ids.controllo;

import com.google.inject.Inject;
import it.unibas.ids.analyzer.IEventAnalyzer;
import it.unibas.ids.analyzer.RuleBasedAnalyzer;
import it.unibas.ids.analyzer.SimpleAnalyzer;
import it.unibas.ids.vista.IMainView;
import it.unibas.ids.vista.IVista;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Singleton
@Slf4j
public class CloneConfigAction extends AbstractAction {
    private IControllo controllo;
    private IVista vista;
    private IMainView mainView;
    private IEventAnalyzer eventAnalyzer;

    @Inject()
    public CloneConfigAction(IControllo controllo, IVista vista, IMainView mainView, IEventAnalyzer eventAnalyzer) {
        this.controllo = controllo;
        this.vista = vista;
        this.mainView = mainView;
        this.eventAnalyzer = eventAnalyzer;
        this.putValue(Action.NAME, "Clona configurazione");
        this.putValue(Action.SHORT_DESCRIPTION, "Clona la configurazione corrente");
        this.putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_C);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
    }

    public void actionPerformed(ActionEvent evt) {
        log.info("Cloning current configuration...");
        IEventAnalyzer cloned = null;
        switch (eventAnalyzer.getAnalysisType()) {
            case SIMPLE -> {
                SimpleAnalyzer analyzer = (SimpleAnalyzer) eventAnalyzer;
                cloned = analyzer.clone();
            }
            case RULE_BASED -> {
                RuleBasedAnalyzer analyzer = (RuleBasedAnalyzer) eventAnalyzer;
                cloned = analyzer.clone();
            }
        };
        log.info("Original configuration: {}", eventAnalyzer.toString());
        log.info("Cloned configuration: {}", cloned.toString());
    }
}
