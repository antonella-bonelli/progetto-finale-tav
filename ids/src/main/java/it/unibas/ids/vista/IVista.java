package it.unibas.ids.vista;

import com.google.inject.ImplementedBy;

@ImplementedBy(Vista.class)
public interface IVista {

    void init();

    void mainView();
}
