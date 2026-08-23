package rest.server;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationServer {
        private final ConcurrentHashMap<String, PrintWriter> connections =
                new ConcurrentHashMap<>();

        @PostConstruct
        public void start() {
            Thread serverThread = new Thread(this::acceptConnections);
            serverThread.setDaemon(true);
            serverThread.start();
        }

        private void acceptConnections() {
            try (ServerSocket serverSocket = new ServerSocket(8081)) {

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

                // Wartet, bis der Client beim Logout die Verbindung schließt.
                while (in.readLine() != null) {
                }

                connections.remove(userEmail);

            } catch (IOException ignored) {
            }
        }

        public void notifyUser(String email, String message) {
            PrintWriter connection = connections.get(email);

            if (connection != null) {
                connection.println(message);
            }
        }
    }
