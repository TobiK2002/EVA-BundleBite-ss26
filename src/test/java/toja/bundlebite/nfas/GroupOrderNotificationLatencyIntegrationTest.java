package toja.bundlebite.nfas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import rest.client.ApiClient;
import rest.client.ConsoleClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integrationstest zur nichtfunktionalen Anforderung: Änderungen an einer GroupOrder
 * werden innerhalb von 0,5 Sekunden per TCP-Socket an betroffene Clients übertragen.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupOrderNotificationLatencyIntegrationTest {

    private static final int NOTIFICATION_PORT = 8081;
    private static final long MAX_NOTIFICATION_LATENCY_MILLIS = 500;
    private static final int SOCKET_READ_TIMEOUT_MILLIS = 2_000;
    private static final String ENTRY_ADDED_NOTIFICATION =
            "Es wurde ein Bestelleintrag zur GroupOrder hinzugefügt.";

    @LocalServerPort
    private int port;

    private ApiClient apiClient;
    private UUID restaurantId;
    private UUID groupOrderId;
    private String clientAEmail;
    private String clientBEmail;

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void groupOrderChangeIsReceivedByExistingMemberViaSocketWithinHalfASecond() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port + "/api");
        UUID dishId = createRestaurantAndDish();
        createClients();
        groupOrderId = createGroupOrder();

        try (Socket clientBSocket = new Socket("localhost", NOTIFICATION_PORT);
             BufferedWriter clientBWriter = new BufferedWriter(new OutputStreamWriter(
                     clientBSocket.getOutputStream(), StandardCharsets.UTF_8));
             BufferedReader clientBReader = new BufferedReader(new InputStreamReader(
                     clientBSocket.getInputStream(), StandardCharsets.UTF_8))) {

            clientBSocket.setSoTimeout(SOCKET_READ_TIMEOUT_MILLIS);
            registerSocketClient(clientBWriter, clientBEmail);

            // Client B besitzt bereits einen Entry und die Nachricht bestätigt,
            // dass seine Socket-Verbindung vollständig beim Server registriert ist.
            apiClient.post(
                    "/group-orders/" + groupOrderId + "/order-entries",
                    new ConsoleClient.CreateOrderEntryRequest(clientBEmail, dishId, 1),
                    ConsoleClient.OrderEntryResponse.class
            );
            assertEquals(ENTRY_ADDED_NOTIFICATION, clientBReader.readLine(),
                    "Client B muss für seinen bestehenden Entry verbunden sein");

            long startNanos = System.nanoTime();
            apiClient.post(
                    "/group-orders/" + groupOrderId + "/order-entries",
                    new ConsoleClient.CreateOrderEntryRequest(clientAEmail, dishId, 1),
                    ConsoleClient.OrderEntryResponse.class
            );
            String notification = clientBReader.readLine();
            long elapsedNanos = System.nanoTime() - startNanos;

            assertNotNull(notification, "Client B muss eine Socket-Benachrichtigung erhalten");
            assertEquals(ENTRY_ADDED_NOTIFICATION, notification);
            assertTrue(elapsedNanos < TimeUnit.MILLISECONDS.toNanos(MAX_NOTIFICATION_LATENCY_MILLIS),
                    () -> "Socket-Benachrichtigung benötigte "
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

    private void createClients() throws Exception {
        clientAEmail = "socket-client-a-" + UUID.randomUUID() + "@example.test";
        clientBEmail = "socket-client-b-" + UUID.randomUUID() + "@example.test";

        apiClient.post("/users", new ConsoleClient.CreateUserRequest(
                "Socket Client A", clientAEmail, "Teststraße 1 04109 Leipzig"),
                ConsoleClient.UserResponse.class);
        apiClient.post("/users", new ConsoleClient.CreateUserRequest(
                "Socket Client B", clientBEmail, "Teststraße 1 04109 Leipzig"),
                ConsoleClient.UserResponse.class);
    }

    private UUID createGroupOrder() throws Exception {
        return apiClient.post(
                "/group-orders",
                new ConsoleClient.CreateGroupOrderRequest(restaurantId, clientAEmail, 5),
                ConsoleClient.GroupOrderResponse.class
        ).id();
    }

    private void registerSocketClient(BufferedWriter writer, String email) throws Exception {
        writer.write(email);
        writer.newLine();
        writer.flush();
    }
}
