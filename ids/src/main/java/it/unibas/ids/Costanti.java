package it.unibas.ids;

import it.unibas.ids.controllo.CloneConfigAction;
import it.unibas.ids.controllo.ExitAction;
import it.unibas.ids.controllo.StartSimulationAction;
import it.unibas.ids.controllo.StopSimulationAction;

public class Costanti {
    public static final String AZIONE_ESCI = ExitAction.class.getName();
    public static final String AZIONE_START = StartSimulationAction.class.getName();
    public static final String AZIONE_CLONE = CloneConfigAction.class.getName();
    public static final String AZIONE_STOP = StopSimulationAction.class.getName();
}
