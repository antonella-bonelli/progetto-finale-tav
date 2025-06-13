package it.unibas.ids;

import com.google.inject.Guice;
import com.google.inject.Injector;
import it.unibas.ids.collector.EventCollector;
import it.unibas.ids.config.IdsModule;

public class Main {
    public static void main(String[] args) {
        System.out.println("🛡️ Starting IDS System...");

        // Setup Dependency Injection
        Injector injector = Guice.createInjector(new IdsModule());

        // Get main components
        EventCollector collector = injector.getInstance(EventCollector.class);

        System.out.println("✅ IDS System started successfully");
        System.out.println("📡 Event collector ready: " + collector.getSubscriberName());
    }
}