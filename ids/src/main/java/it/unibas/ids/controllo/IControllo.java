package it.unibas.ids.controllo;

import com.google.inject.ImplementedBy;

import javax.swing.*;

@ImplementedBy(Controllo.class)
public interface IControllo {

    public Action getAction(String nome);
}
