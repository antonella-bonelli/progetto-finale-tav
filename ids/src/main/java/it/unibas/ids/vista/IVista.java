package it.unibas.ids.vista;

import com.google.inject.ImplementedBy;
import it.unibas.common.model.Event;

import java.util.List;

@ImplementedBy(Vista.class)
public interface IVista {

    void init();

    void mainView();
}
