package it.unibas.ids.controllo;

import it.unibas.ids.collector.EventCollector;
import it.unibas.ids.collector.TCPEventSubscriber;
import it.unibas.ids.vista.IMainView;
import it.unibas.ids.vista.IVista;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Singleton
@Slf4j
public class StartSimulationAction extends AbstractAction {
    private IControllo controllo;
    private IVista vista;
    private IMainView mainView;
    private EventCollector collector;
    private TCPEventSubscriber tcpSubscriber;

    @Inject()
    public StartSimulationAction(IControllo controllo, IVista vista, IMainView mainView, EventCollector collector, TCPEventSubscriber tcpSubscriber) {
        this.controllo = controllo;
        this.vista = vista;
        this.mainView = mainView;
        this.collector = collector;
        this.tcpSubscriber = tcpSubscriber;
        this.putValue(Action.NAME, "Avvia simulazione");
        this.putValue(Action.SHORT_DESCRIPTION, "Avvia la simulazione del sistema IDS");
        this.putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_S);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl S"));
    }

    public void actionPerformed(ActionEvent evt) {
        log.info("Starting IDS module...");

        try {
            log.info("Connecting to tcp socket...");
            this.tcpSubscriber.start(this.collector);

        } catch (Exception e) {
            log.error("Error in IDS module ", e);
            System.exit(1);
        }
    }
}
