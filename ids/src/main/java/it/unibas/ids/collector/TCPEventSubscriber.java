package it.unibas.ids.collector;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibas.ids.config.IdsProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Singleton
public class TCPEventSubscriber {
    private final String host;
    private final int port;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private Thread thread;
    private Socket socket;
    private BufferedReader in;

    @Inject
    public TCPEventSubscriber(IdsProperties properties) {
        this.host = properties.getTcpHost();
        this.port = properties.getTcpPort();
    }

    public void start(EventCollector collector) {
        //log.info("[start] TCPEventSubscriber instance: {}", System.identityHashCode(this));

        if (thread != null && thread.isAlive()) {
            log.warn("[IDS] TCPEventSubscriber è già attivo.");
            return;
        }

        running.set(true);
        thread = new Thread(() -> {
            try (Socket s = new Socket(host, port);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()))
            ) {
                this.socket = s;
                this.in = reader;

                log.info("🚀 [IDS] Connected to simulator on {}:{}", host, port);
                String line;
                while (running.get() && (line = in.readLine()) != null) {
                    collector.onEventJson(line);
                }
            } catch (Exception e) {
                log.error("[IDS] Connection to simulator lost or error: {}", e.getMessage());
            } finally {
                cleanup();
                log.info("[IDS] TCPEventSubscriber stoppato.");
            }
        });
        thread.start();
    }

    private void cleanup() {
        // Chiudo reader e socket in sicurezza
        try {
            if (in != null) in.close();
        } catch (Exception ex) {
            log.warn("Errore nella chiusura del BufferedReader: {}", ex.getMessage());
        } finally {
            in = null;
        }
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ex) {
            log.warn("Errore nella chiusura della socket: {}", ex.getMessage());
        } finally {
            socket = null;
        }
    }

    public void stop() {
        //log.info("[stop] TCPEventSubscriber instance: {}", System.identityHashCode(this));

        running.set(false);
        cleanup();
        if (thread != null && thread.isAlive()) {
            try {
                thread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        thread = null;
        log.debug("🛑 [IDS] TCPEventSubscriber stoppato.");
    }

}
