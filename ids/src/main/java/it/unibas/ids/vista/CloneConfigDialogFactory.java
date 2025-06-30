package it.unibas.ids.vista;

import com.google.inject.Singleton;
import it.unibas.common.model.Event;

import java.util.List;

@Singleton
public interface CloneConfigDialogFactory {
    ICloneConfigDialog create(List<Event> events);

}
