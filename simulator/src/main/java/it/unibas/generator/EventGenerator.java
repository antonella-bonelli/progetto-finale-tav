package it.unibas.generator;

import it.unibas.model.Event;

import java.util.function.Consumer;

public interface EventGenerator {
    // Avvia la generazione degli eventi
    void start();
    //Ferma la generazione degli eventi
    void stop();
    //Ritorna se il generatore è attualmente attivo
    boolean isActive();
    //Genera un singolo evento
    Event generateEvent();
    //Impostare la funzione di callback che gestisce gli eventi generati
    void setEventConsumer(Consumer<Event> eventConsumer);
    //Ritorna il nome del generatore
    String getGeneratorName();
    //Ritorana la configurazione attuale del generatore
    GeneratorConfig getConfig();
    //Aggiorna la configurazione del generatore
    void updateConfig(GeneratorConfig config);
}
