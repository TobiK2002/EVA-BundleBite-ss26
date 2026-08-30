package toja.bundlebite.socketFA;


import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**Ein Testclient meldet sich an, erstellt eine Sammelbestellung und fügt ein Bestelleintrag zu ihr hinzu.
 * Danach wird dieser abgemeldet und meldet sich erneut an.
 * Jetzt wird überprüft ob dem Client die Socket-Benachrichtigungen zum Ablauf des Gruppenbestellungstimers weiterhin übermittelt werden.**/

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "notification.server.port=0")


 class UserReconnectTest {
    @LocalServerPort
    private int port;

    @Autowired
    private NotificationServer notificationServer;

    private ApiClient apiClient;
    private UUID restaurantId;
    private UUID groupOrderId;
    private String clientEmail;
    private UUID orderEntryId;

    @Test
    void userCanReconnectAndReceiveNotifications() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port + "/api");
        UUID dishId = createRestaurantAndDish();
        clientEmail = createUser();

        //Socket verbindung zum Client herstellen
        Socket socket = new Socket("localhost", notificationServer.getListeningPort());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        writer.write(clientEmail);
        writer.newLine();
        writer.flush();

        groupOrderId = createGroupOrder();
        orderEntryId = addOrderEntry(clientEmail, dishId, 2);
        assertTrue(reader.readLine().contains("Es wurde ein Bestelleintrag zur GroupOrder hinzugefügt."));
        //Socket funktioniert erstmal, jetzt User-Socket abmelden und erneut anmelden
        socket.close();

        Socket socket2 = new Socket("localhost", notificationServer.getListeningPort());
        BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(socket2.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader reader2 = new BufferedReader(new InputStreamReader(socket2.getInputStream(), StandardCharsets.UTF_8));
        writer2.write(clientEmail);
        writer2.newLine();
        writer2.flush();

        //Bestellungeintrag abändern
        changeOrderEntry(groupOrderId, orderEntryId, 1);

        //Prüfen, ob der erneut verbundene Client die "Aktualisieren"-Meldung bekommt
        assertTrue(reader2.readLine().contains("Es wurde ein Eintrag in der GroupOrder aktualisiert."));
        socket2.close();

        //Clean up
        apiClient.delete("/group-orders/" + groupOrderId);
        apiClient.delete("/users/" + clientEmail);
        apiClient.delete("/restaurants/" + restaurantId);
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

    private String createUser() throws Exception {
        String mail = "test@test.de";
        apiClient.post("/users",
                new ConsoleClient.CreateUserRequest("Test User", mail, "Teststraße 1 04109 Leipzig"),
                ConsoleClient.UserResponse.class);
        return mail;
    }
    private UUID createGroupOrder() throws Exception {
        return apiClient.post(
                "/group-orders",
                new ConsoleClient.CreateGroupOrderRequest(restaurantId, clientEmail, 5),
                ConsoleClient.GroupOrderResponse.class
        ).id();
    }

    private void changeOrderEntry(UUID orderId, UUID entryId, int quantity) throws Exception {
        apiClient.put(
                "/group-orders/" + orderId + "/order-entries/" + entryId,
                new ConsoleClient.UpdateOrderEntryRequest(quantity)
        );
    }


    private UUID addOrderEntry(String email, UUID dishId, int quantity) throws Exception {
        ConsoleClient.OrderEntryResponse orderEntryResponse = apiClient.post(
                "/group-orders/" + groupOrderId + "/order-entries",
                new ConsoleClient.CreateOrderEntryRequest(email, dishId, quantity),
                ConsoleClient.OrderEntryResponse.class
        );
        return orderEntryResponse.id();
    }
}