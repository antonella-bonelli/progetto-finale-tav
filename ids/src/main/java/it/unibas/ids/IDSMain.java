package it.unibas.ids;

import com.google.inject.Guice;
import com.google.inject.Injector;
import it.unibas.ids.collector.EventCollector;
import it.unibas.ids.collector.TCPEventSubscriber;
import it.unibas.ids.config.IdsModule;
import it.unibas.ids.config.IdsProperties;
import it.unibas.simulator.Main;
import it.unibas.simulator.publisher.EventPublisher;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

@Slf4j
class IDSMain {

    public static void main(String[] args) {
        log.info("Starting IDS module...");

        try {

            log.info("Connected to simulator");
            Injector idsInjector = Guice.createInjector(new IdsModule());

            EventCollector collector = idsInjector.getInstance(EventCollector.class);
            IdsProperties properties = idsInjector.getInstance(IdsProperties.class);

            log.info("Connecting to tcp socket...");
            TCPEventSubscriber tcpSubscriber = new TCPEventSubscriber(properties.getTcpHost(), properties.getTcpPort());
            tcpSubscriber.start(collector);

        } catch (Exception e) {
            log.error("Error in IDS module ", e);
            System.exit(1);
        }
    }

}