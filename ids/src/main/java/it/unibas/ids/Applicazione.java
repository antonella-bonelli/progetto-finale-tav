package it.unibas.ids;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import it.unibas.ids.config.IdsModule;

public class Applicazione {
    private static final Applicazione singleton = new Applicazione();

    public static Applicazione getInstance() {
        return singleton;
    }

    private final Injector injector;

    private Applicazione() {
        this.injector = Guice.createInjector(Stage.DEVELOPMENT, new IdsModule());
    }

    public <T> T getComponentInstance(Class<T> chiave) {
        return this.injector.getInstance(chiave);
    }
}
