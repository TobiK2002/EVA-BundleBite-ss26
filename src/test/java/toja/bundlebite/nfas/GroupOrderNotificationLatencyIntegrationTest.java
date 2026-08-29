package toja.bundlebite.nfas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import rest.client.ApiClient;
import rest.client.ConsoleClient;
import rest.server.NotificationServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prüft, ob eine Änderung einer GroupOrder innerhalb von 0,5 Sekunden
 * als TCP-Socket-Nachricht beim beteiligten Client ankommt.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "notification.server.port=0"
)
class GroupOrderNotificationLatencyIntegrationTest {

    private static final int SOCKET_READ_TIMEOUT_MILLIS = 1_000;
    private static final long MAX_NOTIFICATION_LATENCY_MILLIS = 500;

    @LocalServerPort
    private int port;

    @Autowired
    private NotificationServer notificationServer;

    private ApiClient apiClient;
    private UUID restaurantId;
    private UUID groupOrderId;
    private String clientAEmail;
    private String clientBEmail;

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void notificationReachesExistingGroupOrderMemberWithinHalfASecond() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port + "/api");
        UUID dishId = createRestaurantAndDish();
        createUsers();
        groupOrderId = createGroupOrder();

        // B ist bereits Mitglied der GroupOrder, bevor die Socket-Verbindung geöffnet wird.
        addOrderEntry(clientBEmail, dishId, 1);

        try (Socket socket = new Socket("localhost", waitForNotificationServerPort());
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                     socket.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8))) {

            socket.setSoTimeout(SOCKET_READ_TIMEOUT_MILLIS);
            writer.write(clientBEmail);
            writer.newLine();
            writer.flush();

            assertTrue(waitForClientBRegistration(),
                    "Client B wurde nicht beim NotificationServer registriert");

            long startNanos = System.nanoTime();
            addOrderEntry(clientAEmail, dishId, 1);
            String notification = reader.readLine();
            long elapsedNanos = System.nanoTime() - startNanos;

            assertNotNull(notification, "Client B muss eine Socket-Benachrichtigung erhalten");
            assertTrue(notification.contains("Es wurde ein Bestelleintrag zur GroupOrder hinzugefügt."),
                    "Die empfangene Nachricht muss die Änderung der GroupOrder beschreiben");
            assertTrue(elapsedNanos < TimeUnit.MILLISECONDS.toNanos(MAX_NOTIFICATION_LATENCY_MILLIS),
                    () -> "Die Socket-Benachrichtigung benötigte "
                            + TimeUnit.NANOSECONDS.toMillis(elapsedNanos)
                            + " ms und überschreitet damit 0,5 Sekunden");
        }
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (apiClient == null) {
            return;
        }
        if (groupOrderId != null) {
            apiClient.delete("/group-orders/" + groupOrderId);
        }
        if (clientAEmail != null) {
            apiClient.delete("/users/" + clientAEmail);
        }
        if (clientBEmail != null) {
            apiClient.delete("/users/" + clientBEmail);
        }
        if (restaurantId != null) {
            apiClient.delete("/restaurants/" + restaurantId);
        }
    }

    private UUID createRestaurantAndDish() throws Exception {
        ConsoleClient.RestaurantResponse restaurant = apiClient.post(
                "/restaurants",
                new ConsoleClient.CreateRestaurantRequest(
                        "Socket-Test Restaurant", "Teststraße 1 04109 Leipzig", 0),
                ConsoleClient.RestaurantResponse.class
        );
        restaurantId = restaurant.id();

        return apiClient.post(
                "/restaurants/" + restaurantId + "/dishes",
                new ConsoleClient.CreateDishRequest(
                        "Socket-Test Gericht", "Gericht für den Latenztest", 9.99,
                        List.of("Teig", "Tomaten")),
                ConsoleClient.DishResponse.class
        ).id();
    }

    private void createUsers() throws Exception {
        clientAEmail = "socket-client-a-" + UUID.randomUUID() + "@example.test";
        clientBEmail = "socket-client-b-" + UUID.randomUUID() + "@example.test";
        createUser("Socket Client A", clientAEmail);
        createUser("Socket Client B", clientBEmail);
    }

    private void createUser(String name, String email) throws Exception {
        apiClient.post("/users",
                new ConsoleClient.CreateUserRequest(name, email, "Teststraße 1 04109 Leipzig"),
                ConsoleClient.UserResponse.class);
    }

    private UUID createGroupOrder() throws Exception {
        return apiClient.post(
                "/group-orders",
                new ConsoleClient.CreateGroupOrderRequest(restaurantId, clientAEmail, 5),
                ConsoleClient.GroupOrderResponse.class
        ).id();
    }

    private void addOrderEntry(String email, UUID dishId, int quantity) throws Exception {
        apiClient.post(
                "/group-orders/" + groupOrderId + "/order-entries",
                new ConsoleClient.CreateOrderEntryRequest(email, dishId, quantity),
                ConsoleClient.OrderEntryResponse.class
        );
    }

    private boolean waitForClientBRegistration() throws InterruptedException {
        long timeoutNanos = TimeUnit.SECONDS.toNanos(2);
        long startNanos = System.nanoTime();

        while (System.nanoTime() - startNanos < timeoutNanos) {
            if (notificationServer.isClientConnected(clientBEmail)) {
                return true;
            }
            Thread.sleep(10);
        }
        return false;
    }

    private int waitForNotificationServerPort() throws InterruptedException {
        long timeoutNanos = TimeUnit.SECONDS.toNanos(2);
        long startNanos = System.nanoTime();

        while (System.nanoTime() - startNanos < timeoutNanos) {
            int port = notificationServer.getListeningPort();
            if (port > 0) {
                return port;
            }
            Thread.sleep(10);
        }
        throw new IllegalStateException("NotificationServer wurde nicht gestartet");
    }
}
