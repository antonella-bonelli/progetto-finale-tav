package it.unibas.ids.controllo;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.ids.Applicazione;
import it.unibas.ids.vista.IVista;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@Singleton
@Slf4j
public class Controllo implements IControllo {

    private final Map<String, Action> mapAction = new HashMap<>();

    @Override
    public Action getAction(String nome) {
        Action action = this.mapAction.get(nome);
        if (action == null) {
            throw new IllegalArgumentException("Action not found: " + nome);
        }
        return action;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Applicazione applicazione = Applicazione.getInstance();
                IVista vista = (IVista) applicazione.getComponentInstance(IVista.class);
                vista.init();
            }
        });
    }

    @Inject
    public void setAzioneEsci(ExitAction azione) {
        this.addAction(azione);
    }

    @Inject
    public void setAzioneAvviaSimulazione(StartSimulationAction azione) {
        this.addAction(azione);
    }

    @Inject
    public void setAzioneStopSimulazione(StopSimulationAction azione) {
        this.addAction(azione);
    }

    @Inject
    public void setAzioneClonaConfigurazione(CloneConfigAction azione) {
        this.addAction(azione);
    }

    @Inject
    public void setAzioneTestConfigurazione(TestConfigAction azione) {
        this.addAction(azione);
    }

    private void addAction(Action action) {
        String name = action.getClass().getName();
        log.info("Action added: {}", name);
        this.mapAction.put(name, action);
    }

}