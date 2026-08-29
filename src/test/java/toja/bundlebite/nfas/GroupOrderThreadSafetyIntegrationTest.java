package toja.bundlebite.nfas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import rest.client.ApiClient;
import rest.client.ConsoleClient;
import tools.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prüft, ob eine GroupOrder bei gleichzeitigen Änderungen mehrerer Clients
 * konsistent bleibt.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GroupOrderThreadSafetyIntegrationTest {

    private static final int USER_COUNT = 6;

    @LocalServerPort
    private int port;

    private ApiClient apiClient;
    private UUID restaurantId;
    private UUID groupOrderId;
    private final List<String> userEmails = new ArrayList<>();

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void simultaneousAdditionsAndChangesKeepGroupOrderConsistent() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port + "/api");
        UUID dishId = createRestaurantAndDish();
        createUsers();
        groupOrderId = createGroupOrder();

        // Jeder Nutzer besitzt bereits einen Eintrag, den er später bearbeitet.
        List<ConsoleClient.OrderEntryResponse> existingEntries = createInitialEntries(dishId);

        CountDownLatch clientsReady = new CountDownLatch(USER_COUNT);
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(USER_COUNT);

        try {
            List<Future<?>> actions = new ArrayList<>();

            for (int index = 0; index < USER_COUNT; index++) {
                String email = userEmails.get(index);
                UUID existingEntryId = existingEntries.get(index).id();
                int newQuantity = index + 2;

                actions.add(executor.submit(() -> {
                    ApiClient client = new ApiClient("http://localhost:" + port + "/api");

                    clientsReady.countDown();
                    assertTrue(startSignal.await(10, TimeUnit.SECONDS),
                            "Startsignal wurde nicht empfangen");

                    // Realistische parallele Aktion: weiteren Eintrag hinzufügen
                    // und den bereits vorhandenen Eintrag in derselben GroupOrder ändern.
                    client.post(
                            "/group-orders/" + groupOrderId + "/order-entries",
                            new ConsoleClient.CreateOrderEntryRequest(email, dishId, 1),
                            ConsoleClient.OrderEntryResponse.class
                    );
                    client.put(
                            "/group-orders/" + groupOrderId + "/order-entries/" + existingEntryId,
                            new ConsoleClient.UpdateOrderEntryRequest(newQuantity)
                    );
                    return null;
                }));
            }

            assertTrue(clientsReady.await(10, TimeUnit.SECONDS),
                    "Nicht alle Clients waren bereit");
            startSignal.countDown();

            for (Future<?> action : actions) {
                action.get(15, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        List<ConsoleClient.OrderEntryResponse> entries = apiClient.getList(
                "/group-orders/" + groupOrderId + "/order-entries",
                new TypeReference<>() {
                }
        );

        // Für jeden Nutzer müssen der ursprüngliche und der neue Eintrag existieren.
        assertEquals(USER_COUNT * 2, entries.size(),
                "Bei parallelen Aktionen darf kein Bestelleintrag verloren gehen");
        for (String email : userEmails) {
            long entriesForUser = entries.stream()
                    .filter(entry -> entry.userEmail().equals(email))
                    .count();
            assertEquals(2, entriesForUser,
                    "Jeder Nutzer muss genau zwei Einträge in der GroupOrder haben");
        }

        // Zusätzlich muss jede parallele Änderung des ursprünglichen Eintrags erhalten bleiben.
        for (int index = 0; index < USER_COUNT; index++) {
            ConsoleClient.OrderEntryResponse updatedEntry = apiClient.get(
                    "/order-entries/" + existingEntries.get(index).id(),
                    ConsoleClient.OrderEntryResponse.class
            );
            assertEquals(index + 2, updatedEntry.quantity(),
                    "Die Änderung von Nutzer " + (index + 1) + " darf nicht überschrieben werden");
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
        for (String email : userEmails) {
            apiClient.delete("/users/" + email);
        }
        if (restaurantId != null) {
            apiClient.delete("/restaurants/" + restaurantId);
        }
    }

    private UUID createRestaurantAndDish() throws Exception {
        ConsoleClient.RestaurantResponse restaurant = apiClient.post(
                "/restaurants",
                new ConsoleClient.CreateRestaurantRequest(
                        "Thread-Safety-Test Restaurant", "Teststraße 1 04109 Leipzig", 0),
                ConsoleClient.RestaurantResponse.class
        );
        restaurantId = restaurant.id();

        return apiClient.post(
                "/restaurants/" + restaurantId + "/dishes",
                new ConsoleClient.CreateDishRequest(
                        "Thread-Safety-Test Gericht", "Gericht für parallele Änderungen", 9.99,
                        List.of("Teig", "Tomaten")),
                ConsoleClient.DishResponse.class
        ).id();
    }

    private void createUsers() throws Exception {
        for (int index = 0; index < USER_COUNT; index++) {
            String email = "thread-safety-" + UUID.randomUUID() + "@example.test";
            apiClient.post(
                    "/users",
                    new ConsoleClient.CreateUserRequest(
                            "Test Nutzer " + (index + 1), email, "Teststraße 1 04109 Leipzig"),
                    ConsoleClient.UserResponse.class
            );
            userEmails.add(email);
        }
    }

    private UUID createGroupOrder() throws Exception {
        return apiClient.post(
                "/group-orders",
                new ConsoleClient.CreateGroupOrderRequest(restaurantId, userEmails.get(0), 5),
                ConsoleClient.GroupOrderResponse.class
        ).id();
    }

    private List<ConsoleClient.OrderEntryResponse> createInitialEntries(UUID dishId) throws Exception {
        List<ConsoleClient.OrderEntryResponse> entries = new ArrayList<>();
        for (String email : userEmails) {
            entries.add(apiClient.post(
                    "/group-orders/" + groupOrderId + "/order-entries",
                    new ConsoleClient.CreateOrderEntryRequest(email, dishId, 1),
                    ConsoleClient.OrderEntryResponse.class
            ));
        }
        return entries;
    }
}
