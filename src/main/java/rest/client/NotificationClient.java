package rest.client;

import java.io.*;
import java.net.Socket;

public class NotificationClient implements AutoCloseable{
    private final String userEmail;
    private Socket socket;

    public NotificationClient(String email) {
        this.userEmail = email;
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8081);

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true
            );
            out.println(userEmail); // einmalig beim Verbinden identifizieren

            Thread listener = new Thread(() -> listenForMessages(socket));
            listener.setDaemon(true);
            listener.start();

        } catch (IOException exception) {
            System.out.println("Benachrichtigungen nicht verfügbar.");
        }
    }

    private void listenForMessages(Socket socket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("\n[UPDATE] " + message);
            }
        } catch (IOException ignored) {
            // Verbindung wurde geschlossen
        }
    }

    @Override
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
