package rest.server;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NotificationServer {
        @Value("${notification.server.port:8081}")
        private int configuredPort;

        private volatile int listeningPort;

        private final ConcurrentHashMap<String, PrintWriter> connections =
                new ConcurrentHashMap<>();

    public static final Logger logger = LoggerFactory.getLogger(NotificationServer.class);

        @PostConstruct
        public void start() {
            Thread serverThread = new Thread(this::acceptConnections);
            serverThread.setDaemon(true);
            serverThread.start();
            logger.info("Notification Server wurde erfolgreich gestartet");
        }

        private void acceptConnections() {
            try (ServerSocket serverSocket = new ServerSocket(configuredPort)) {
                listeningPort = serverSocket.getLocalPort();

                while (true) {
                    Socket socket = serverSocket.accept();

                    Thread clientThread = new Thread(
                            () -> registerClient(socket)
                    );
                    clientThread.setDaemon(true);
                    clientThread.start();
                }

            } catch (IOException exception) {
                System.out.println("Notification-Server konnte nicht starten.");
            }
        }

        private void registerClient(Socket socket) {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                String userEmail = in.readLine();
                PrintWriter out = new PrintWriter(
                        socket.getOutputStream(), true
                );

                connections.put(userEmail, out);


                logger.info("Client " + userEmail + " Erfolgreich per Socket verbunden.");

                // Wartet, bis der Client beim Logout die Verbindung schließt.
                while (in.readLine() != null) {
                }

                connections.remove(userEmail);

                logger.info("Client " + userEmail + " hat Erfolgreich die Verbindung getrennt");

            } catch (IOException ignored) {
            }
        }

        public void notifyUser(String email, String message) {
            PrintWriter connection = connections.get(email);

            if (connection != null) {
                connection.println(message);
            }

            logger.info("An Client " + email + " Wurde folgende Nachricht gesendet: " + message);
        }

        public boolean isClientConnected(String email) {
            return connections.containsKey(email);
        }

        public int getListeningPort() {
            return listeningPort;
        }
    }
