package it.unibas.ids.controllo;

import it.unibas.ids.collector.TCPEventSubscriber;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Slf4j
@Singleton
public class StopSimulationAction extends AbstractAction {
    private TCPEventSubscriber tcpSubscriber;

    @Inject()
    public StopSimulationAction(TCPEventSubscriber tcpSubscriber) {
        this.tcpSubscriber = tcpSubscriber;
        this.putValue(Action.NAME, "Ferma simulazione");
        this.putValue(Action.SHORT_DESCRIPTION, "Ferma la simulazione del sistema IDS");
        this.putValue(Action.MNEMONIC_KEY, java.awt.event.KeyEvent.VK_F);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl F"));
    }

    public void actionPerformed(ActionEvent evt) {
        if (tcpSubscriber != null) {
            tcpSubscriber.stop();
            log.info("Stopping IDS module...");
        }
    }
}
