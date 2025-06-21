package it.unibas.simulator.publisher;

import it.unibas.common.model.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.unibas.common.util.ObjectMapperFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class TCPSocketPublisher {
    private ServerSocket serverSocket;
    private final CopyOnWriteArrayList<Socket> clients = new CopyOnWriteArrayList<>();
    private final int port;

    private final ObjectMapper objectMapper = ObjectMapperFactory.create();

    public TCPSocketPublisher(int port) {
        this.port = port;
    }

    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                log.info("[Simulator] TCP Publisher on port {}", port);
                while (true) {
                    Socket client = serverSocket.accept();
                    clients.add(client);
                    log.info("[Simulator] IDS connected: {}", client.getInetAddress());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void broadcast(Event event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            for (Socket client : clients) {
                try{
                    OutputStream out = client.getOutputStream();
                    out.write((json + "\n").getBytes());
                    out.flush();
                } catch (Exception e) {
                    clients.remove(client);
                    try {
                        client.close();
                    } catch (Exception ex) {
                        log.error("Error: {}", ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            // Chiudere tutti i client
            for (Socket client : clients) {
                try {
                    client.close();
                } catch (Exception e) {
                    log.warn("Error closing client: {}", e.getMessage());
                }
            }
            clients.clear();
        } catch (Exception e) {
            log.error("Error stopping TCP publisher: {}", e.getMessage());
        }
    }

}
