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
 * Integrationstest zur nichtfunktionalen Anforderung: mindestens 50
 * gleichzeitig aktive Clients muessen Aktionen am Server ausfuehren koennen.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ConcurrentClientsIntegrationTest {

    private static final int CLIENT_COUNT = 50;

    @LocalServerPort
    private int port;

    private ApiClient apiClient;

    private final List<String> createdUsers = new ArrayList<>();
    private UUID restaurantId;
    private UUID groupOrderId;

    @Test
    @Timeout(value = 45, unit = TimeUnit.SECONDS)
    void serverSupportsFiftyConcurrentClientsPerformingActions() throws Exception {
        apiClient = new ApiClient("http://localhost:" + port + "/api");

        UUID dishId = createRestaurantAndDish();
        createUsers();
        groupOrderId = createGroupOrder();

        CountDownLatch clientsReady = new CountDownLatch(CLIENT_COUNT);
        CountDownLatch startSignal = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(CLIENT_COUNT);

        try {
            List<Future<ConsoleClient.OrderEntryResponse>> responses = new ArrayList<>();

            for (String email : createdUsers) {
                responses.add(executor.submit(() -> {
                    ApiClient client = new ApiClient("http://localhost:" + port + "/api");

                    clientsReady.countDown();
                    assertTrue(startSignal.await(10, TimeUnit.SECONDS), "Startsignal wurde nicht empfangen");

                    return client.post(
                            "/group-orders/" + groupOrderId + "/order-entries",
                            new ConsoleClient.CreateOrderEntryRequest(email, dishId, 1),
                            ConsoleClient.OrderEntryResponse.class
                    );
                }));
            }

            assertTrue(clientsReady.await(30, TimeUnit.SECONDS), "Nicht alle Clients waren bereit");
            startSignal.countDown();

            for (Future<ConsoleClient.OrderEntryResponse> response : responses) {
                response.get(20, TimeUnit.SECONDS);
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

        assertEquals(CLIENT_COUNT, entries.size(),
                "Alle 50 Bestelleintraege muessen gespeichert worden sein");
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (apiClient == null) {
            return;
        }

        if (groupOrderId != null) {
            apiClient.delete("/group-orders/" + groupOrderId);
        }

        for (String email : createdUsers) {
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
                        "Lasttest Restaurant",
                        "Teststraße 1 04109 Leipzig",
                        0
                ),
                ConsoleClient.RestaurantResponse.class
        );

        restaurantId = restaurant.id();

        ConsoleClient.DishResponse dish = apiClient.post(
                "/restaurants/" + restaurantId + "/dishes",
                new ConsoleClient.CreateDishRequest(
                        "Lasttest Gericht",
                        "Gericht fuer den Paralleltest",
                        9.99,
                        List.of("Teig", "Tomaten")
                ),
                ConsoleClient.DishResponse.class
        );

        return dish.id();
    }

    private void createUsers() throws Exception {
        for (int index = 0; index < CLIENT_COUNT; index++) {
            String email = "lasttest-" + UUID.randomUUID() + "@example.test";

            apiClient.post(
                    "/users",
                    new ConsoleClient.CreateUserRequest(
                            "Test Client " + index,
                            email,
                            "Teststraße 1 04109 Leipzig"
                    ),
                    ConsoleClient.UserResponse.class
            );

            createdUsers.add(email);
        }
    }

    private UUID createGroupOrder() throws Exception {
        ConsoleClient.GroupOrderResponse groupOrder = apiClient.post(
                "/group-orders",
                new ConsoleClient.CreateGroupOrderRequest(
                        restaurantId,
                        createdUsers.get(0),
                        5
                ),
                ConsoleClient.GroupOrderResponse.class
        );

        return groupOrder.id();
    }
}
