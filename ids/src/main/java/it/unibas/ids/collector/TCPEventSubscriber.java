package it.unibas.ids.collector;


import com.google.inject.Inject;
import it.unibas.ids.config.IdsProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

@Slf4j
public class TCPEventSubscriber {
    private final String host;
    private final int port;

    @Inject
    public TCPEventSubscriber(IdsProperties properties) {
        this.host = properties.getTcpHost();
        this.port = properties.getTcpPort();
    }

    public void start(EventCollector collector) {
        new Thread(() -> {
            try (Socket socket = new Socket(host, port);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                log.info("[IDS] Connected to simulator on {}:{}", host, port);
                String line;
                while ((line = in.readLine()) != null) {
                    // Elabora/aggiungi evento al collector

                    log.info("line: {}", line);

                    collector.onEventJson(line);
                }
            } catch (Exception e) {
                log.error("[IDS] Connection to simulator lost or error: {}", e.getMessage());
            }
        }).start();
    }
}
